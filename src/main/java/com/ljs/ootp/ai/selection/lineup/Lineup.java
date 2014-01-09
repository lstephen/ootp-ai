package com.ljs.ootp.ai.selection.lineup;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.BattingRatings;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.SplitStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.fest.assertions.api.Assertions;

public class Lineup implements Iterable<Lineup.Entry>, Printable {

    private Defense defense;
    private List<Player> order;

    public static final class Entry {

        private final Position position;
        private final Player player;

        public Entry(Position position, Player player) {
            this.position = position;
            this.player = player;
        }

        public Player getPlayer() {
            return player;
        }

        public String getPosition() {
            return position.getAbbreviation();
        }

        public Position getPositionEnum() {
            return position;
        }

        public String getShortName() {
            return player == null ? "" : player.getShortName();
        }

        public String format(String fmt) {
            return String.format(fmt, getPosition(), getShortName());
        }

    }

    public static enum VsHand {

        VS_LHP {
            public BattingStats getStats(TeamStats predictions, Player p) {
                return (BattingStats) predictions.getSplits(p).getVsLeft();
            }

            public BattingRatings getRatings(Player p) {
                return (BattingRatings) p.getBattingRatings().getVsLeft();
            }
        },
        VS_RHP {
            public BattingStats getStats(TeamStats<BattingStats> predictions, Player p) {
                SplitStats<BattingStats> splits = predictions.getSplits(p);

                Assertions.assertThat(splits).isNotNull().as("Expected to find splits for " + p.getShortName());

                return splits.getVsRight();
            }

            public BattingRatings getRatings(Player p) {
                return (BattingRatings) p.getBattingRatings().getVsRight();
            }
        };

        public abstract BattingStats getStats(TeamStats<BattingStats> teamstats, Player player);

        public abstract BattingRatings getRatings(Player player);
    }

    public Lineup() { }

    public void setOrder(Iterable ps) {
        order = ImmutableList.copyOf(ps);
    }

    public void setDefense(Defense defense) {
        this.defense = defense;
    }

    public Entry getEntry(int entry) {
        if (entry >= order.size()) {
            return new Entry(Position.PITCHER, null);
        } else {
            Player p = order.get(entry);
            return new Entry(getPosition(p), p);
        }
    }

    public Iterator<Entry> iterator() {
        return Lists
            .transform(
                ImmutableList.of(0, 1, 2, 3, 4, 5, 6, 7, 8),
                new Function<Integer, Entry>() {
                    public Entry apply(Integer i) {
                        return getEntry(i);
                    }
                })
            .iterator();
    }

    public Boolean contains(Player p) {
        return order.contains(p);
    }

    public Set<Player> playerSet() {
        return ImmutableSet.copyOf(order);
    }

    public Position getPosition(Player p) {
        return defense.contains(p)  ? defense.getPosition(p) : Position.DESIGNATED_HITTER;
    }

    @Override
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
