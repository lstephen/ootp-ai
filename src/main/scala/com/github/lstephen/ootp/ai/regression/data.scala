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

  def updated(idx: Integer, f: Double => Double) =
    new Input(is.updated(idx, get(idx).map(f)))

  val length = is.length

  def get(idx: Integer): Option[Double] = is(idx)

  def toArray(f: Int => Double): Array[Double] =
    is.zipWithIndex.map {
      case (d, idx) => d getOrElse f(idx)
    }.toArray

  def toVector(f: Int => Double): Vector = new DenseVector(toArray(f))

  def toOptionList: List[Option[Double]] = is
}

object Input {
  //def apply(ds: Integer*): Input = apply(ds.toList.map(_.doubleValue))
  def apply[N <: Number](ds: Option[N]*): Input = new Input(ds.map(_.map(_.doubleValue)).toList)
  def apply(ds: List[Double]): Input = new Input(ds.map(Some(_)))
}

class DataPoint(val input: Input, val output: Double, val weight: Int) {
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

    (0 until features).map { idx =>
      val entries = ds.map(d => d.input.get(idx).map(v => (v * d.weight, d.weight))).flatten

      val sum = entries.map(_._1).sum
      val weights = entries.map(_._2).sum

      if (weights == 0) 0 else sum / weights
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
