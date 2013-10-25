// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Rotation.java

package com.ljs.scratch.ootp.selection.rotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.ljs.scratch.ootp.player.Player;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class Rotation
{

    public Rotation()
    {
        starters = Lists.newArrayList();
        middleRelievers = Lists.newArrayList();
    }

    public void setStarters(Iterable ps)
    {
        starters = ImmutableList.copyOf(ps);
    }

    public void setMiddleRelievers(Iterable middleRelievers)
    {
        this.middleRelievers = ImmutableList.copyOf(middleRelievers);
    }

    public void print(OutputStream out)
    {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w)
    {
        w.println();
        w.println(String.format("   %-15s %-15s %-15s %-15s %-15s", new Object[] {
            "SP", "LR", "MR", "SU", "CL"
        }));
        for(int i = 0; i < 5; i++)
            w.println(String.format("%d. %-15s %-15s %-15s %-15s %-15s", new Object[] {
                Integer.valueOf(i + 1), i >= starters.size() ? "" : ((Player)starters.get(i)).getShortName(), "", i >= middleRelievers.size() ? "" : ((Player)middleRelievers.get(i)).getShortName(), "", ""
            }));

        w.flush();
    }

    private List starters;
    private List middleRelievers;
}
