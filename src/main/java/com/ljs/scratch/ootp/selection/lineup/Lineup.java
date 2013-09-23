// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Lineup.java
package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.collect.ImmutableList;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.ratings.*;
import com.ljs.scratch.ootp.stats.*;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class Lineup {
    private Defense defense;

    private List order;

    public static final class Entry {

        public String getPosition() {
            return position;
        }

        public String getShortName() {
            return name;
        }

        public String format(String fmt) {
            return String.format(fmt, new Object[]{
                position, name
            });
        }
        private final String position;

        private final String name;

        public Entry(String position, String name) {
            this.position = position;
            this.name = name;
        }
    }

    public static enum VsHand {

        VS_LHP {
            BattingStats getStats(TeamStats predictions, Player p) {
                return (BattingStats) predictions.getSplits(p).getVsLeft();
            }

            BattingRatings getRatings(Player p) {
                return (BattingRatings) p.getBattingRatings().getVsLeft();
            }
        },
        VS_RHP {
            BattingStats getStats(TeamStats predictions, Player p) {
                return (BattingStats) predictions.getSplits(p).getVsRight();
            }

            BattingRatings getRatings(Player p) {
                return (BattingRatings) p.getBattingRatings().getVsRight();
            }
        };

        abstract BattingStats getStats(TeamStats teamstats, Player player);

        abstract BattingRatings getRatings(Player player);
    }

    public Lineup() {
    }

    public void setOrder(Iterable ps) {
        order = ImmutableList.copyOf(ps);
    }

    public void setDefense(Defense defense) {
        this.defense = defense;
    }

    public Entry getEntry(int entry) {
        if (entry >= order.size()) {
            return new Entry("P", "");
        } else {
            Player p = (Player) order.get(entry);
            return new Entry(
                defense.contains(p) ? ((Position) defense.getPosition(p))
                .getAbbreviation() : "DH", p.getShortName());
        }
    }

    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {
        int idx = 1;
        for (Iterator i$ = order.iterator(); i$.hasNext();) {
            Player p = (Player) i$.next();
            String pos = defense.contains(p) ? ((Position) defense.getPosition(p))
                .getAbbreviation() : "DH";
            w.println(String.format("%d. %2s %-15s", new Object[]{
                Integer.valueOf(idx), pos, p.getShortName()
            }));
            idx++;
        }

        if (idx < 10) {
            w.println("9.  P");
        }
        w.flush();
    }

}
