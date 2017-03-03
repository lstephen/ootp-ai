package com.github.lstephen.ootp.ai.regression

import collection.JavaConversions._

import com.github.lstephen.ootp.ai.player.ratings.{
  BattingRatings,
  PitchingRatings
}
import com.github.lstephen.ootp.ai.site.{SiteHolder, Version}
import com.google.common.base.Optional
import com.typesafe.scalalogging.StrictLogging
import scala.math.ScalaNumericAnyConversions

trait Regressable[-T] {
  def toInput(t: T): Input
  def features: Seq[String]
}

object Regressable {
  // Note that this is java.lang.Integer
  def toSomeDouble(i: Integer): Some[Double] = Some(i.doubleValue)

  val version = SiteHolder.get.getType

  def s(n: Number): Option[Number] = Some(n)
  def o[T](o: Optional[T]): Option[T] = if (o.isPresent) Some(o.get) else None

  implicit object RegressableBattingRatings
      extends Regressable[BattingRatings[_]] {

    def toInput(r: BattingRatings[_]): Input = version match {
      case Version.OOTP5 =>
        Input(s(r.getContact),
              s(r.getGap),
              o(r.getTriples),
              s(r.getPower),
              s(r.getEye),
              o(r.getK),
              o(r.getRunningSpeed))
      case Version.OOTP6 =>
        Input(s(r.getContact),
              s(r.getGap),
              s(r.getPower),
              s(r.getEye),
              o(r.getK),
              o(r.getRunningSpeed))
    }

    val features = version match {
      case Version.OOTP5 =>
        Seq("Hits",
            "Doubles",
            "Triples",
            "Homeruns",
            "Walks",
            "Strikeouts",
            "Running Speed")
      case Version.OOTP6 =>
        Seq("Contact", "Gap", "Power", "Eye", "Avoid K's", "Running Speed")
    }
  }

  implicit object RegressablePitchingRatings
      extends Regressable[PitchingRatings[_]] {

    def toInput(r: PitchingRatings[_]) = version match {
      case Version.OOTP5 =>
        Input(o(r.getRuns),
              s(r.getHits),
              s(r.getGap),
              s(r.getMovement),
              s(r.getControl),
              s(r.getStuff),
              o(r.getGroundBallPct),
              s(r.getEndurance))
      case Version.OOTP6 =>
        Input(s(r.getStuff),
              s(r.getControl),
              s(r.getMovement),
              o(r.getGroundBallPct),
              s(r.getEndurance))
    }

    val features = version match {
      case Version.OOTP5 =>
        Seq("Runs",
            "Hits",
            "Doubles",
            "Homeruns",
            "Walks",
            "Strikeouts",
            "Groundball Pct.",
            "Endurance")
      case Version.OOTP6 =>
        Seq("Stuff", "Control", "Movement", "Groundball Pct.", "Endurance")
    }
  }
}

class Regression(label: String) extends StrictLogging {

  import Regressable._

  var data: DataSet = DataSet()

  var _regression: Option[RegressionPyModel.Predict] = None

  val model: RegressionPyModel = new RegressionPyModel

  def regression: RegressionPyModel.Predict = _regression match {
    case Some(r) => r
    case None =>
      logger.info(
        s"Creating regression for $label, size: ${data.length}, averages: ${data.averages}")

      val p = model train data

      _regression = Some(p)

      p
  }

  def addData[T](x: T, y: Double, w: Int)(
      implicit regressable: Regressable[T]): Unit = {
    data = data :+ new DataPoint(regressable.toInput(x), y, w)
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

  def modelReport = regression.report(label)
}

object Regression {
  def predict[T: Regressable](rs: Map[String, Regression],
                              xs: Seq[T]): Map[String, Seq[Double]] =
    predict(rs, xs.map(implicitly[Regressable[T]].toInput(_)))

  def predict(rs: Map[String, Regression],
              xs: Seq[Input]): Map[String, Seq[Double]] = {
    val models = rs.mapValues(_.regression)

    RegressionPyModel.predict(models, xs)
  }
}
