package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.selection.All;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import org.fest.assertions.api.Assertions;

public final class AllLineups implements Iterable<Lineup> {

    private static final Integer LINEUP_SIZE = 9;
    private static final String LINEUP_ENTRY_FORMAT = "%2s %-15s";

    private final All<Lineup> all;

    private AllLineups(All<Lineup> all) {
        Assertions.assertThat(all).isNotNull();

        this.all = all;
    }

    public Lineup getVsRhp() {
        return all.getVsRhp();
    }

    public Lineup getVsRhpPlusDh() {
        return all.getVsRhpPlusDh();
    }

    public Lineup getVsLhp() {
        return all.getVsLhp();
    }

    public Lineup getVsLhpPlusDh() {
        return all.getVsLhpPlusDh();
    }

    @Override
    public Iterator<Lineup> iterator() {
        return all.iterator();
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

    /**
     * Get {@link Player}s that are in all lineups.
     *
     * @return
     */
    public Iterable<Player> getCommonPlayers() {

        Set<Player> result = Sets.newHashSet(getAllPlayers());

        for (Lineup l : this) {
            result = Sets.intersection(result, l.playerSet());
        }

        return result;
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
                    all.getVsRhp().getEntry(i).format(LINEUP_ENTRY_FORMAT),
                    all.getVsRhpPlusDh().getEntry(i).format(LINEUP_ENTRY_FORMAT),
                    all.getVsLhp().getEntry(i).format(LINEUP_ENTRY_FORMAT),
                    all.getVsLhpPlusDh().getEntry(i).format(LINEUP_ENTRY_FORMAT)
                ));
        }

        w.flush();
    }

    public static AllLineups create(All<Lineup> all) {
        return new AllLineups(all);
    }

}
