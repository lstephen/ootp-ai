package com.github.lstephen.ootp.ai.regression

object Model {
  type Predict = Input => Double
}

trait Model {
  import Model._

  def train(ds: DataSet): Predict
}


class CompositeModel(ms: Seq[Model]) extends Model {

  def train(ds: DataSet) = {
    val trained = ms.map(_.train(ds))

    i => trained.map(_(i)).sum / ms.length
  }

}

