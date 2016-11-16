package com.github.lstephen.ootp.ai.regression

import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.configuration.Algo
import org.apache.spark.mllib.tree.configuration.Strategy
import org.apache.spark.mllib.tree.impurity.Variance

import com.typesafe.scalalogging.StrictLogging

class RandomForestModel extends Model with StrictLogging {
  private val seed = 42

  def train(ds: DataSet) = {
    val strategy = new Strategy(
      algo = Algo.Regression,
      impurity = Variance,
      maxDepth = 5,
      minInstancesPerNode = ds.length / 200)

    val model = RandomForest.trainRegressor(ds.toRdd, strategy, 100, "auto", seed)

    //val model = RandomForest.trainRegressor(
    //  ds.toRdd, Map[Int, Int](), 100, "auto", "variance", 5, 32, seed)

    logger.info(s"Model: $model")

    i => model.predict(i.toVector(ds.averageForColumn(_)))
  }
}

