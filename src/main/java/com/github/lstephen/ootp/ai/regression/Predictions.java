package com.github.lstephen.ootp.ai.regression;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.selection.lineup.Lineup;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.PitcherOverall;
import com.github.lstephen.ootp.ai.stats.PitchingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;

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

    public boolean containsPitcher(Player p) {
        return pitching.contains(p);
    }

    public boolean containsHitter(Player p) {
        return hitting.contains(p);
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

    public Integer getHitting(Player p, Lineup.VsHand vs) {
      return vs.getStats(hitting, p).getWobaPlus();
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
