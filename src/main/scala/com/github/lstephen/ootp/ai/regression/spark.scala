package com.github.lstephen.ootp.ai.regression

import org.apache.spark.mllib.tree.RandomForest

import com.typesafe.scalalogging.StrictLogging

class RandomForestModel extends Model with StrictLogging {
  private val seed = 42

  def train(ds: DataSet) = {
    val model = RandomForest.trainRegressor(
      ds.toRdd, Map[Int, Int](), 100, "auto", "variance", 5, 32, seed)

    logger.info(s"Model: $model")

    i => model.predict(i.toVector(ds.averageForColumn(_)))
  }
}

