package com.github.lstephen.ootp.ai.regression

import collection.JavaConversions._

import com.github.lstephen.ootp.ai.player.ratings.BattingRatings
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings
import com.github.lstephen.ootp.ai.site.SiteHolder
import com.github.lstephen.ootp.ai.site.Version

import com.typesafe.scalalogging.StrictLogging

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

import scala.math.ScalaNumericAnyConversions

object Spark {
  lazy val session = {
    val s = SparkSession.builder.master("local").appName("ootp-ai").getOrCreate
    sys.ShutdownHookThread { s.stop }
    s
  }
}

trait Regressable[-T] {
  def toInput(t: T): Input
}

object Regressable {
  // Note that this is java.lang.Integer
  def toSomeDouble(i: Integer): Some[Double] = Some(i.doubleValue)

  implicit object RegressableBattingRatings
      extends Regressable[BattingRatings[_]] {
    def toInput(r: BattingRatings[_]) = {
      var extras = List(r.getK, r.getRunningSpeed).map { o =>
        if (o.isPresent) Some(o.get.doubleValue) else None
      }

      Input(r.getContact, r.getGap, r.getPower, r.getEye) ++ new Input(extras)
    }
  }

  implicit object RegressablePitchingRatings
      extends Regressable[PitchingRatings[_]] {
    val version = SiteHolder.get.getType

    def toInput(r: PitchingRatings[_]) = {
      var as: Input =
        Input(r.getMovement, r.getControl, r.getStuff)

      as = as :+ (if (r.getGroundBallPct.isPresent)
                    Some(r.getGroundBallPct.get.doubleValue)
                  else None)

      if (version == Version.OOTP5) {
        as = as ++ Input(r.getHits, r.getGap)
      }

      as
    }
  }
}

class Regression(label: String) extends StrictLogging {

  import Regressable._

  var data: DataSet = DataSet()

  var _regression: Option[Model.Predict] = None

  val model = new RandomForestModel

  def regression = _regression match {
    case Some(r) => r
    case None =>
      logger.info(
        s"Creating regression for $label, size: ${data.length}, averages: ${data.averages}")

      val p = model train data

      _regression = Some(p)

      p
  }

  def addData[T](x: T, y: Double)(implicit regressable: Regressable[T]): Unit = {
    data = data :+ new DataPoint(regressable.toInput(x), y)
    _regression = None
  }

  def train: Unit = regression

  def getN: Long = data.length

  def predict[T: Regressable](xs: Seq[T]): Seq[Double] =
    predict(xs.map(implicitly[Regressable[T]].toInput(_)))

  def predict(xs: Seq[Input]): Seq[Double] = regression(xs)

  def mse =
    ((predict(data.map(_.input)), data.map(_.output)).zipped.map {
      case (p, o) => math.pow(o - p, 2)
    }.sum) / data.length

  def rsme = math.pow(mse, 0.5)

  def format: String = {
    f"$label%15s | ${rsme}%.3f"
  }
}
