package com.github.lstephen.ootp.ai.selection;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;

public final class SlotSelection implements Selection {

    private final Multiset<Slot> slots;

    private final Integer size;

    private final Ordering<Player> ordering;

    private final Slot fillToSize;

    private SlotSelection(Builder builder) {
        Preconditions.checkNotNull(builder.slots);
        Preconditions.checkNotNull(builder.size);
        Preconditions.checkNotNull(builder.ordering);

        slots = builder.slots;
        size = builder.size;
        ordering = builder.ordering;
        fillToSize = builder.fillToSize;
    }

    @Override
    public ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced, Iterable<Player> available) {

        Multimap<Slot, Player> selected = ArrayListMultimap.create();
        Multiset<Slot> remainingSlots = HashMultiset.create(slots);

        for (Player p : forced) {
            for (Slot s : Slot.getPlayerSlots(p)) {
                if (remainingSlots.contains(s)) {
                    selected.put(s, p);
                    remainingSlots.remove(s);
                    break;
                }
                selected.put(Slot.getPrimarySlot(p), p);
            }
        }

        for (Player p : ordering.sortedCopy(available)) {

            for (Slot st : Slot.getPlayerSlots(p)) {
                if (remainingSlots.contains(st)
                    && !selected.containsValue(p)
                    && selected.size() < size) {

                    selected.put(st, p);
                    remainingSlots.remove(st);
                }
            }
        }

        if (fillToSize != null) {
            for (Player p : ordering.sortedCopy(available)) {
                if (p.getSlots().contains(fillToSize)
                    && !selected.containsValue(p)
                    && selected.size() < size) {

                    selected.put(fillToSize, p);
                }
            }
        }

        return ImmutableMultimap.copyOf(selected);
    }

    public static Builder builder() {
        return Builder.create();
    }

    private static SlotSelection build(Builder builder) {
        return new SlotSelection(builder);

    }

    public static final class Builder {

        private Multiset<Slot> slots;

        private Integer size;

        private Ordering<Player> ordering;

        private Slot fillToSize;


        private Builder() { }

        public Builder ordering(Ordering<Player> ordering) {
            this.ordering = ordering;
            return this;
        }

        public Builder slots(Iterable<Slot> slots) {
            this.slots = HashMultiset.create(slots);
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public Builder fillToSize(Slot slot) {
            this.fillToSize = slot;
            return this;
        }

        public SlotSelection build() {
            return SlotSelection.build(this);
        }

        private static Builder create() {
            return new Builder();
        }
    }
}
