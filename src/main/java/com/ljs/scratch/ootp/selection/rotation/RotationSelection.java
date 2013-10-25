// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RotationSelection.java
package com.ljs.scratch.ootp.selection.rotation;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.selection.Mode;
import com.ljs.scratch.ootp.selection.Selections;
import com.ljs.scratch.ootp.selection.Slot;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.TeamStats;
import java.util.Iterator;
import java.util.List;

// Referenced classes of package com.ljs.scratch.ootp.selection.rotation:
//            Rotation
public final class RotationSelection {

    private static final class RotationDefinition {

        public Integer getRotationSize() {
            return rotation;
        }

        public Integer getRelieversSize() {
            return relievers;
        }

        public static RotationDefinition regularSeason() {
            return new RotationDefinition(REGULAR_SEASON_ROTATION, RELIEVERS);
        }

        public static RotationDefinition playoffs() {
            return new RotationDefinition(PLAYOFFS_ROTATION, RELIEVERS);
        }
        private static final Integer REGULAR_SEASON_ROTATION = Integer
            .valueOf(5);

        private static final Integer PLAYOFFS_ROTATION = Integer.valueOf(4);

        private static final Integer RELIEVERS = Integer.valueOf(4);

        private final Integer rotation;

        private final Integer relievers;

        private RotationDefinition(Integer rotation, Integer relievers) {
            this.rotation = rotation;
            this.relievers = relievers;
        }
    }

    private RotationSelection(TeamStats predictions, PitcherOverall method,
        RotationDefinition definition) {
        this.predictions = predictions;
        this.method = method;
        this.definition = definition;
    }

    public Rotation select(Iterable available) {
        Rotation rotation = new Rotation();
        List starters = Lists.newArrayList();
        Iterator i$ = byOverall().compound(method.byWeightedRating()).compound(
            Player.byAge()).sortedCopy(Selections.onlyPitchers(available))
            .iterator();
        do {
            if (!i$.hasNext()) {
                break;
            }
            Player p = (Player) i$.next();
            if (Slot.getPlayerSlots(p).contains(Slot.SP) && starters.size() <
                definition.getRotationSize().intValue()) {
                starters.add(p);
            }
        } while (true);
        rotation.setStarters(starters);
        List relievers = byOverall().compound(method.byWeightedRating())
            .compound(Player.byAge()).sortedCopy(Selections.onlyPitchers(Sets
            .difference(ImmutableSet.copyOf(available), ImmutableSet.copyOf(
            starters)))).subList(0, definition.getRelieversSize().intValue());
        rotation.setMiddleRelievers(relievers);
        return rotation;
    }

    public Ordering byOverall() {
        return Ordering.natural().onResultOf(new Function<Player, Double>() {
            public Double apply(Player p) {
                return method.get(predictions, p);
            }
        });
    }

    public static RotationSelection regularSeason(TeamStats pitching,
        PitcherOverall method) {
        return new RotationSelection(pitching, method, RotationDefinition
            .regularSeason());
    }

    public static RotationSelection playoffs(TeamStats pitching,
        PitcherOverall method) {
        return new RotationSelection(pitching, method, RotationDefinition
            .playoffs());
    }

    public static RotationSelection forMode(Mode mode, TeamStats<PitchingStats> pitching, PitcherOverall method) {
        switch (mode) {
            case PRESEASON:
            case REGULAR_SEASON:
            case EXPANDED:
                return regularSeason(pitching, method);
            case PLAYOFFS:
                return playoffs(pitching, method);
            default:
                throw new IllegalStateException();
        }


    }
    private final TeamStats predictions;

    private final PitcherOverall method;

    private final RotationDefinition definition;

}
