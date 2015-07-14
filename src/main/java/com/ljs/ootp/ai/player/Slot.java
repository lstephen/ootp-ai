package com.ljs.ootp.ai.player;

import com.ljs.ootp.ai.player.ratings.DefensiveRatings;
import com.ljs.ootp.ai.player.ratings.Position;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;

import java.util.List;

public enum Slot {

    C, SS, IF, CF, OF, H, SP, MR, P;

    public static final Multiset<Slot> MAJOR_LEAGUE_PITCHING_SLOTS = ImmutableMultiset.of(
        Slot.SP, Slot.SP, Slot.SP, Slot.SP, Slot.SP,
        Slot.MR, Slot.MR, Slot.MR, Slot.MR,
        Slot.P, Slot.P);

    public static int countPlayersWithPrimary(Iterable<Player> ps, Slot slot) {

        int count = 0;
        for (Player p : ps) {
            if (getPrimarySlot(p) == slot) {
                count++;
            }
        }

        return count;
    }

    public static Slot getPrimarySlot(Player p) {
        return p.getSlots().get(0);
    }

    public static ImmutableList<Slot> getPlayerSlots(Player p) {
        return p.isHitter() ? getBatterSlots(p) : getPitcherSlots(p);
    }

    private static ImmutableList<Slot> getPitcherSlots(Player p) {
        return p.getPitchingRatings().getVsLeft().getEndurance() <= 5 ? ImmutableList.of(MR, P) : ImmutableList.of(SP, P);
    }

    private static ImmutableList<Slot> getBatterSlots(Player p)
    {
        List<Slot> result = Lists.newArrayList();

        final DefensiveRatings def = p.getDefensiveRatings();

        List<Position> specialtyPositions = ImmutableList.copyOf(
            Iterables.filter(
                Ordering
                    .natural()
                    .reverse()
                    .onResultOf(def::getPositionScore)
                    .sortedCopy(ImmutableSet.of(Position.CATCHER, Position.SHORTSTOP, Position.CENTER_FIELD)),
                pl -> def.getPositionScore(pl).doubleValue() > 0.0D ));

        result.addAll(
            Lists.transform(
                specialtyPositions,
                new Function<Position, Slot>() {

                    public Slot apply(Position p) {
                        Preconditions.checkNotNull(p);

                        switch(p) {
                            case CATCHER:
                                return Slot.C;
                            case SHORTSTOP:
                                return Slot.SS;
                            case CENTER_FIELD:
                                return Slot.CF;
                        }
                        throw new IllegalStateException();
                    }}));

        double ifScore = ((Double)Ordering.natural().max(def.getPositionScore(Position.SECOND_BASE), def.getPositionScore(Position.THIRD_BASE), def.getPositionScore(Position.SHORTSTOP), new Double[0])).doubleValue();
        double ofScore = ((Double)Ordering.natural().max(def.getPositionScore(Position.LEFT_FIELD), def.getPositionScore(Position.CENTER_FIELD), def.getPositionScore(Position.RIGHT_FIELD), new Double[0])).doubleValue();

        if(ifScore > 0.0 && ofScore > 0.0) {
            if(ofScore > ifScore) {
                result.add(OF);
                result.add(IF);
            } else {
                result.add(IF);
                result.add(OF);
            }
        } else {
            if(ifScore > 0.0) {
                result.add(IF);
            } else if(ofScore > 0.0) {
                result.add(OF);
            }
        }
        result.add(H);
        return ImmutableList.copyOf(result);
    }

}
