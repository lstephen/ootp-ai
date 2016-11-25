package com.github.lstephen.ootp.ai.regression

import com.typesafe.scalalogging.StrictLogging

import org.apache.spark.ml.linalg.{Vector, DenseVector}
import org.apache.spark.ml.linalg.SQLDataTypes._

import org.apache.spark.sql._
import org.apache.spark.sql.types._

class Input(private val is: List[Option[Double]]) {
  def :+(rhs: Double): Input = this :+ Some(rhs)
  def :+(rhs: Option[Double]): Input = new Input(is :+ rhs)
  def ++(rhs: Input): Input = new Input(is ++ rhs.is)

  val length = is.length

  def get(idx: Integer): Option[Double] = is(idx)

  def toArray(f: Int => Double): Array[Double] =
    is.zipWithIndex.map {
      case (d, idx) => d getOrElse f(idx)
    }.toArray

  def toVector(f: Int => Double): Vector = new DenseVector(toArray(f))
}

object Input {
  def apply(ds: Integer*): Input = apply(ds.toList.map(_.doubleValue))
  def apply(ds: List[Double]): Input = new Input(ds.map(Some(_)))
}

class DataPoint(val input: Input, val output: Double) {
  def features = input.length

  def toTuple(f: Int => Double): (Vector, Double) = (input.toVector(f), output)
}

class DataSet(ds: List[DataPoint]) extends StrictLogging {
  def :+(rhs: DataPoint): DataSet = {
    if (!ds.isEmpty && rhs.input.length != features) {
      throw new IllegalArgumentException
    }

    new DataSet(ds :+ rhs)
  }

  lazy val averages: List[Double] = {
    logger.info("Calculating averages...")

    (0 to (features - 1)).map { idx =>
      val vs = ds.map(_.input.get(idx)).flatten

      if (vs.isEmpty) 50.0 else (vs.sum / vs.length)
    }.toList
  }

  def averageForColumn(i: Integer): Double = averages(i)

  def map[T](f: DataPoint => T): List[T] = ds.map(f)

  def toList = ds

  val length = ds.length

  def features = ds.head.input.length

  def toDataFrame: DataFrame = {
    import Spark.session.implicits._

    ds.map { d =>
      (d.input.toVector(averageForColumn(_)), d.output)
    }.toDF("input", "output")
  }
}

object DataSet {
  def apply(): DataSet = new DataSet(List())
}
