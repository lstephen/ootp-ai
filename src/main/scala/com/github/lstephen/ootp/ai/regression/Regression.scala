package com.github.lstephen.ootp.ai.regression

import org.apache.commons.math3.stat.regression.SimpleRegression


class Regression {

  val r = new SimpleRegression

  def addData(x: Double, y: Double): Unit = r.addData(x, y)

  def getN: Long = r.getN

  def predict(x: Double): Double = r predict x

  def format: String = s"${r.getRSquare}.3f"

}
