package com.github.lstephen.ootp.ai.rating

class OneToTwenty extends IntegerScale {
  override def scale(v : Integer) : Integer = v * 5 - 2
}

