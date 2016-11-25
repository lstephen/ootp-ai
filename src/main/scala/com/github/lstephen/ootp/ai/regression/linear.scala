package com.github.lstephen.ootp.ai.regression

import org.apache.commons.math3.stat.regression.SimpleRegression

import com.typesafe.scalalogging.StrictLogging

class LinearRegressionModel extends Model with StrictLogging {
  class IndexedRegression(regression: SimpleRegression,
                          idx: Integer,
                          avg: Double) {
    val getRSquare = regression.getRSquare

    def predict(x: Double): Double = regression.predict(x)
    def predict(i: Input): Double = predict(i.get(idx).getOrElse(avg))
  }

  def train(ds: DataSet) = {

    val regressions = (0 to (ds.features - 1)).map { idx =>
      ds.toList.foldLeft(new SimpleRegression(true)) { (r, d) =>
        d.input.get(idx) match {
          case Some(n) => r.addData(n, d.output)
          case None => ()
        }
        r
      }
    }.zipWithIndex.map {
      case (r, i) => new IndexedRegression(r, i, ds.averageForColumn(i))
    }

    logger.info(s"R2s: ${regressions.map(_.getRSquare)}")

    val r = regressions.maxBy(_.getRSquare)

    is =>
      is.map(r.predict(_))
  }
}
