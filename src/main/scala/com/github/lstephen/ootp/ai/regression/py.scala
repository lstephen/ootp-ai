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
case class PredictInput(features: Array[Double])

class RegressionPyModel extends Model with StrictLogging {
  implicit def TrainInputCodecJson =
    casecodec3(TrainInput.apply, TrainInput.unapply)("weight",
                                                     "features",
                                                     "label")

  implicit def PredictInputCodecJson =
    casecodec1(PredictInput.apply, PredictInput.unapply)("features")

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
          in.map(i => PredictInput(i.toArray(ds.averageForColumn(_)))).asJson

        val results = RegressionPyCli.predict(modelFile, json.toString)

        Parse
          .decodeOption[Array[Double]](results)
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

  def train(f: String, in: String): String = run("train", f, in)
  def predict(f: String, in: String): String = run("predict", f, in)

  def run(cmd: String, modelFile: String, in: String): String = {
    import scala.sys.process._

    s"python target/regression.py ${cmd} ${modelFile}" #< new ByteArrayInputStream(
      in.getBytes("UTF-8")) !! ProcessLogger(logger.info(_))
  }

}
