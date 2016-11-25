package com.github.lstephen.ootp.ai.regression

import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.regression.RandomForestRegressor

import org.apache.spark.ml.linalg.SQLDataTypes._

import org.apache.spark.sql._
import org.apache.spark.sql.types._

import com.typesafe.scalalogging.StrictLogging

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

    is =>
      {
        import Spark.session.implicits._

        val df =
          is.map(i => Tuple1(i.toVector(ds averageForColumn _))).toDF("input")

        model.transform(df).collect.map(r => r.getAs[Double]("prediction"))
      }
  }
}
