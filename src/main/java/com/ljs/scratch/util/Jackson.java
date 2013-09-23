package com.ljs.scratch.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 * @author lstephen
 */
public final class Jackson {

    private Jackson() { }

    public static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setVisibilityChecker(
            mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enableDefaultTyping();

        return mapper;
    }

}
