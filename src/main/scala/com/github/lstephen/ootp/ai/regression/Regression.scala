package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.site.SiteHolder

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

trait Regressable[T] {
  def registerSourceColumns(ds: VersatileMLDataSet, label: String): List[ColumnDefinition]
  def toArray(t: T): Array[Option[Double]]
}

object Regressable {
  implicit object RegressableDouble extends Regressable[Double] {
    def registerSourceColumns(ds: VersatileMLDataSet, label: String) =
      List(ds.defineSourceColumn(s"${label}::Input", 1, ColumnType.continuous))

    def toArray(d: Double) = Array(Some(d))
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


  def addData[T: Regressable](x: T, y: Double): Unit = {
    if (data.data.isEmpty) {
      implicitly[Regressable[T]].registerSourceColumns(dataSet, label)
        .foreach { c => dataSet.getNormHelper.defineMissingHandler(c, new MeanMissingHandler) }
    }

    data.add(new DataPoint(implicitly[Regressable[T]].toArray(x), y))
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
  def addBattingData(x: Double, y: Double): Unit = addData(x, y)
  def addPitchingData(x: Double, y: Double): Unit = addData(x, y)

  def predictBatting(x: Double): Double = predict(x)
  def predictPitching(x: Double): Double = predict(x)
}
