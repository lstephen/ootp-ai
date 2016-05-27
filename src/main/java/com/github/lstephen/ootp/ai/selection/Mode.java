package com.github.lstephen.ootp.ai.selection;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.github.lstephen.ootp.ai.player.Slot;

public enum Mode {
    IDEAL(25) {
        @Override
        public ImmutableMultiset<Slot> getHittingSlots() {
            return REGULAR_SEASON.getHittingSlots();
        }

        @Override
        public ImmutableMultiset<Slot> getPitchingSlots() {
            return REGULAR_SEASON.getPitchingSlots();
        }
    },
    PRESEASON(25) {
        @Override
        public ImmutableMultiset<Slot> getHittingSlots() {
            return REGULAR_SEASON.getHittingSlots();
        }

        @Override
        public ImmutableMultiset<Slot> getPitchingSlots() {
            return REGULAR_SEASON.getPitchingSlots();
        }
    },
    REGULAR_SEASON(25) {
        @Override
        public ImmutableMultiset<Slot> getHittingSlots() {
            return ImmutableMultiset.of(
                Slot.C, Slot.C,
                Slot.SS, Slot.IF, Slot.IF, Slot.IF, Slot.IF,
                Slot.CF, Slot.OF, Slot.OF, Slot.OF,
                Slot.H, Slot.H, Slot.H);
        }

        @Override
        public ImmutableMultiset<Slot> getPitchingSlots() {
            return ImmutableMultiset.of(
                Slot.SP, Slot.SP, Slot.SP, Slot.SP, Slot.SP,
                Slot.MR, Slot.MR, Slot.MR, Slot.MR,
                Slot.P, Slot.P);
        }
    },
    EXPANDED(40) {
        @Override
        public ImmutableMultiset<Slot> getHittingSlots() {
            Multiset<Slot> expanded =
                HashMultiset.create(REGULAR_SEASON.getHittingSlots());

            expanded.addAll(
                ImmutableMultiset.of(
                    Slot.C,
                    Slot.SS, Slot.IF,
                    Slot.CF, Slot.OF,
                    Slot.H));

            return ImmutableMultiset.copyOf(expanded);
        }

        @Override
        public ImmutableMultiset<Slot> getPitchingSlots() {
            Multiset<Slot> expanded =
                HashMultiset.create(REGULAR_SEASON.getPitchingSlots());

            expanded.addAll(
                ImmutableMultiset.of(Slot.SP, Slot.MR, Slot.P));

            return ImmutableMultiset.copyOf(expanded);
        }
    },
    PLAYOFFS(25) {
        @Override
        public ImmutableMultiset<Slot> getHittingSlots() {
            Multiset<Slot> playoffs =
                HashMultiset.create(REGULAR_SEASON.getHittingSlots());

            playoffs.add(Slot.H);

            return ImmutableMultiset.copyOf(playoffs);
        }

        @Override
        public ImmutableMultiset<Slot> getPitchingSlots() {
            Multiset<Slot> playoffs =
                HashMultiset.create(REGULAR_SEASON.getPitchingSlots());

            playoffs.remove(Slot.SP);

            return ImmutableMultiset.copyOf(playoffs);
        }
    };

    private final Integer rosterSize;

    Mode(Integer rosterSize) {
        this.rosterSize = rosterSize;
    }

    public Integer getMajorLeagueRosterLimit() {
        return rosterSize;
    }

    public abstract ImmutableMultiset<Slot> getHittingSlots();

    public abstract ImmutableMultiset<Slot> getPitchingSlots();

    public ImmutableMultiset<Slot> getAllSlots() {
        return ImmutableMultiset.copyOf(
            Iterables.concat(getHittingSlots(), getPitchingSlots()));
    }
}
