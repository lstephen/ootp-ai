package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.player.ratings.BattingRatings
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings
import com.github.lstephen.ootp.ai.site.SiteHolder
import com.github.lstephen.ootp.ai.site.Version

import org.encog.ConsoleStatusReportable

import org.encog.ml.MLRegression

import org.encog.ml.data.versatile.VersatileMLDataSet
import org.encog.ml.data.versatile.columns.ColumnDefinition
import org.encog.ml.data.versatile.columns.ColumnType
import org.encog.ml.data.versatile.missing.MeanMissingHandler
import org.encog.ml.data.versatile.sources.VersatileDataSource

import org.encog.ml.factory.MLMethodFactory

import org.encog.ml.model.EncogModel

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
      var columns = List("Power", "Eye", "K") ++
        (if (version == Version.OOTP5) List("Hits", "Doubles") else List())

      toSourceColumns(columns, ds, s"Pitching::${label}")
    }

    def toArray(r: PitchingRatings[_ <: Object]) = {
      var as = Array(r.getMovement, r.getControl, r.getStuff)

      if (version == Version.OOTP5) {
        as = as ++ Array(r.getHits, r.getGap)
      }

      as.map(toSomeDouble(_))
    }
  }
}

class Regression(label: String, category: String) {

  import Regressable._

  val data = new InMemoryDataSource

  val dataSet = new VersatileMLDataSet(data)

  dataSet.getNormHelper defineUnknownValue "?"

  val outputColumn = dataSet.defineSourceColumn(s"${label}::Output", 0, ColumnType.continuous)

  var _model: Option[EncogModel] = None
  var _regression: Option[MLRegression] = None

  def normalizationHelper = dataSet.getNormHelper

  def regression = _regression match {
    case Some(r) => r
    case None    =>
      println(s"Creating regression for $label, size: ${data.data.length}")

      dataSet defineSingleOutputOthersInput outputColumn

      dataSet.analyze

      val m = new EncogModel(dataSet)
      m.selectMethod(dataSet, MLMethodFactory.TYPE_FEEDFORWARD)
      m.setReport(new ConsoleStatusReportable)

      dataSet.normalize

      m.holdBackValidation(0.3, true, 666)
      m.selectTrainingType(dataSet)

      val bestMethod = m.crossvalidate(5, true).asInstanceOf[MLRegression]

      println(s"Training error: ${m.calculateError(bestMethod, m.getTrainingDataset)}")
      println(s"Validation error: ${m.calculateError(bestMethod, m.getValidationDataset)}")

      println(s"${normalizationHelper}")
      println(s"Final model: ${bestMethod}")

      _regression = Some(bestMethod)
      bestMethod
  }


  def addData[T](x: T, y: Double)(implicit regressable: Regressable[T]): Unit = {
    if (data.data.isEmpty) {
      regressable.registerSourceColumns(dataSet, label)
        .foreach { c => dataSet.getNormHelper.defineMissingHandler(c, new MeanMissingHandler) }
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
