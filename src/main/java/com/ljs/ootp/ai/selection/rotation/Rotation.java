// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Rotation.java

package com.ljs.ootp.ai.selection.rotation;

import com.google.common.collect.ImmutableList;
import com.ljs.ootp.ai.player.Player;
import java.io.OutputStream;
import java.io.PrintWriter;

public final class Rotation {

    private final ImmutableList<Player> sps;
    private final ImmutableList<Player> mrs;

    private Rotation(Iterable<Player> sps, Iterable<Player> mrs) {
        this.sps = ImmutableList.copyOf(sps);
        this.mrs = ImmutableList.copyOf(mrs);
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

    public static final Rotation create(Iterable<Player> sps, Iterable<Player> mrs) {
        return new Rotation(sps, mrs);
    }

}
