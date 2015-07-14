package com.ljs.ootp.ai.selection.bench;

import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.selection.lineup.Lineup;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

/**
 *
 * @author lstephen
 */
public class BenchScorer {

    private final TeamStats<BattingStats> predictions;

    public BenchScorer(TeamStats<BattingStats> predictions) {
        this.predictions = predictions;
    }

    public Double score(Player toScore, Iterable<Player> bench, Lineup lineup, Lineup.VsHand vs) {
        return score(bench, lineup, vs, Predicates.equalTo(toScore));
    }

    public Double score(Iterable<Player> bench, Lineup lineup, Lineup.VsHand vs) {
        return score(bench, lineup, vs, Predicates.<Player>alwaysTrue());
    }

    private Double score(Iterable<Player> bench, Lineup lineup, Lineup.VsHand vs, Predicate<Player> predicate) {
        Double score = 0.0;

        for (Lineup.Entry entry : lineup) {
            if (!entry.getPositionEnum().equals(Position.PITCHER)) {
                Integer count = 0;
                for (Player p : Iterables.limit(selectBenchPlayer(bench, lineup, vs, entry.getPositionEnum()), 2)) {
                    count++;
                    if (predicate.apply(p)) {
                        Double countFactor = 1.0;
                        Double positionFactor = getPositionFactor(entry.getPositionEnum());

                        if (count > 1) {
                            countFactor = 0.2;
                            positionFactor = .04;
                        }

                        score += (positionFactor * countFactor * vs.getStats(predictions, p).getWobaPlus());
                        score +=
                            (positionFactor * countFactor * p.getDefensiveRatings().getPositionScore(entry.getPositionEnum())) / 2;
                    }
                }
            }
        }

        return score;
    }

    private Double getPositionFactor(Position pos) {
        switch (pos) {
            case CATCHER:
            case SHORTSTOP:
                return .2;
            case THIRD_BASE:
            case SECOND_BASE:
                return .16;
            case CENTER_FIELD:
                return .12;
            case LEFT_FIELD:
            case RIGHT_FIELD:
                return .08;
            default:
                return .04;
        }
    }

    private ImmutableList<Player> selectBenchPlayer(Iterable<Player> bench, Lineup lineup, final Lineup.VsHand vs, Position pos) {
        ImmutableList<Player> sortedBench = Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return vs.getStats(predictions, p).getWobaPlus();
                }
            })
            .compound(Player.byTieBreak())
            .immutableSortedCopy(bench);

        List<Player> ps = new ArrayList<>();

        for (Player p : sortedBench) {
            if (p.canPlay(pos)) {
                ps.add(p);
            }
        }
        return ImmutableList.copyOf(ps);
    }

}
