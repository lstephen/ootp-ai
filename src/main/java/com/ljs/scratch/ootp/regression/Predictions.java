package com.ljs.scratch.ootp.regression;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public class Predictions {

    private final TeamStats<BattingStats> hitting;

    private final TeamStats<PitchingStats> pitching;

    private final PitcherOverall pitcherOverall;

    private Predictions(TeamStats<BattingStats> hitting, TeamStats<PitchingStats> pitching, PitcherOverall pitcherOverall) {
        this.hitting = hitting;
        this.pitching = pitching;
        this.pitcherOverall = pitcherOverall;
    }

    public boolean containsPlayer(Player p) {
        return hitting.contains(p) || pitching.contains(p);
    }

    public TeamStats<BattingStats> getAllBatting() {
        return hitting;
    }

    public TeamStats<PitchingStats> getAllPitching() {
        return pitching;
    }

    public PitcherOverall getPitcherOverall() {
        return pitcherOverall;
    }

    public Iterable<Player> getAllPlayers() {
        return ImmutableSet.copyOf(Iterables.concat(hitting.getPlayers(), pitching.getPlayers()));
    }

    public Integer getOverallHitting(Player p) {
        return hitting.getOverall(p).getWobaPlus();
    }

    public Integer getOverallPitching(Player p) {
        return pitcherOverall.getPlus(pitching, p);
    }

    public static Using predict(final Iterable<Player> ps) {
        return new Using() {
            @Override
            public Predictions using(
                BattingRegression br, PitchingRegression pr, PitcherOverall pitcherOverall) {

                return new Predictions(br.predict(ps), pr.predict(ps), pitcherOverall);
            }
        };
    }

    public static Using predictFuture(final Iterable<Player> ps) {
        return new Using() {
            @Override
            public Predictions using(
                BattingRegression br, PitchingRegression pr, PitcherOverall pitcherOverall) {

                return new Predictions(br.predictFuture(ps), pr.predictFuture(ps), pitcherOverall);
            }
        };
    }

    public interface Using {
        Predictions using(BattingRegression br, PitchingRegression pr, PitcherOverall pitcherOverall);
    }

}
