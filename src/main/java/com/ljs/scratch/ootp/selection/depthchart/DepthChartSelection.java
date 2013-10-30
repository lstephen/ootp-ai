package com.ljs.scratch.ootp.selection.depthchart;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.fest.util.Lists;

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

    private void selectBackup(DepthChart dc, Position position, Iterable<Player> bench, Lineup.VsHand vs) {

        Player starter = dc.getStarter(position);
        Set<Player> backups = ImmutableSet.copyOf(Iterables.limit(selectBackups(position, bench, vs), 3));

        Player primary = starter;

        Long remaining = 100L;

        for (Player backup : backups) {
            Double pct = calculateBackupPct(position, primary, backup, vs);

            Long primaryPct = remaining - Math.round(pct * remaining);

            if (backups.contains(primary)) {
                dc.addBackup(position, primary, Math.max(primaryPct, 1));

            }

            remaining -= primaryPct;
            primary = backup;
        }

        dc.addBackup(position, primary, Math.max(remaining, 1));
    }

    private Double calculateBackupPct(Position p, Player primary, Player backup, Lineup.VsHand vs) {
        Double factor = backup.getDefensiveRatings().getPositionScore(p) > 0
            ? 1.0
            : 0.5;

        Integer primaryAbility = vs.getStats(predictions, primary).getWobaPlus();
        Integer backupAbility = vs.getStats(predictions, backup).getWobaPlus();

        Double daysOff = (primaryAbility - backupAbility) / Defense.getPositionFactor(p) + 1;

        return factor * 1 / (daysOff + 1);
    }

    private Iterable<Player> selectBackups(Position position, Iterable<Player> bench, final Lineup.VsHand vs) {

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

        List<Player> backups = Lists.newArrayList();

        if (position != Position.DESIGNATED_HITTER || position != Position.FIRST_BASE) {
            Iterables.addAll(backups, selectBackupByPosition(position, sortedBench));
        }

        if (backups.isEmpty()) {
            Iterables.addAll(backups, selectBackupBySlot(position, sortedBench));
        }

        if (backups.isEmpty()) {
            backups.add(fallback);
        }

        return backups;
    }

    private Iterable<Player> selectBackupByPosition(Position position, Iterable<Player> bench) {
        List<Player> result = Lists.newArrayList();

        for (Player p : bench) {
            if (p.getDefensiveRatings().getPositionScore(position) > 0) {
                result.add(p);
            }
        }

        return result;
    }

    private Iterable<Player> selectBackupBySlot(Position position, Iterable<Player> bench) {
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
                return Collections.emptySet();
        }
    }

    private Iterable<Player> selectBackupBySlot(Slot slot, Iterable<Player> bench) {
        List<Player> result = Lists.newArrayList();

        for (Player p : bench) {
            if (p.getSlots().contains(slot)) {
                result.add(p);
            }
        }

        return result;
    }

    public static DepthChartSelection create(TeamStats<BattingStats> predictions) {
        return new DepthChartSelection(predictions);
    }

}
