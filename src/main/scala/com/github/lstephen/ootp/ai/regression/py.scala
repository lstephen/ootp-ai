package com.github.lstephen.ootp.ai.regression

import argonaut._
import Argonaut._
import com.github.lstephen.ootp.ai.io.Printable
import com.typesafe.scalalogging.StrictLogging
import java.io.ByteArrayInputStream
import java.io.File
import java.io.PrintWriter
import java.util.UUID

case class TrainInput(weight: Integer, features: Array[Double], label: Double)

object TrainInput {
  implicit def TrainInputCodecJson =
    casecodec3(TrainInput.apply, TrainInput.unapply)("weight",
                                                     "features",
                                                     "label")
}

case class PredictFeatures(features: Array[Double])

object PredictFeatures {
  implicit def PredictFeaturesCodecJson =
    casecodec1(PredictFeatures.apply, PredictFeatures.unapply)("features")
}

case class PredictInput(models: Map[String, String],
                        data: Array[PredictFeatures])

object PredictInput {
  implicit def PredictInputCodecJson =
    casecodec2(PredictInput.apply, PredictInput.unapply)("models", "data")
}

class RegressionPyModel extends Model with StrictLogging {
  val modelFile =
    File.createTempFile(UUID.randomUUID.toString, ".mdl").getAbsolutePath

  def train(ds: DataSet): RegressionPyModel.Predict = {
    val json = ds
      .map(
        d =>
          TrainInput(d.weight,
                     d.input.toArray(ds.averageForColumn(_)),
                     d.output))
      .asJson

    val regressionPyReport = RegressionPyCli.train(modelFile, json.toString)

    new RegressionPyModel.Predict {
      val modelFile = RegressionPyModel.this.modelFile
      val dataSet = ds

      def apply(in: Seq[Input]): Seq[Double] = {
        if (in.size == 0) return Seq()

        val json =
          PredictInput(
            Map("model" -> modelFile),
            in.map(i => PredictFeatures(i.toArray(ds.averageForColumn(_))))
              .toArray).asJson

        val results = RegressionPyCli.predict(json.toString)

        Parse
          .decodeOption[Map[String, Array[Double]]](results)
          .flatMap(_.get("model"))
          .getOrElse(throw new IllegalStateException)
      }

      override def report(l: String) = new Printable {
        def print(w: PrintWriter) {
          w.println(s"-- ${l}")
          w.println(regressionPyReport)
        }
      }
    }
  }
}

object RegressionPyModel {

  trait Predict extends Model.Predict {
    def modelFile: String
    def dataSet: DataSet
  }

  def predict(models: Map[String, Predict],
              in: Seq[Input]): Map[String, Seq[Double]] = {
    if (in.size == 0) return models.mapValues(_ => Seq())

    val json =
      PredictInput(
        models.mapValues(_.modelFile),
        in.map(
            i =>
              PredictFeatures(
                i.toArray(models.head._2.dataSet.averageForColumn(_))))
          .toArray).asJson

    val results = RegressionPyCli.predict(json.toString)

    Parse
      .decodeOption[Map[String, Array[Double]]](results)
      .getOrElse(throw new IllegalStateException)
      .mapValues(_.toSeq)
  }
}

object RegressionPyCli extends StrictLogging {

  def train(modelFile: String, in: String): String = {
    import scala.sys.process._

    s"python target/regression.py train ${modelFile}" #< new ByteArrayInputStream(
      in.getBytes("UTF-8")) !! ProcessLogger(logger.info(_))
  }

  def predict(in: String): String = {
    import scala.sys.process._

    s"python target/regression.py predict" #< new ByteArrayInputStream(
      in.getBytes("UTF-8")) !! ProcessLogger(logger.info(_))
  }

}
