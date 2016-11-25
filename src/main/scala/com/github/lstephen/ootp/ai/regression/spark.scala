package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.io.Printable

import com.typesafe.scalalogging.StrictLogging

import java.io.PrintWriter

import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.regression.RandomForestRegressor

import org.apache.spark.ml.linalg.SQLDataTypes._

import org.apache.spark.sql._
import org.apache.spark.sql.types._


class RandomForestModel extends Model with StrictLogging {
  private val seed = 42

  def train(ds: DataSet) = {
    val regressor = new RandomForestRegressor()
      .setLabelCol("output")
      .setFeaturesCol("input")
      .setNumTrees(100)
      .setMaxDepth(5)
      .setMaxBins(32)
      .setMinInstancesPerNode(ds.length / 100)
      .setSeed(seed)

    val model = regressor.fit(ds.toDataFrame)

    logger.info(s"Model: $model")

    new Model.Predict {
      def apply(is: Seq[Input]) = {
        import Spark.session.implicits._

        val df =
          is.map(i => Tuple1(i.toVector(ds averageForColumn _))).toDF("input")

        model.transform(df).collect.map(r => r.getAs[Double]("prediction"))
      }

      def report(l: String) = new Printable {
        def print(w: PrintWriter): Unit = {
          w.println(s"-- ${l}")
          w.println(s"Feature Importances: ${model.featureImportances.toArray.map(n => f"$n%.3f").mkString(", ")}")
          w.println(s"Parameters: ${model.extractParamMap}")
        }
      }
    }
  }

}
