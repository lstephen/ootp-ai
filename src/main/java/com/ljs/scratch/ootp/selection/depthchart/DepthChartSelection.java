package com.ljs.scratch.ootp.selection.depthchart;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.ratings.Position;
import com.ljs.scratch.ootp.selection.All;
import com.ljs.scratch.ootp.selection.Slot;
import com.ljs.scratch.ootp.selection.lineup.AllLineups;
import com.ljs.scratch.ootp.selection.lineup.Defense;
import com.ljs.scratch.ootp.selection.lineup.Lineup;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public class DepthChartSelection {

    private final TeamStats<BattingStats> predictions;

    private DepthChartSelection(TeamStats<BattingStats> predictions) {
        this.predictions = predictions;
    }

    public AllDepthCharts select(
        AllLineups lineups, Iterable<Player> available) {

        return AllDepthCharts.create(All
            .<DepthChart>builder()
            .vsRhp(select(lineups.getVsRhp(), available, Lineup.VsHand.VS_RHP))
            .vsRhpPlusDh(select(lineups.getVsRhpPlusDh(), available, Lineup.VsHand.VS_RHP))
            .vsLhp(select(lineups.getVsLhp(), available, Lineup.VsHand.VS_LHP))
            .vsLhpPlusDh(select(lineups.getVsLhpPlusDh(), available, Lineup.VsHand.VS_LHP))
            .build());
    }

    private DepthChart select(Lineup lineup, Iterable<Player> available, Lineup.VsHand vs) {
        DepthChart dc = new DepthChart();

        Iterable<Player> bench = Sets.difference(ImmutableSet.copyOf(available), lineup.playerSet());

        for (Lineup.Entry entry : lineup) {
            if (entry.getPositionEnum() == Position.PITCHER) {
                continue;
            }

            dc.setStarter(entry.getPositionEnum(), entry.getPlayer());

            if (entry.getPositionEnum() != Position.DESIGNATED_HITTER) {
                Optional<Player> dr = selectDefensiveReplacement(entry.getPositionEnum(), entry.getPlayer(), bench);

                if (dr.isPresent()) {
                    dc.setDefensiveReplacement(entry.getPositionEnum(), dr.get());
                }
            }

            selectBackup(dc, entry.getPositionEnum(), bench, vs);
        }

        return dc;
    }

    private Optional<Player> selectDefensiveReplacement(final Position position, Player starter, Iterable<Player> bench) {

        Player candidate = Ordering
            .natural()
            .onResultOf(new Function<Player, Double>() {
                public Double apply(Player p) {
                    return p.getDefensiveRatings().getPositionScore(position);
                }
            })
            .max(bench);

        return candidate.getDefensiveRatings().getPositionScore(position)
            > starter.getDefensiveRatings().getPositionScore(position)
            ? Optional.of(candidate)
            : Optional.<Player>absent();
    }

    private void selectBackup(DepthChart dc, Position position, Iterable<Player> bench, final Lineup.VsHand vs) {

        Double factor = 0.0;

        ImmutableList<Player> sortedBench = Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player player) {
                    return vs.getStats(predictions, player).getWobaPlus();
                }
            })
            .immutableSortedCopy(bench);

        Player fallback = sortedBench.get(0);

        Player backup = null;

        if (position != Position.DESIGNATED_HITTER || position != Position.FIRST_BASE) {
            Optional<Player> candidate = selectBackupByPosition(position, sortedBench);

            if (candidate.isPresent()) {
                backup = candidate.get();
                factor = 1.0;
            }
        }

        if (backup == null) {
            Optional<Player> candidate = selectBackupBySlot(position, sortedBench);

            if (candidate.isPresent()) {
                backup = candidate.get();
                factor = 0.5;
            }
        }

        if (backup == null) {
            backup = fallback;
        }

        Integer starterAbility = vs.getStats(predictions, dc.getStarter(position)).getWobaPlus();
        Integer backupAbility = vs.getStats(predictions, backup).getWobaPlus();

        Integer diff = starterAbility - backupAbility;
        Double daysOff = diff / Defense.getPositionFactor(position) + 1;

        Double pct = 100 / (daysOff + 1);

        dc.setBackup(position, backup, Math.max(Math.round(factor * pct), 1));
    }

    private Optional<Player> selectBackupByPosition(Position position, Iterable<Player> bench) {
        for (Player p : bench) {
            if (p.getDefensiveRatings().getPositionScore(position) > 0) {
                return Optional.of(p);
            }
        }

        return Optional.absent();
    }

    private Optional<Player> selectBackupBySlot(Position position, Iterable<Player> bench) {
        switch (position) {
            case SECOND_BASE:
            case THIRD_BASE:
            case SHORTSTOP:
                return selectBackupBySlot(Slot.IF, bench);
            case LEFT_FIELD:
            case CENTER_FIELD:
            case RIGHT_FIELD:
                return selectBackupBySlot(Slot.OF, bench);
            case FIRST_BASE:
            case DESIGNATED_HITTER:
                return selectBackupBySlot(Slot.H, bench);
            default:
                return Optional.absent();
        }
    }

    private Optional<Player> selectBackupBySlot(Slot slot, Iterable<Player> bench) {
        for (Player p : bench) {
            if (p.getSlots().contains(slot)) {
                return Optional.of(p);
            }
        }

        return Optional.absent();
    }

    public static DepthChartSelection create(TeamStats<BattingStats> predictions) {
        return new DepthChartSelection(predictions);
    }

}
