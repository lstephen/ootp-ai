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

case class PredictInput(models: Map[String, String], data: Array[PredictFeatures])
case class PredictFeatures(features: Array[Double])

class RegressionPyModel extends Model with StrictLogging {
  implicit def TrainInputCodecJson =
    casecodec3(TrainInput.apply, TrainInput.unapply)("weight",
                                                     "features",
                                                     "label")

  implicit def PredictFeaturesCodecJson =
    casecodec1(PredictFeatures.apply, PredictFeatures.unapply)("features")

  implicit def PredictInputCodecJson =
    casecodec2(PredictInput.apply, PredictInput.unapply)("models", "data")


  val modelFile =
    File.createTempFile(UUID.randomUUID.toString, ".mdl").getAbsolutePath

  def train(ds: DataSet): Model.Predict = {
    val json = ds
      .map(
        d =>
          TrainInput(d.weight,
                     d.input.toArray(ds.averageForColumn(_)),
                     d.output))
      .asJson

    val regressionPyReport = RegressionPyCli.train(modelFile, json.toString)

    new Model.Predict {
      def apply(in: Seq[Input]): Seq[Double] = {
        if (in.size == 0) return Seq()

        val json =
          PredictInput(Map("model" -> modelFile), in.map(i => PredictFeatures(i.toArray(ds.averageForColumn(_)))).toArray).asJson

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
