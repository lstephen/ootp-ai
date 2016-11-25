package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.io.Printable

import com.typesafe.scalalogging.StrictLogging

import java.io.PrintWriter

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.regression.{
  RandomForestRegressor,
  RandomForestRegressionModel
}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}

import org.apache.spark.ml.linalg.SQLDataTypes._

import org.apache.spark.sql._
import org.apache.spark.sql.types._

import scala.reflect.{classTag, ClassTag}

class RandomForestModel extends Model with StrictLogging {
  private val seed = 42

  def train(ds: DataSet) = {
    val regressor = new RandomForestRegressor()
      .setLabelCol("output")
      .setFeaturesCol("input")
      .setNumTrees(100)
      .setSeed(seed)

    val pipeline = new Pipeline().setStages(Array(regressor))

    val paramGrid = new ParamGridBuilder()
      .addGrid(regressor.maxDepth, Array(1, 2, 3, 5, 8))
      .addGrid(regressor.maxBins, Array(20, 30))
      .addGrid(regressor.numTrees, Array(10, 30, 100))
      .build()

    val cv = new CrossValidator()
      .setEstimator(pipeline)
      .setEvaluator(
        new RegressionEvaluator().setLabelCol("output").setMetricName("mse"))
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(3)
      .setSeed(42)

    val model = cv.fit(ds.toDataFrame)

    logger.info(s"Model: $model")

    new Model.Predict {
      def apply(is: Seq[Input]) = {
        import Spark.session.implicits._

        val df =
          is.map(i => Tuple1(i.toVector(ds averageForColumn _))).toDF("input")

        model.transform(df).collect.map(r => r.getAs[Double]("prediction"))
      }

      def getPipeline = model.bestModel.asInstanceOf[PipelineModel]

      def getStage[T: ClassTag]: T = {
        getPipeline.stages
          .find(classTag[T].runtimeClass.isInstance(_))
          .map(_.asInstanceOf[T])
          .getOrElse(
            throw new IllegalStateException("Could not find pipeline stage"))
      }

      def report(l: String) = new Printable {
        def print(w: PrintWriter): Unit = {
          w.println(s"-- ${l}")
          w.println(
            s"Feature Importances: ${getStage[RandomForestRegressionModel].featureImportances.toArray
              .map(n => f"$n%.3f")
              .mkString(", ")}")
          w.println(s"${getPipeline} [")
          for (s <- getPipeline.stages) {
            w.println(s"Model: ${s}")
            w.println(s"Parameters: ${s.extractParamMap}")
          }
          w.println("]")
        }
      }
    }
  }

}
