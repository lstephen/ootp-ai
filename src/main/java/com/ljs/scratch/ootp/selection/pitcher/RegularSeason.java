// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RegularSeason.java

package com.ljs.scratch.ootp.selection.pitcher;

import com.google.common.collect.*;
import com.ljs.scratch.ootp.selection.*;

public final class RegularSeason
    implements Selection
{

    private RegularSeason(Ordering ordering)
    {
        this.ordering = ordering;
    }

    public ImmutableMultimap select(Iterable forced, Iterable available)
    {
        return SlotSelection.builder().slots(SLOTS).size(Integer.valueOf(SLOTS.size())).ordering(ordering).build().select(forced, available);
    }

    public static RegularSeason orderingBy(Ordering ordering)
    {
        return new RegularSeason(ordering);
    }

    public static final ImmutableMultiset SLOTS;
    private final Ordering ordering;

    static 
    {
        SLOTS = ImmutableMultiset.of(Slot.C, Slot.C, Slot.SS, Slot.IF, Slot.IF, Slot.IF, new Slot[] {
            Slot.IF, Slot.CF, Slot.OF, Slot.OF, Slot.OF, Slot.H, Slot.H, Slot.H
        });
    }
}
