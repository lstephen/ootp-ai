package com.ljs.ootp.ai.stats;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public final class Woba {

    private static final Woba INSTANCE = new Woba();

    public static enum Event { SINGLE, DOUBLE, TRIPLE, HOME_RUN, WALK }

    private EnumMap<Event, Double> constants = Maps.newEnumMap(Event.class);

    private Woba() { }


    public Double calculate(BattingStats stats) {
        return (constants.get(Event.WALK) * stats.getWalks()
            + constants.get(Event.SINGLE) * stats.getSingles()
            + constants.get(Event.DOUBLE) * stats.getDoubles()
            + constants.get(Event.TRIPLE) * stats.getTriples()
            + constants.get(Event.HOME_RUN) * stats.getHomeRuns())
            / stats.getPlateAppearances();
    }

    public Double calculate(PitchingStats stats) {
        return (constants.get(Event.WALK) * stats.getWalks()
            + constants.get(Event.SINGLE) * stats.getSingles()
            + constants.get(Event.DOUBLE) * stats.getDoubles()
            + constants.get(Event.TRIPLE) * stats.getTriples()
            + constants.get(Event.HOME_RUN) * stats.getHomeRuns())
            / stats.getPlateAppearances();
    }

    public static Woba get() {
        return INSTANCE;
    }

    public static void setConstants(Map<Event, Double> constants) {
        get().constants = Maps.newEnumMap(constants);
    }


}
