package com.github.lstephen.ootp.ai.regression

import collection.JavaConversions._

import com.github.lstephen.ootp.ai.player.ratings.BattingRatings
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings
import com.github.lstephen.ootp.ai.site.SiteHolder
import com.github.lstephen.ootp.ai.site.Version

import com.typesafe.scalalogging.StrictLogging

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.nn.conf.GradientNormalization
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.Updater
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit

import org.deeplearning4j.optimize.listeners.ScoreIterationListener

//import org.deeplearning4j.ui.weights.HistogramIterationListener

import org.nd4j.linalg.dataset.{ DataSet => Nd4jDataSet }
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions

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
}

object Input {
  def apply(ds: Integer*): Input = apply(ds.toList.map(_.doubleValue))
  def apply(ds: List[Double]): Input = new Input(ds.map(Some(_)))
}


class DataPoint(val input: Input, val output: Double) {
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

    (0 to (inputSize - 1))
      .map { idx =>
        val vs = ds.map(_.input.get(idx)).flatten

        vs.sum / vs.length
      }
      .toList
  }

  def averageForColumn(i: Integer): Double = averages.get(i)

  def map[T](f: DataPoint => T): List[T] = ds.map(f)

  val length = ds.length

  def inputSize = ds.head.input.length

  def toDataSet: DataSetIterator = {
    val inArray = Nd4j.vstack(
      map { d: DataPoint =>
        Nd4j.create(d.input.toArray(averageForColumn(_)).map(_ / 100.0), Array(1, inputSize))
      })

    //logger.info(s"$inArray")

    val outArray = Nd4j.create(map(_.output).toArray)

    //logger.info(s"$outArray")

    new ListDataSetIterator(new Nd4jDataSet(inArray, outArray).asList, 1)
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

  var _regression: Option[MultiLayerNetwork] = None

  def regression = _regression match {
    case Some(r) => r
    case None    =>
      logger.info(s"Creating regression for $label, size: ${data.length}, averages: ${data.averages}")

      val dataSet = data.toDataSet

      val conf = new NeuralNetConfiguration.Builder()
        .seed(42)
        .iterations(1)
        .learningRate(1e-6)
        .weightInit(WeightInit.ZERO)
        //.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
        .updater(Updater.RMSPROP).rmsDecay(0.9)
        .list
        //.layer(0, new DenseLayer.Builder()
        //  .nIn(data.inputSize)
        //  .nOut(data.inputSize * 2)
        //  .activation("tanh")
        //  .build)
        .layer(0, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
          .nIn(data.inputSize)
          .nOut(1)
          .activation("identity")
          .build)
        .pretrain(false)
        .backprop(true)
        .build

      val network = new MultiLayerNetwork(conf)
      network.init
      network.setListeners(new ScoreIterationListener(50))
      //network.setListeners(new HistogramIterationListener(1))

      for( i <- 0 to 1000 ) {
        dataSet.reset
        network.fit(dataSet);
      }

      logger.info(s"${network}")

      _regression = Some(network)
      network
  }


  def addData[T](x: T, y: Double)(implicit regressable: Regressable[T]): Unit = {
    data = data :+ new DataPoint(regressable.toInput(x), y)
    _regression = None
  }

  def getN: Long = data.length

  def predict[T: Regressable](x: T): Double =
    predict(implicitly[Regressable[T]].toInput(x))

  def predict(xs: Input): Double =
    regression
      .output(Nd4j.create(xs.toArray(data.averageForColumn(_)).map(_ / 100.0)))
      .getDouble(0)

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
