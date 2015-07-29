package com.github.lstephen.ootp.ai.rating

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

