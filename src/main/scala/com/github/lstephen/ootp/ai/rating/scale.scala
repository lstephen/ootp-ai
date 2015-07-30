package com.github.lstephen.ootp.ai.rating

import java.util.Locale

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo

import com.google.common.base.CharMatcher

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(Array(
    new Type(classOf[ZeroToTen]),
    new Type(classOf[OneToTen]),
    new Type(value = classOf[Potential], name = "PotentialRating$RatingScale"),
    new Type(classOf[OneToOneHundred]),
    new Type(classOf[OneToTwenty]),
    new Type(classOf[TwoToEight])
))
trait Scale[T] {
  def parse(s: String): Rating[T, _ <: Scale[T]]
  def normalize(v: T): Rating[Integer, OneToOneHundred]
}

abstract class IntegerScale(scale: Integer => Integer) extends Scale[Integer] {
  override def normalize(v: Integer): Rating[Integer, OneToOneHundred] =
    OneToOneHundred.valueOf(scale(v))

  override def parse(s: String): Rating[Integer, IntegerScale] =
    Rating(Integer.parseInt(s), this)
}

object OneToOneHundred {
  def valueOf(v: Integer): Rating[Integer, OneToOneHundred] =
    Rating(v, OneToOneHundred())
}

case class OneToOneHundred() extends IntegerScale(v => v)
case class OneToTen() extends IntegerScale(v => v * 10 - 5)
case class OneToTwenty() extends IntegerScale(v => v * 5 - 2)
case class TwoToEight() extends IntegerScale(v => (v * 2 + (v - 5)) * 5)
case class ZeroToTen() extends IntegerScale(v => if (v == 0) 1 else v * 10)

case class Potential() extends Scale[String] {
  private val ratings = Map(
    "BRILLIANT" -> 90,
    "GOOD"      -> 70,
    "AVERAGE"   -> 50,
    "FAIR"      -> 30,
    "POOR"      -> 10
  )

  override def parse(s: String): Rating[String, Potential] = {
    require(
      ratings.keySet.contains(sanitize(s)),
      s"Unknown rating: $s")

    Rating(s, this)
  }

  override def normalize(v: String): Rating[Integer, OneToOneHundred] =
    OneToOneHundred.valueOf(normalizeToInt(v))

  private def normalizeToInt(v: String): Integer =
    ratings.getOrElse(sanitize(v), throw new IllegalStateException(s"Unknown rating: $v"))

  private def sanitize(v: String): String =
    CharMatcher.WHITESPACE.trimFrom(v).toUpperCase(Locale.ENGLISH)
}

