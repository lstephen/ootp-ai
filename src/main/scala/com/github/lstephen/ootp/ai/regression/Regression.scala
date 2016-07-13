package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.site.SiteHolder

import org.encog.ConsoleStatusReportable

import org.encog.ml.MLRegression

import org.encog.ml.data.versatile.VersatileMLDataSet
import org.encog.ml.data.versatile.columns.ColumnType
import org.encog.ml.data.versatile.sources.VersatileDataSource

import org.encog.ml.factory.MLMethodFactory

import org.encog.ml.model.EncogModel

import scala.collection.mutable.ListBuffer

class DataPoint(val input: Double, val output: Double) {
  def toArray: Array[Double] = Array(output, input)
}

class InMemoryDataSource extends VersatileDataSource {
  var index = 0

  val data = ListBuffer.empty[DataPoint]

  def add(p: DataPoint): Unit = data += p

  def columnIndex(n: String) = -1

  def readLine: Array[String] = {
    index = index + 1

    if (index >= data.length) return null

    data(index).toArray.map(_.toString)
  }

  def rewind: Unit = { index = -1 }
}


class Regression(label: String, category: String) {

  val data = new InMemoryDataSource

  val dataSet = new VersatileMLDataSet(data)

  val inputColumn = dataSet.defineSourceColumn(s"${label}::Input", 1, ColumnType.continuous)
  val outputColumn = dataSet.defineSourceColumn(s"${label}::Output", 0, ColumnType.continuous)

  dataSet defineSingleOutputOthersInput outputColumn

  var _model: Option[EncogModel] = None
  var _regression: Option[MLRegression] = None

  def normalizationHelper = dataSet.getNormHelper

  def regression = _regression match {
    case Some(r) => r
    case None    =>
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

  def addData(x: Double, y: Double): Unit = {
    data.add(new DataPoint(x, y))
    _regression = None
  }

  def getN: Long = data.data.length

  def predict(x: Double): Double = {
    val r = regression // Force model computation

    val input = normalizationHelper.allocateInputVector

    normalizationHelper.normalizeInputVector(Array(x.toString), input.getData, false)

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

}
