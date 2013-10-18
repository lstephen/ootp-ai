package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

public class AllLineups implements Iterable<Lineup> {

    private static final Integer LINEUP_SIZE = 9;
    private static final String LINEUP_ENTRY_FORMAT = "%2s %-15s";

    private Lineup vsRhp;
    private Lineup vsRhpPlusDh;
    private Lineup vsLhp;
    private Lineup vsLhpPlusDh;

    public Lineup getVsRhp() {
        return vsRhp;
    }

    public void setVsRhp(Lineup vsRhp) {
        this.vsRhp = vsRhp;
    }

    public Lineup getVsRhpPlusDh() {
        return vsRhpPlusDh;
    }

    public void setVsRhpPlusDh(Lineup vsRhpPlusDh) {
        this.vsRhpPlusDh = vsRhpPlusDh;
    }

    public Lineup getVsLhp() {
        return vsLhp;
    }

    public void setVsLhp(Lineup vsLhp) {
        this.vsLhp = vsLhp;
    }

    public Lineup getVsLhpPlusDh() {
        return vsLhpPlusDh;
    }

    public void setVsLhpPlusDh(Lineup vsLhpPlusDh) {
        this.vsLhpPlusDh = vsLhpPlusDh;
    }

    @Override
    public Iterator<Lineup> iterator() {
        return ImmutableList
            .of(vsRhp, vsRhpPlusDh, vsLhp, vsLhpPlusDh)
            .iterator();
    }

    public Iterable<Player> getAllPlayers() {
        Set<Player> players = Sets.newHashSet();

        for (Lineup l : this) {
            for (Lineup.Entry e : l) {
                if (e.getPlayer() != null) {
                    players.add(e.getPlayer());
                }
            }
        }

        return players;
    }

    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {
        w.println();
        w.println(
            String.format(
                "   %-19s %-19s %-19s %-19s",
                "vsRHP",
                "vsRHP+DH",
                "vsLHP",
                "vsLHP+DH"
        ));

        for (int i = 0; i < LINEUP_SIZE; i++) {
            w.println(
                String.format(
                    "%d. %-19s %-19s %-19s %-19s",
                    Integer.valueOf(i + 1),
                    vsRhp.getEntry(i).format(LINEUP_ENTRY_FORMAT),
                    vsRhpPlusDh.getEntry(i).format(LINEUP_ENTRY_FORMAT),
                    vsLhp.getEntry(i).format(LINEUP_ENTRY_FORMAT),
                    vsLhpPlusDh.getEntry(i).format(LINEUP_ENTRY_FORMAT)
                ));
        }

        w.flush();
    }

}
