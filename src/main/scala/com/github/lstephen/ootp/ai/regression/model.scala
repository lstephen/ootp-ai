package com.github.lstephen.ootp.ai.regression

object Model {
  type Predict = Seq[Input] => Seq[Double]
}

trait Model {
  import Model._

  def train(ds: DataSet): Predict
}
