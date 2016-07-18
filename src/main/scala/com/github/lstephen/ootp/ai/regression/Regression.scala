package com.github.lstephen.ootp.ai.regression

import collection.JavaConversions._

import com.github.lstephen.ootp.ai.player.ratings.BattingRatings
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings
import com.github.lstephen.ootp.ai.site.SiteHolder
import com.github.lstephen.ootp.ai.site.Version

import com.typesafe.scalalogging.StrictLogging

import org.encog.engine.network.activation.ActivationLinear

import org.encog.mathutil.randomize.generate.BasicGenerateRandom

import org.encog.ml.MLRegression

import org.encog.ml.data.MLData
import org.encog.ml.data.MLDataPair
import org.encog.ml.data.MLDataSet
import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataPair
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.ml.data.folded.FoldedDataSet

import org.encog.ml.train.strategy.StopTrainingStrategy

import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.cross.CrossValidationKFold
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation
import org.encog.neural.networks.training.propagation.quick.QuickPropagation

import scala.math.ScalaNumericAnyConversions

class Input(private val is: List[Option[Double]]) {
  def :+(rhs: Double): Input = this :+ Some(rhs)
  def :+(rhs: Option[Double]): Input = new Input(is :+ rhs)
  def ++(rhs: Input): Input = new Input(is ++ rhs.is)

  val length = is.length

  def get(idx: Integer): Option[Double] = is get idx

  def toArray(f: Int => Double): Array[Double] =
    is
      .zipWithIndex
      .map {
        case (d, idx) => d getOrElse f(idx)
      }
      .toArray

    def toMLData(f: Int => Double): MLData = new BasicMLData(toArray(f))
}

object Input {
  def apply(ds: Integer*): Input = apply(ds.toList.map(_.doubleValue))
  def apply(ds: List[Double]): Input = new Input(ds.map(Some(_)))
}


class DataPoint(val input: Input, val output: Double) {
  def toMLDataPair(implicit ds: DataSet): MLDataPair =
    new BasicMLDataPair(input.toMLData(ds.averageForColumn(_)), new BasicMLData(Array(output)))
}


class DataSet(ds: List[DataPoint]) extends StrictLogging {
  def :+(rhs: DataPoint): DataSet = {
    if (!ds.isEmpty && rhs.input.length != ds.head.input.length) {
      throw new IllegalArgumentException
    }

    new DataSet(ds :+ rhs)
  }

  lazy val averages: List[Double] = {
    logger.info("Calculating averages...")

    (0 to (ds.head.input.length - 1))
      .map { idx =>
        val vs = ds.map(_.input.get(idx)).flatten

        vs.sum / vs.length
      }
      .toList
  }

  def averageForColumn(i: Integer): Double = averages.get(i)

  def map[T](f: DataPoint => T): List[T] = ds.map(f)

  val length = ds.length

  def toMLDataSet: MLDataSet = {
    implicit val ds = this
    new BasicMLDataSet(ds.map(_.toMLDataPair))
  }
}

object DataSet {
  def apply(): DataSet = new DataSet(List())
}


trait Regressable[-T] {
  def toInput(t: T): Input
}

object Regressable {
  // Note that this is java.lang.Integer
  def toSomeDouble(i: Integer): Some[Double] = Some(i.doubleValue)

  implicit object RegressableBattingRatings extends Regressable[BattingRatings[_ <: Object]] {
    def toInput(r: BattingRatings[_ <: Object]) = {
      var k = if (r.getK.isPresent) Some(r.getK.get.doubleValue) else None

      Input(r.getContact, r.getGap, r.getPower, r.getEye) :+ k
    }
  }

  implicit object RegressablePitchingRatings extends Regressable[PitchingRatings[_ <: Object]] {
    val version = SiteHolder.get.getType

    def toInput(r: PitchingRatings[_ <: Object]) = {
      var as: Input =
        Input(r.getMovement, r.getControl, r.getStuff)

      as = as :+ (if (r.getGroundBallPct.isPresent) Some(r.getGroundBallPct.get.doubleValue) else None)

      if (version == Version.OOTP5) {
        as = as ++ Input(r.getHits, r.getGap)
      }

      as
    }
  }
}


class Regression(label: String, category: String) extends StrictLogging {

  import Regressable._

  var data: DataSet = DataSet()

  var _regression: Option[MLRegression] = None

  def regression = _regression match {
    case Some(r) => r
    case None    =>
      logger.info(s"Creating regression for $label, size: ${data.length}, averages: ${data.averages}")

      val dataSet = data.toMLDataSet

      val network = new BasicNetwork

      network.addLayer(new BasicLayer(null, true, dataSet.getInputSize))
      network.addLayer(new BasicLayer(new ActivationLinear(), false, 1))

      network.getStructure.finalizeStructure
      network.reset

      val folded = new FoldedDataSet(dataSet)

      val train = new CrossValidationKFold(new ResilientPropagation(network, folded), 10)

      val strategy = new StopTrainingStrategy

      train.addStrategy(strategy)

      var iteration = 0;
      while (!train.isTrainingDone) {
        train.iteration();

        iteration += 1;
        println(f"${iteration}%3d: ${train.getError}%.8f")
      }

      _regression = Some(network)
      network
  }


  def addData[T](x: T, y: Double)(implicit regressable: Regressable[T]): Unit = {
    if (data.length < 20000)
      data = data :+ new DataPoint(regressable.toInput(x), y)

    _regression = None
  }

  def getN: Long = data.length

  def predict[T: Regressable](x: T): Double =
    predict(implicitly[Regressable[T]].toInput(x))

  def predict(xs: Input): Double =
    regression.compute(xs.toMLData(data.averageForColumn(_))).getData(0)

  def mse =
    (data.map{ p => math.pow(p.output - predict(p.input), 2) }.sum) / data.length

  def rsme = math.pow(mse, 0.5)

  def format: String = {
    f"$label%15s | ${rsme}%.3f"
  }

  // For Java interop
  def addBattingData(x: BattingRatings[_ <: Object], y: Double): Unit = addData(x, y)(Regressable.RegressableBattingRatings)
  def addPitchingData(x: PitchingRatings[_ <: Object], y: Double): Unit = addData(x, y)(Regressable.RegressablePitchingRatings)

  def predictBatting(x: BattingRatings[_ <: Object]): Double = predict(x)(Regressable.RegressableBattingRatings)
  def predictPitching(x: PitchingRatings[_ <: Object]): Double = predict(x)(Regressable.RegressablePitchingRatings)
}
