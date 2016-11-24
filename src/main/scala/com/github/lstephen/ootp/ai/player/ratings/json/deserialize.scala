package com.github.lstephen.ootp.ai.player.ratings.json

import com.github.lstephen.ootp.ai.rating.Rating
import com.github.lstephen.ootp.ai.site.SiteHolder

import scala.beans.BeanProperty

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{
  DeserializationContext,
  JsonDeserializer
}

class BuntForHitDeserializer extends JsonDeserializer[Rating[_, _]] {
  override def deserialize(jp: JsonParser,
                           ctx: DeserializationContext): Rating[_, _] =
    SiteHolder.get.getBuntScale.parse(jp.getText)
}

class StealingDeserializer extends JsonDeserializer[Rating[_, _]] {
  override def deserialize(jp: JsonParser,
                           ctx: DeserializationContext): Rating[_, _] =
    SiteHolder.get.getRunningScale.parse(jp.getText)
}
