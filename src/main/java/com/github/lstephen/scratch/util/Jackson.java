package com.github.lstephen.scratch.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.lstephen.ootp.ai.player.ratings.json.BattingPotentialSerializer;
import com.github.lstephen.ootp.ai.site.Site;

/** @author lstephen */
public final class Jackson {

  private Jackson() {}

  public static ObjectMapper getMapper(Site site) {
    ObjectMapper mapper = new ObjectMapper();

    mapper.registerModule(new GuavaModule());

    mapper.setVisibilityChecker(
        mapper
            .getSerializationConfig()
            .getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    InjectableValues ivs = new InjectableValues.Std().addValue(Site.class, site);

    mapper.setInjectableValues(ivs);

    BattingPotentialSerializer.setSite(site);

    return mapper;
  }
}
