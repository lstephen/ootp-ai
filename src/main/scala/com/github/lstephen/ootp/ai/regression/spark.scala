package com.github.lstephen.ootp.ai.regression

import org.apache.spark.mllib.tree.RandomForest

class RandomForestModel extends Model {

  def train(ds: DataSet) = {
    val model = RandomForest.trainRegressor(
      ds.toRdd, Map[Int, Int](), 100, "auto", "variance", 5, 32)

    i => model.predict(i.toVector(ds.averageForColumn(_)))
  }

}

