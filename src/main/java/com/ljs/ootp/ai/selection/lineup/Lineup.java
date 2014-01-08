package com.ljs.ootp.ai.selection.lineup;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.BattingRatings;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Lineup implements Iterable<Lineup.Entry>, Printable {

    private static final int W_1B = 0;
    private static final int W_2B = 1;
    private static final int W_3B = 2;
    private static final int W_HR = 3;
    private static final int W_BB = 4;
    private static final int W_K = 7;
    private static final int W_OUT = 8;

    private static final double[][] WEIGHTINGS = new double[][] {
        { .515, .806, 1.121, 1.421, .385, .411, .542, -.329, -.328 },
        { .515, .799, 1.100, 1.450, .366, .396, .536, -.322, -.324 },
        { .493, .779, 1.064, 1.452, .335, .369, .514, -.317, -.315 },
        { .517, .822, 1.117, 1.472, .345, .377, .541, -.332, -.327 },
        { .513, .809, 1.106, 1.438, .346, .381, .530, -.324, -.323 },
        { .482, .763, 1.050, 1.376, .336, .368, .504, -.306, -.306 },
        { .464, .738, 1.014, 1.336, .323, .353, .486, -.296, -.296 },
        { .451, .714,  .980, 1.293, .312, .340, .470, -.287, -.286 },
        { .436, .689,  .948, 1.249, .302, .329, .454, -.278, -.277 }
    };

    private Defense defense;
    private ImmutableList<Player> order;

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
            public BattingStats getStats(TeamStats predictions, Player p) {
                return (BattingStats) predictions.getSplits(p).getVsRight();
            }

            public BattingRatings getRatings(Player p) {
                return (BattingRatings) p.getBattingRatings().getVsRight();
            }
        };

        public abstract BattingStats getStats(TeamStats teamstats, Player player);

        public abstract BattingRatings getRatings(Player player);
    }

    public Lineup() { }

    public ImmutableList<Player> getOrder() {
        return order;
    }

    public void setOrder(Iterable ps) {
        order = ImmutableList.copyOf(ps);
    }

    public void setDefense(Defense defense) {
        this.defense = defense;
    }

    public Lineup swap(Player lhs, Player rhs) {
        List<Player> order = Lists.newArrayList(this.order);

        int lhsIdx = order.indexOf(lhs);
        int rhsIdx = order.indexOf(rhs);

        Preconditions.checkState(lhsIdx >= 0, "lhs:" + lhs.getShortName() + lhsIdx);
        Preconditions.checkState(rhsIdx >= 0, "rhs:" + rhs.getShortName() + rhsIdx);

        order.add(lhsIdx, rhs);
        order.remove(lhs);
        order.add(rhsIdx, lhs);
        order.remove(rhs);

        Lineup next = new Lineup();
        next.setDefense(defense);
        next.setOrder(order);
        return next;
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

    public Double score(VsHand vs, TeamStats<BattingStats> ps) {
        Double score = 0.0;

        for (int i = 0; i < 9; i++) {
            Entry e = getEntry(i);

            if (e.getPlayer() == null) {
                continue;
            }

            score += score(vs.getStats(ps, e.getPlayer()), WEIGHTINGS[i]);
        }

        return score;
    }

    private Double score(BattingStats s, double[] w) {
        Double score = 0.0;
        score += s.getSinglesPerPlateAppearance() * w[W_1B];
        score += s.getDoublesPerPlateAppearance() * w[W_2B];
        score += s.getTriplesPerPlateAppearance() * w[W_3B];
        score += s.getHomeRunsPerPlateAppearance() * w[W_HR];
        score += s.getWalksPerPlateAppearance() * w[W_BB];
        score += s.getOutsPerPlateAppearance() * w[W_OUT];

        return score * 700;
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
