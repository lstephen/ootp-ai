package com.github.lstephen.ootp.ai.rating

import java.util.Locale

import com.google.common.base.CharMatcher

abstract class IntegerScale(scale: Integer => Integer) extends Scale[Integer] {
  override def normalize(v: Integer): Rating[Integer, OneToOneHundred] =
    OneToOneHundred.valueOf(scale(v))

  override def parse(s: String): Rating[Integer, IntegerScale] =
    Rating.create(Integer.parseInt(s), this)
}

object OneToOneHundred {
  def valueOf(v: Integer): Rating[Integer, OneToOneHundred] =
    Rating.create(v, OneToOneHundred())
}

case class OneToOneHundred() extends IntegerScale(v => v)
case class OneToTen() extends IntegerScale(v => v * 10 - 5)
case class OneToTwenty() extends IntegerScale(v => v * 5 - 2)
case class TwoToEight() extends IntegerScale(v => (v * 2 + (v - 5)) * 5)
case class ZeroToTen() extends IntegerScale(v => if (v == 0) 1 else v * 10)

/**
 * Legacy naming. In areas such as JSON decoding with Jackson the
 * class name is important.
 */
object PotentialRating {

  def scale = RatingScale()

  case class RatingScale() extends Scale[String] {
    private val ratings = Map(
      "BRILLIANT" -> 90,
      "GOOD"      -> 70,
      "AVERAGE"   -> 50,
      "FAIR"      -> 30,
      "POOR"      -> 10
    )

    override def parse(s: String): Rating[String, RatingScale] = {
      require(
        ratings.keySet.contains(sanitize(s)),
        s"Unknown rating: $s")

      Rating.create(s, this)
    }

    override def normalize(v: String): Rating[Integer, OneToOneHundred] =
      OneToOneHundred.valueOf(normalizeToInt(v))

    private def normalizeToInt(v: String): Integer =
      ratings.getOrElse(sanitize(v), throw new IllegalStateException(s"Unknown rating: $v"))

    private def sanitize(v: String): String =
      CharMatcher.WHITESPACE.trimFrom(v).toUpperCase(Locale.ENGLISH)
  }
}
