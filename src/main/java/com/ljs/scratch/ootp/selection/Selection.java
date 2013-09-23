// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Selection.java

package com.ljs.scratch.ootp.selection;

import com.google.common.collect.ImmutableMultimap;
import com.ljs.scratch.ootp.core.Player;

public interface Selection
{

    public abstract ImmutableMultimap<Slot, Player> select(Iterable<Player> iterable, Iterable<Player> iterable1);
}
