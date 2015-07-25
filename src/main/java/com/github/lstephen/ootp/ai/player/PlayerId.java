package com.github.lstephen.ootp.ai.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ljs.scratch.util.Wrapped;

/**
 *
 * @author lstephen
 */
public class PlayerId extends Wrapped<String> {

    @JsonCreator
    public PlayerId(String id) {
        super(id);
    }

    /**
     * Override so that it can be useful as a key in maps after serialization
     * to JSON.
     * 
     * @return 
     */
    @JsonValue
    @Override
    public String toString() {
        return unwrap();
    }

}
