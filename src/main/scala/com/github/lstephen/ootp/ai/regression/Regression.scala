package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.site.SiteHolder

import org.apache.commons.math3.stat.regression.SimpleRegression

import org.sameersingh.scalaplot.XYPlotStyle
import org.sameersingh.scalaplot.Implicits._

import scala.collection.mutable.ListBuffer

import java.io.File


class Regression(label: String, category: String) {

  val r = new SimpleRegression

  val data = ListBuffer.empty[(Double, Double)]

  def addData(x: Double, y: Double): Unit = {
    r.addData(x, y)
    data += ((x, y))
  }

  def getN: Long = r.getN

  def predict(x: Double): Double = r predict x

  def format: String = {
    val dir = s"${sys.env("OOTPAI_DATA")}/charts/${SiteHolder.get.getName}/${category}/"

    new File(dir).mkdirs

    output(PNG(dir, label), xyChart(List(XY(data, style=XYPlotStyle.Dots))))

    f"$label%15s | ${r.getRSquare}%.3f"
  }

}
