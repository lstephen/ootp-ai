package com.ljs.scratch.ootp.selection;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

public enum Mode {
    PRESEASON {
        @Override
        public ImmutableMultiset<Slot> getHittingSlots() {
            return REGULAR_SEASON.getHittingSlots();
        }

        @Override
        public ImmutableMultiset<Slot> getPitchingSlots() {
            return REGULAR_SEASON.getPitchingSlots();
        }
    },
    REGULAR_SEASON {
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
    EXPANDED {
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
    PLAYOFFS {
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

    public abstract ImmutableMultiset<Slot> getHittingSlots();

    public abstract ImmutableMultiset<Slot> getPitchingSlots();
}
