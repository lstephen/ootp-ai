package com.github.lstephen.ootp.ai.player.ratings;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lstephen
 */
public enum Position {
    PITCHER("P"),
    CATCHER("C"),
    FIRST_BASE("1B"),
    SECOND_BASE("2B"),
    THIRD_BASE("3B"),
    SHORTSTOP("SS"),
    LEFT_FIELD("LF"),
    CENTER_FIELD("CF"),
    RIGHT_FIELD("RF"),
    DESIGNATED_HITTER("DH");

    private final String abbreviation;

    Position(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public static List<Position> hitting() {
      return Arrays.asList(
          CATCHER,
          FIRST_BASE, SECOND_BASE, THIRD_BASE, SHORTSTOP,
          LEFT_FIELD, CENTER_FIELD, RIGHT_FIELD,
          DESIGNATED_HITTER);
    }
}
