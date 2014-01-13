// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
package com.ljs.ootp.ai.selection.rotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.ljs.ootp.ai.player.Player;
import java.io.OutputStream;
import java.io.PrintWriter;

public final class Rotation {

    private final ImmutableList<Player> sps;
    private final ImmutableList<Player> mrs;
    private final ImmutableSet<Player> rest;

    private Rotation(Iterable<Player> sps, Iterable<Player> mrs, Iterable<Player> rest) {
        this.sps = ImmutableList.copyOf(sps);
        this.mrs = ImmutableList.copyOf(mrs);
        this.rest = ImmutableSet.copyOf(rest);
    }

    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {
        w.println();
        w.println(String.format("   %-15s %-15s %-15s %-15s %-15s", new Object[] {
            "SP", "LR", "MR", "SU", "CL"
        }));
        for(int i = 0; i < 5; i++)
            w.println(String.format("%d. %-15s %-15s %-15s %-15s %-15s", new Object[] {
                Integer.valueOf(i + 1), i >= sps.size() ? "" : sps.get(i).getShortName(), "", i >= mrs.size() ? "" : mrs.get(i).getShortName(), "", ""
            }));

        w.flush();
    }

    public static final Rotation create(Iterable<Player> sps, Iterable<Player> mrs, Iterable<Player> rest) {
        return new Rotation(sps, mrs, rest);
    }

}
