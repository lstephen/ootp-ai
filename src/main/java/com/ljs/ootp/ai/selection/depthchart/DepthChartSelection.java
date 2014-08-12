package com.ljs.ootp.ai.selection.depthchart;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.selection.lineup.All;
import com.ljs.ootp.ai.selection.lineup.AllLineups;
import com.ljs.ootp.ai.selection.lineup.Defense;
import com.ljs.ootp.ai.selection.lineup.Lineup;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.fest.util.Lists;

/**
 *
 * @author lstephen
 */
public final class DepthChartSelection {

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
            .compound(Player.byTieBreak())
            .max(bench);

        return candidate.getDefensiveRatings().getPositionScore(position)
            > starter.getDefensiveRatings().getPositionScore(position) * 1.1
            ? Optional.of(candidate)
            : Optional.<Player>absent();
    }

    private void selectBackup(DepthChart dc, Position position, Iterable<Player> bench, Lineup.VsHand vs) {

        Player starter = dc.getStarter(position);
        Set<Player> backups = ImmutableSet.copyOf(Iterables.limit(selectBackups(position, bench, vs), 2));

        Player primary = starter;

        Long remaining = 100L;

        for (Player backup : backups) {
            Double pct = calculateBackupPct(position, primary, backup, vs);

            Long primaryPct = remaining - Math.round(pct * remaining);

            if (backups.contains(primary) && primaryPct >= 1) {
                dc.addBackup(position, primary, primaryPct);

            }

            remaining -= primaryPct;
            primary = backup;
        }

        if (remaining >= 1) {
            dc.addBackup(position, primary, remaining);
        }
    }

    private Double calculateBackupPct(Position p, Player primary, Player backup, Lineup.VsHand vs) {
        Double factor =
            primary.canPlay(p) && !backup.canPlay(p) ? 0.5 : 1.0;

        Double primaryAbility = vs.getStats(predictions, primary).getWobaPlus()
            + Defense.getPositionFactor(p) * primary.getDefensiveRatings().getPositionScore(p) / 2;
        Double backupAbility = vs.getStats(predictions, backup).getWobaPlus()
            + Defense.getPositionFactor(p) * backup.getDefensiveRatings().getPositionScore(p) / 2;

        if (backupAbility > primaryAbility) {
            backupAbility = primaryAbility;
        }

        Double daysOff = (double) (primaryAbility - backupAbility) / Defense.getPositionFactor(p) + 1;

        return factor * 1 / (daysOff + 1);
    }

    private Iterable<Player> selectBackups(final Position position, Iterable<Player> bench, final Lineup.VsHand vs) {

        ImmutableList<Player> sortedBench = Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Double>() {
                public Double apply(Player player) {
                    return vs.getStats(predictions, player).getWobaPlus()
                        + Defense.getPositionFactor(position) * player.getDefensiveRatings().getPositionScore(position) / 2;
                }
            })
            .compound(Player.byTieBreak())
            .immutableSortedCopy(bench);

        Player fallback = sortedBench.get(0);

        List<Player> backups = Lists.newArrayList();

        if (position != Position.DESIGNATED_HITTER && position != Position.FIRST_BASE) {
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
