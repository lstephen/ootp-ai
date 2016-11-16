package com.github.lstephen.ootp.ai.rating

import scala.collection.immutable.TreeMap

import java.util.Locale

import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.annotation.JsonSubTypes.Type

import com.google.common.base.CharMatcher

import scalaz.CaseInsensitive

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(Array(
  new Type(classOf[OneToFive]),
  new Type(classOf[OneToTen]),
  new Type(classOf[OneToOneHundred]),
  new Type(classOf[OneToTwenty]),
  new Type(value = classOf[Potential], name = "PotentialRating$RatingScale"),
  new Type(classOf[TwoToEight]),
  new Type(classOf[ZeroToTen])))
trait Scale[T] {
  def parse(s: String): Rating[T, _ <: Scale[T]]
  def normalize(v: T): Rating[Integer, OneToOneHundred]
}

@JsonIgnoreProperties(Array("scale"))
abstract class IntegerScale(scale: Integer => Integer) extends Scale[Integer] {
  override def normalize(v: Integer): Rating[Integer, OneToOneHundred] =
    OneToOneHundred.valueOf(scale(v))

  override def parse(s: String): Rating[Integer, IntegerScale] =
    Rating(Integer.parseInt(s), this)
}

object OneToOneHundred {
  def valueOf(v: Integer): Rating[Integer, OneToOneHundred] = Rating(v, OneToOneHundred())
}

case class OneToFive() extends IntegerScale(v => v * 20 - 10)
case class OneToOneHundred() extends IntegerScale(v => v)
case class OneToTen() extends IntegerScale(v => v * 10 - 5)
case class OneToTwenty() extends IntegerScale(v => v * 5 - 2)
case class TwoToEight() extends IntegerScale(v => (v * 2 + (v - 5)) * 5)
case class ZeroToTen() extends IntegerScale(v => if (v == 0) 1 else v * 10)

case class AToE() extends StringScale(
  Map("A" -> 90, "B" -> 70, "C" -> 50, "D" -> 30, "E" -> 10))

case class Potential() extends StringScale(
  Map(
    "Brilliant" -> 90,
    "Good" -> 70,
    "Average" -> 50,
    "Fair" -> 30,
    "Poor" -> 10))

@JsonIgnoreProperties(Array("ratings"))
class StringScale(rs: Map[String, Integer]) extends Scale[String] {

  val ratings = rs map { case (k, v) => (CaseInsensitive(k), v) }

  def contains(k: String): Boolean = ratings contains CaseInsensitive(k)

  def get(k: String): Integer =
    ratings.getOrElse(
      CaseInsensitive(k),
      throw new IllegalStateException(s"Unkown rating: $k"))

  override def parse(s: String): Rating[String, StringScale] = {
    require(contains(s), s"Unknown rating: $s")

    Rating(s, this)
  }

  override def normalize(k: String): Rating[Integer, OneToOneHundred] =
    OneToOneHundred.valueOf(get(k))
}

