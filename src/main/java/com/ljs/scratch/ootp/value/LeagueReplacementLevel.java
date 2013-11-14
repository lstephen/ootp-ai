package com.ljs.scratch.ootp.value;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.io.Printable;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.selection.Mode;
import com.ljs.scratch.ootp.player.Slot;
import com.ljs.scratch.ootp.site.Site;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class LeagueReplacementLevel implements Printable {

    private Site site;

    private PlayerValue value;

    private Iterable<Player> players;

    private LeagueReplacementLevel(Site site, PlayerValue value, Iterable<Player> players) {
        this.site = site;
        this.value = value;
        this.players = players;
    }

    public Integer getReplacementLevel(final Slot s, Function<Player, Integer> value) {
        Iterable<Player> playersAtSlot =
            Iterables.filter(players, new Predicate<Player>() {
                public boolean apply(Player p) {
                    return Slot.getPrimarySlot(p) == s;
                }
            });

        Integer replacementLevelRank = Mode.REGULAR_SEASON.getAllSlots().count(s) * Iterables.size(site.getTeamIds());

        return value.apply(
            Iterables.getFirst(
                Iterables.skip(
                    Ordering
                        .natural()
                        .reverse()
                        .onResultOf(value)
                        .sortedCopy(playersAtSlot),
                    replacementLevelRank),
                null));
    }

    @Override
    public void print(PrintWriter w) {
        w.println();
        w.println("** League Replacement Level **");
        w.print("Current: ");
        for (Slot s : Slot.values()) {
            if (s == Slot.P) {
                continue;
            }
            w.print(String.format(" %s:%3d ", s, getReplacementLevel(s, value.getNowValue())));
        }
        w.println();
        w.print("Future:  ");
        for (Slot s : Slot.values()) {
            if (s == Slot.P) {
                continue;
            }
            w.print(String.format(" %s:%3d ", s, getReplacementLevel(s, value.getFutureValue())));
        }
        w.println();
    }

    public static LeagueReplacementLevel create(Site site, PlayerValue value, Iterable<Player> players) {
        return new LeagueReplacementLevel(site, value, players);
    }

}
