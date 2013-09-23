// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SlotSelection.java

package com.ljs.scratch.ootp.selection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.ljs.scratch.builder.ValidatingBuilder;
import com.ljs.scratch.ootp.core.Player;

// Referenced classes of package com.ljs.scratch.ootp.selection:
//            Slot, Selection

public final class SlotSelection
    implements Selection
{
    public static final class Builder
        implements com.ljs.scratch.builder.Builder
    {

        public Builder ordering(Ordering ordering)
        {
            ((SlotSelection)_flddelegate.getBuilding()).ordering = ordering;
            return this;
        }

        public Builder slots(Multiset slots)
        {
            ((SlotSelection)_flddelegate.getBuilding()).slots = slots;
            return this;
        }

        public Builder size(Integer size)
        {
            ((SlotSelection)_flddelegate.getBuilding()).size = size;
            return this;
        }

        public SlotSelection build()
        {
            return (SlotSelection)_flddelegate.build();
        }

        private static Builder create()
        {
            return new Builder();
        }

        private final ValidatingBuilder _flddelegate = ValidatingBuilder.build(SlotSelection.create());


        private Builder()
        {
        }
    }


    private SlotSelection()
    {
    }

    @Override
    public ImmutableMultimap<Slot, Player> select(Iterable<Player> forced, Iterable<Player> available)
    {
        Multimap<Slot, Player> selected = ArrayListMultimap.create();
        Multiset<Slot> remainingSlots = HashMultiset.create(slots);

        for (Player p : forced) {
            Slot slot = Slot.getPrimarySlot(p);
            selected.put(slot, p);
            remainingSlots.remove(slot);
        }

        for (Player p : ordering.sortedCopy(available)) {

            for (Slot st : Slot.getPlayerSlots(p)) {
                if(remainingSlots.contains(st) && !selected.containsValue(p) && selected.size() < size.intValue()) {
                    selected.put(st, p);
                    remainingSlots.remove(st);
                }
            }
        }

        return ImmutableMultimap.copyOf(selected);
    }

    private static SlotSelection create()
    {
        return new SlotSelection();
    }

    public static Builder builder()
    {
        return Builder.create();
    }

    private Multiset slots;
    private Integer size;
    private Ordering<Player> ordering;




}
