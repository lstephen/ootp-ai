package com.ljs.scratch.ootp.report;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.selection.Mode;
import com.ljs.scratch.ootp.selection.Slot;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class RosterReport {

    private final Iterable<Player> roster;

    private RosterReport(Iterable<Player> roster) {
        this.roster = roster;
    }

    private int getPrimaryCount(Slot s) {
        int count = 0;
        for (Player p : roster) {
            if (Slot.getPrimarySlot(p) == s) {
                count++;
            }
        }
        return count;
    }

    private int getAnyCount(Slot s) {
        int count = 0;
        for (Player p : roster) {
            if (Slot.getPlayerSlots(p).contains(s)) {
                count++;
            }
        }
        return count;
    }

    private int getRatio(Slot s) {
        return getPrimaryCount(s) * 10
            / Math.max(
                Mode.REGULAR_SEASON.getHittingSlots().count(s),
                Slot.MAJOR_LEAGUE_PITCHING_SLOTS.count(s));
    }

    public Set<Slot> getSurplusSlots() {
        Set<Slot> slots = Sets.newHashSet(Slot.values());

        slots.remove(Slot.P);

        return Sets.filter(slots, new Predicate<Slot>() {
            public boolean apply(Slot s) {
                return getRatio(s) > 45;
            }
        });
    }

    public Set<Slot> getNeededSlots() {
        Set<Slot> slots = Sets.newHashSet(Slot.values());

        slots.remove(Slot.P);

        return Sets.filter(slots, new Predicate<Slot>() {
            public boolean apply(Slot s) {
                return getRatio(s) < 35;
            }
        });
    }

    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {
        w.println();

        w.println("Roster Report");
        w.print("Primary:");
        for (Slot s : Slot.values()) {
            if (s == Slot.P) {
                continue;
            }
            w.print(String.format(" %s:%3d ", s, getPrimaryCount(s)));
        }
        w.println();

        w.print("Any:    ");
        for (Slot s : Slot.values()) {
            if (s == Slot.P) {
                continue;
            }
            w.print(String.format(" %s:%3d ", s, getAnyCount(s)));
        }
        w.println();

        w.print("Ratio:  ");
        for (Slot s : Slot.values()) {
            if (s == Slot.P) {
                continue;
            }
            w.print(String.format(" %s:%3d ", s, getRatio(s)));
        }
        w.println();

        w.print("Needs:   ");
        w.print(Joiner.on(',').join(getNeededSlots()));
        w.println();

        w.print("Surplus: ");
        w.print(Joiner.on(',').join(getSurplusSlots()));
        w.println();

        w.flush();
    }

    public static RosterReport create(Roster roster) {
        return create(roster.getAllPlayers());
    }

    public static RosterReport create(Iterable<Player> ps) {
        return new RosterReport(ps);
    }

}
