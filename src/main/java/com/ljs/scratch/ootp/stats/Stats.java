package com.ljs.scratch.ootp.stats;

import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 *
 * @author lstephen
 */
@JsonSubTypes({@JsonSubTypes.Type(BattingStats.class), @JsonSubTypes.Type(PitchingStats.class)})
public interface Stats<Self extends Stats<Self>> {

    Self multiply(double factor);

    Self add(Self rhs);

}
