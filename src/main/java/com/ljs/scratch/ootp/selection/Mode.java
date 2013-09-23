// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Mode.java

package com.ljs.scratch.ootp.selection;

import com.google.common.collect.*;

// Referenced classes of package com.ljs.scratch.ootp.selection:
//            Slot

public enum Mode {

    REGULAR_SEASON {
        public ImmutableMultiset getHittingSlots()
            {
                return ImmutableMultiset.of(Slot.C, Slot.C, Slot.SS, Slot.IF, Slot.IF, Slot.IF, new Slot[] {
                    Slot.IF, Slot.CF, Slot.OF, Slot.OF, Slot.OF, Slot.H, Slot.H, Slot.H
                });
            }

        }
    ,
    EXPANDED {

            public ImmutableMultiset getHittingSlots()
            {
                Multiset expanded = HashMultiset.create(REGULAR_SEASON.getHittingSlots());
                expanded.addAll(ImmutableMultiset.of(Slot.C, Slot.SS, Slot.IF, Slot.CF, Slot.OF, Slot.H, new Slot[] {
                    Slot.H
                }));
                return ImmutableMultiset.copyOf(expanded);
            }

        }
    ,
    PLAYOFFS {

            public ImmutableMultiset getHittingSlots()
            {
                Multiset playoffs = HashMultiset.create(REGULAR_SEASON.getHittingSlots());
                playoffs.add(Slot.H);
                return ImmutableMultiset.copyOf(playoffs);
            }

        };

    public abstract ImmutableMultiset<Slot> getHittingSlots();
}
