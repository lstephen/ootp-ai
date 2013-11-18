package com.ljs.ootp.ai.roster;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.roster.Roster.Status;
import java.io.PrintWriter;
import java.util.Set;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public class RosterChanges implements Printable {

    private final Set<RosterChange> changes = Sets.newHashSet();

    public void addChange(Player player, Status from, Status to) {
        changes.add(new RosterChange(player, from, to));
    }

    @Override
    public void print(PrintWriter w) {
        w.println();
        for (RosterChange c : RosterChange.ordering().sortedCopy(changes)) {
            c.println(w);
        }
    }

    private static class RosterChange {
        private final Player player;
        private final Status from;
        private final Status to;

        public RosterChange(Player player, Status from, Status to) {
            this.player = player;
            this.from = from;
            this.to = to;
        }

        public void println(PrintWriter w) {
            w.println(
                String.format(
                    "%4s -> %-4s %2s %s",
                    from == null ? "" : from,
                    to == null ? "" : to,
                    player.getPosition(),
                    player.getShortName()));
        }

        public static Ordering<RosterChange> ordering() {
            return Ordering
                .natural()
                .nullsLast()
                .onResultOf(new Function<RosterChange, Status>() {
                    @Override
                    public Status apply(RosterChange change) {
                        Assertions.assertThat(change).isNotNull();
                        return change.from;
                    }
                })
                .compound(Ordering
                    .natural()
                    .nullsLast()
                    .onResultOf(new Function<RosterChange, Status>() {
                        @Override
                        public Status apply(RosterChange change) {
                            Assertions.assertThat(change).isNotNull();
                            return change.to;
                        }
                    }));
        }
    }


}