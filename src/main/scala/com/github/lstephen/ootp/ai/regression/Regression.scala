package com.github.lstephen.ootp.ai.regression

import collection.JavaConversions._

import com.github.lstephen.ootp.ai.player.ratings.BattingRatings
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings
import com.github.lstephen.ootp.ai.site.SiteHolder
import com.github.lstephen.ootp.ai.site.Version

import com.typesafe.scalalogging.StrictLogging

import org.encog.engine.network.activation.ActivationSigmoid

import org.encog.mathutil.randomize.generate.BasicGenerateRandom

import org.encog.ml.MLRegression

import org.encog.ml.data.versatile.NormalizationHelper
import org.encog.ml.data.versatile.VersatileMLDataSet
import org.encog.ml.data.versatile.columns.ColumnDefinition
import org.encog.ml.data.versatile.columns.ColumnType
import org.encog.ml.data.versatile.missing.MeanMissingHandler
import org.encog.ml.data.versatile.missing.MissingHandler
import org.encog.ml.data.versatile.normalizers.PassThroughNormalizer
import org.encog.ml.data.versatile.normalizers.strategies.BasicNormalizationStrategy
import org.encog.ml.data.versatile.sources.VersatileDataSource

import org.encog.ml.train.strategy.StopTrainingStrategy

import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation

import scala.collection.mutable.ListBuffer


class DataPoint(val input: Array[Option[Double]], val output: Double) {
  def toArray: Array[Option[Double]] = Some(output) +: input
  def toStringArray = DataPoint.toStringArray(toArray)
}

object DataPoint {
  def toStringArray(xs: Array[Option[Double]]): Array[String] =
    xs.map { x => x.map(_.toString).getOrElse("?") }
}

class InMemoryDataSource extends VersatileDataSource {
  var index = 0

  val data = ListBuffer.empty[DataPoint]

  def add(p: DataPoint): Unit = data += p

  def columnIndex(n: String) = -1

  def readLine: Array[String] = {
    index = index + 1

    if (index >= data.length) return null

    data(index).toStringArray
  }

  def rewind: Unit = { index = -1 }
}

trait Regressable[-T] {
  def registerSourceColumns(ds: VersatileMLDataSet, label: String): List[ColumnDefinition]
  def toArray(t: T): Array[Option[Double]]
}

object Regressable {
  // Note that this is java.lang.Integer
  def toSomeDouble(i: Integer): Some[Double] = Some(i.doubleValue)

  def toSourceColumns(cs: List[String], ds: VersatileMLDataSet, label: String) =
    cs
      .zipWithIndex
      .map { case (name, idx) =>
        ds.defineSourceColumn(s"${label}::${name}", idx + 1, ColumnType.continuous)
      }

  implicit object RegressableDouble extends Regressable[Double] {
    def registerSourceColumns(ds: VersatileMLDataSet, label: String) =
      List(ds.defineSourceColumn(s"${label}::Input", 1, ColumnType.continuous))

    def toArray(d: Double) = Array(Some(d))
  }

  implicit object RegressableBattingRatings extends Regressable[BattingRatings[_ <: Object]] {
    def registerSourceColumns(ds: VersatileMLDataSet, label: String) =
      toSourceColumns(
        List("Contact", "Gap", "Power", "Eye", "K"), ds, s"Batting::${label}")

    def toArray(r: BattingRatings[_ <: Object]) = {
      var k = if (r.getK.isPresent) Some(r.getK.get.doubleValue) else None

      Array(r.getContact, r.getGap, r.getPower, r.getEye).map(toSomeDouble(_)) :+ k
    }
  }

  implicit object RegressablePitchingRatings extends Regressable[PitchingRatings[_ <: Object]] {
    val version = SiteHolder.get.getType

    def registerSourceColumns(ds: VersatileMLDataSet, label: String) = {
      var columns = List("Power", "Eye", "K", "GBP") ++
        (if (version == Version.OOTP5) List("Hits", "Doubles") else List())

      toSourceColumns(columns, ds, s"Pitching::${label}")
    }

    def toArray(r: PitchingRatings[_ <: Object]) = {
      var as: Array[Option[Double]] =
        Array(r.getMovement, r.getControl, r.getStuff).map(toSomeDouble(_))

      as = as :+ (if (r.getGroundBallPct.isPresent) Some(r.getGroundBallPct.get.doubleValue) else None)

      if (version == Version.OOTP5) {
        as = as ++ (Array(r.getHits, r.getGap).map(toSomeDouble(_)))
      }

      as
    }
  }
}

class Regression(label: String, category: String) {

  import Regressable._

  class LoggingMissingHandler extends MissingHandler with StrictLogging {
    val handler = new MeanMissingHandler

    def init(n: NormalizationHelper) =
      handler.init(n)

    def processString(c: ColumnDefinition) = handler.processString(c)

    def processDouble(c: ColumnDefinition) = {
      val d = handler.processDouble(c)
      logger.info(s"Replaced unknown with: ${d})")
      d
    }
  }

  val data = new InMemoryDataSource

  val dataSet = new VersatileMLDataSet(data)


  val outputColumn = dataSet.defineSourceColumn(s"${label}::Output", 0, ColumnType.continuous)

  var _regression: Option[MLRegression] = None

  def normalizationHelper = dataSet.getNormHelper

  def regression = _regression match {
    case Some(r) => r
    case None    =>
      println(s"Creating regression for $label, size: ${data.data.length}")

      dataSet defineSingleOutputOthersInput outputColumn
      dataSet.analyze

      val normalization = new BasicNormalizationStrategy
      normalization.assignInputNormalizer(ColumnType.continuous, new PassThroughNormalizer)
      normalization.assignOutputNormalizer(ColumnType.continuous, new PassThroughNormalizer)

      dataSet.getNormHelper.setStrategy(normalization)
      dataSet.getNormHelper.defineUnknownValue("?")
      dataSet.normalize

      println(s"${normalizationHelper}")

      val network = new BasicNetwork

      network.addLayer(new BasicLayer(null, true, dataSet.getCalculatedInputSize))
      network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1))

      network.getStructure.finalizeStructure
      network.reset

      val train = new ResilientPropagation(network, dataSet)

      val strategy = new StopTrainingStrategy

      train.addStrategy(strategy)

      while (!train.isTrainingDone) {
        train.iteration();

        println(f"${train.getIteration}%3d: ${train.getError}%.8f")
      }

      throw new IllegalStateException


      /*val m = new EncogModel(dataSet)
      m.selectMethod(dataSet, MLMethodFactory.TYPE_FEEDFORWARD)
      m.setReport(new ConsoleStatusReportable)


      m.holdBackValidation(0.0, true, 1001)
      m.selectTrainingType(dataSet)

      val bestMethod = m.crossvalidate(2, true).asInstanceOf[MLRegression]

      println(s"Training error: ${m.calculateError(bestMethod, m.getTrainingDataset)}")
      println(s"Validation error: ${m.calculateError(bestMethod, m.getValidationDataset)}")

      println(s"${normalizationHelper}")
      println(s"Final model: ${bestMethod}")

      _regression = Some(bestMethod)
      bestMethod*/
  }


  def addData[T](x: T, y: Double)(implicit regressable: Regressable[T]): Unit = {
    if (data.data.isEmpty) {
      regressable.registerSourceColumns(dataSet, label)
        .foreach { c => dataSet.getNormHelper.defineMissingHandler(c, new LoggingMissingHandler) }
    }

    data.add(new DataPoint(regressable.toArray(x), y))
    _regression = None
  }

  def getN: Long = data.data.length

  def predict[T: Regressable](x: T): Double =
    predict(implicitly[Regressable[T]].toArray(x))

  def predict(xs: Array[Option[Double]]): Double = {
    val r = regression // Force model computation

    val input = normalizationHelper.allocateInputVector

    normalizationHelper.normalizeInputVector(
      DataPoint.toStringArray(xs), input.getData, false)

    normalizationHelper
      .denormalizeOutputVectorToString(r.compute(input))(0)
      .toDouble
  }

  def mse =
    (data.data.map{ p => math.pow(p.output - predict(p.input), 2) }.sum) / data.data.length

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
