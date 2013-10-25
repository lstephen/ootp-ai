package com.ljs.scratch.ootp.selection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.ljs.scratch.builder.ValidatingBuilder;
import com.ljs.scratch.ootp.player.Player;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public final class SlotSelection implements Selection {

    @NotNull
    private Multiset<Slot> slots;

    @NotNull @Min(0)
    private Integer size;

    @NotNull
    private Ordering<Player> ordering;

    private Slot fillToSize;

    private SlotSelection() { }

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

    private static SlotSelection create() {
        return new SlotSelection();
    }

    public static Builder builder() {
        return Builder.create();
    }

    public static final class Builder
        implements com.ljs.scratch.builder.Builder<SlotSelection> {

        private final ValidatingBuilder<SlotSelection> delegate =
            ValidatingBuilder.build(SlotSelection.create());

        private Builder() { }

        public Builder ordering(Ordering<Player> ordering) {
            delegate.getBuilding().ordering = ordering;
            return this;
        }

        public Builder slots(Multiset<Slot> slots) {
            delegate.getBuilding().slots = slots;
            return this;
        }

        public Builder size(Integer size) {
            delegate.getBuilding().size = size;
            return this;
        }

        public Builder fillToSize(Slot slot) {
            delegate.getBuilding().fillToSize = slot;
            return this;
        }

        @Override
        public SlotSelection build() {
            return delegate.build();
        }

        private static Builder create() {
            return new Builder();
        }

    }
}
