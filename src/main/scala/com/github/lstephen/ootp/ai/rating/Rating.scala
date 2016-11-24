package com.github.lstephen.ootp.ai.rating

import com.fasterxml.jackson.annotation.JsonValue;

case class Rating[T, S <: Scale[T]](value: T, scale: S) {

  @JsonValue
  def get: T = value

  def normalize: Rating[Integer, OneToOneHundred] = scale.normalize(value)

}
