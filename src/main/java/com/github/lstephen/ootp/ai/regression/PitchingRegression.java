package com.github.lstephen.ootp.ai.regression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.splits.Splits;
import com.github.lstephen.ootp.ai.stats.EraBaseRuns;
import com.github.lstephen.ootp.ai.stats.FipBaseRuns;
import com.github.lstephen.ootp.ai.stats.History;
import com.github.lstephen.ootp.ai.stats.PitchingStats;
import com.github.lstephen.ootp.ai.stats.SplitPercentagesHolder;
import com.github.lstephen.ootp.ai.stats.SplitStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;
import java.io.PrintWriter;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author lstephen
 */
public final class PitchingRegression {

    private enum Predicting { HITS, DOUBLES, STRIKEOUTS, WALKS, HOME_RUNS }

    private static final Long DEFAULT_PLATE_APPEARANCES = 700L;

    private TeamStats<PitchingStats> stats;

    private final SimpleRegression hits = new SimpleRegression();

    private final SimpleRegression doubles = new SimpleRegression();

    private final SimpleRegression strikeouts = new SimpleRegression();

    private final SimpleRegression walks = new SimpleRegression();

    private final SimpleRegression homeRuns = new SimpleRegression();

    private final SimpleRegression era = new SimpleRegression();

    private final Site site;

    private final PitchingStats leaguePitching;

    private Boolean ignoreStatsInPredictions = Boolean.FALSE;

    private PitchingRegression(Site site) {
        this.site = site;
        this.leaguePitching = site.getLeaguePitching();
    }

    public PitchingRegression ignoreStatsInPredictions() {
        this.ignoreStatsInPredictions = true;
        return this;
    }

    public Boolean isEmpty() {
        return walks.getN() == 0;
    }

    private void addData(TeamStats<PitchingStats> teamStats) {
       for (Player p : teamStats.getPlayers()) {
           Splits<PitchingStats> stats = teamStats.getSplits(p);
           Splits<PitchingRatings<?>> ratings = p.getPitchingRatings();

           if (stats != null && ratings != null) {
               addData(stats.getVsLeft(), ratings.getVsLeft());
               addData(stats.getVsRight(), ratings.getVsRight());
           }
       }
    }

    private void addData(PitchingStats stats, PitchingRatings ratings) {
        for (int i = 0; i < stats.getPlateAppearances(); i++) {
            hits.addData(ratings.getHits(), stats.getHitsPerPlateAppearance());
            doubles.addData(ratings.getGap(), stats.getDoublesPerPlateAppearance());
            strikeouts.addData(
                ratings.getStuff(), stats.getStrikeoutsPerPlateAppearance());
            walks.addData(ratings.getControl(), stats.getWalksPerPlateAppearance());
            homeRuns.addData(
                ratings.getMovement(), stats.getHomeRunsPerPlateAppearance());
        }
    }

    private double predict(Predicting predicting, int rating) {
        SimpleRegression regression;

        switch (predicting) {
            case HITS:
                regression = hits;
                break;
            case DOUBLES:
                regression = doubles;
                break;
            case STRIKEOUTS:
                regression = strikeouts;
                break;
            case HOME_RUNS:
                regression = homeRuns;
                break;
            case WALKS:
                regression = walks;
                break;
            default:
                throw new IllegalStateException();
        }

        return Math.max(0, regression.predict(rating));
    }

    public TeamStats<PitchingStats> predict(Iterable<Player> ps) {
        Map<Player, SplitStats<PitchingStats>> results = Maps.newHashMap();

        for (Player p : ps) {
            if (p.hasPitchingRatings()) {
                results.put(p, predict(p));
            }
        }

        return TeamStats.create(results);
    }

    public SplitStats<PitchingStats> predict(Player p) {
        Long paToProject = Math.round(DEFAULT_PLATE_APPEARANCES + DEFAULT_PLATE_APPEARANCES * era.getRSquare());

        SplitStats<PitchingStats> base = predict(p.getPitchingRatings(), paToProject * 100);
        PitchingStats vsLeft = base.getVsLeft();
        PitchingStats vsRight = base.getVsRight();

        if (!ignoreStatsInPredictions && stats.contains(p)) {
            SplitStats<PitchingStats> splits = stats.getSplits(p);

            vsLeft = vsLeft.add(splits.getVsLeft().multiply(100.0));
            vsRight = vsRight.add(splits.getVsRight().multiply(100.0));
        }

        return SplitStats.create(vsLeft, vsRight);
    }

    public TeamStats<PitchingStats> predictFuture(Iterable<Player> ps) {
        Map<Player, SplitStats<PitchingStats>> results = Maps.newHashMap();

        for (Player p : ps) {
            if (p.hasPitchingRatings()) {
                results.put(
                    p,
                    SplitStats.create(
                        predict(p.getPitchingPotentialRatings().getVsLeft()),
                        predict(p.getPitchingPotentialRatings().getVsRight())));
            }
        }

        return TeamStats.create(results);
    }

    public SplitStats<PitchingStats> predict(Splits<? extends PitchingRatings<?>> ratings) {
        return predict(ratings, DEFAULT_PLATE_APPEARANCES * 100);
    }

    private SplitStats<PitchingStats> predict(Splits<? extends PitchingRatings<?>> ratings, Long pa) {
        Long vsRightPa = Math.round(pa * SplitPercentagesHolder.get().getVsRhbPercentage());
        Long vsLeftPa = pa - vsRightPa;

        return SplitStats.create(
            predict(ratings.getVsLeft(), vsLeftPa),
            predict(ratings.getVsRight(), vsRightPa));
    }

    public PitchingStats predict(PitchingRatings<?> ratings) {
        return predict(ratings, DEFAULT_PLATE_APPEARANCES);
    }

    public PitchingStats predict(PitchingRatings<?> ratings, Long plateAppearances) {
        long predictedStrikeouts =
            Math.round(plateAppearances
                * predict(Predicting.STRIKEOUTS, ratings.getStuff()));

        long predictedHomeRuns =
            Math.round(plateAppearances
                * predict(Predicting.HOME_RUNS, ratings.getMovement()));

        long predictedWalks =
            Math.round(plateAppearances
                * predict(Predicting.WALKS, ratings.getControl()));

        long predictedHits;
        long predictedDoubles;
        switch (site.getType()) {
            case OOTP5:
                predictedHits =
                    Math.round(plateAppearances * predict(Predicting.HITS, ratings.getHits()));
                predictedDoubles =
                    Math.round(plateAppearances * predict(Predicting.DOUBLES, ratings.getGap()));
                break;
            case OOTP6:
                predictedDoubles = 0;
                predictedHits =
                    Math.round(
                        (plateAppearances
                            - predictedWalks
                            - predictedStrikeouts
                            - predictedHomeRuns)
                        * leaguePitching.getBabip());
                break;
            default:
                throw new IllegalStateException();
        }

        PitchingStats predicted = new PitchingStats();
        predicted.setLeaguePitching(leaguePitching);
        predicted.setAtBats((int) (plateAppearances - predictedWalks));
        predicted.setHits(predictedHits);
        predicted.setDoubles(predictedDoubles);
        predicted.setHomeRuns(predictedHomeRuns);
        predicted.setWalks(predictedWalks);
        predicted.setStrikeouts(predictedStrikeouts);

        return predicted;
    }

    public CorrelationReport correlationReport() {
        return CorrelationReport.create(this);
    }

    private void runRegression(Site site) {
        stats = site.getTeamPitching();

        addData(stats);

        History history = History.create();

        int season = site.getDate().getYear();

        history.savePitching(stats, site, season);

        Iterable<TeamStats<PitchingStats>> historical = history.loadPitching(site, season, 5);

        for (TeamStats<PitchingStats> h : historical) {
            addData(h);
        }

        Iterable<TeamStats<PitchingStats>> all =
            Iterables.concat(ImmutableList.of(stats), historical);

        for (TeamStats<PitchingStats> tss : all) {
            for (Player p : tss.getPlayers()) {
                if (!p.isPitcher()) {
                  continue;
                }

                SplitStats<PitchingStats> splits = tss.getSplits(p);
                SplitStats<PitchingStats> predicted = predict(p.getPitchingRatings());

                for (int i = 0; i < splits.getVsLeft().getPlateAppearances(); i++) {
                    if (splits.getVsLeft().getInningsPitched() > 0) {
                        era.addData(
                            site.getPitcherSelectionMethod().getEraEstimate(splits.getVsLeft()),
                            site.getPitcherSelectionMethod().getEraEstimate(predicted.getVsLeft()));
                    }
                }
                for (int i = 0; i < splits.getVsRight().getPlateAppearances(); i++) {
                    if (splits.getVsRight().getInningsPitched() > 0) {
                        era.addData(
                            site.getPitcherSelectionMethod().getEraEstimate(splits.getVsRight()),
                            site.getPitcherSelectionMethod().getEraEstimate(predicted.getVsRight()));
                    }
                }
            }
        }
    }

    public static PitchingRegression run(Site site) {
        PitchingRegression regression = new PitchingRegression(site);
        regression.runRegression(site);
        return regression;
    }

    public static class CorrelationReport implements Printable {

        private final PitchingRegression regression;

        private CorrelationReport(@Nonnull PitchingRegression regression) {
            this.regression = regression;
        }

        @Override
        public void print(PrintWriter w) {
            w.println("  |   K%  |  BB%  |  HR%  |   H%  |  2B%  |  ERA  |");

            w.println(
                String.format(
                    "R2| %.3f | %.3f | %.3f | %.3f | %.3f | %.3f |",
                    regression.strikeouts.getRSquare(),
                    regression.walks.getRSquare(),
                    regression.homeRuns.getRSquare(),
                    regression.hits.getRSquare(),
                    regression.doubles.getRSquare(),
                    regression.era.getRSquare()));

            w.format(
                "ERA: %.2f, FIP: %.2f%n",
                EraBaseRuns.get().calculate(regression.leaguePitching),
                FipBaseRuns.get().calculate(regression.leaguePitching));
        }

        public static CorrelationReport create(PitchingRegression regression) {
            return new CorrelationReport(regression);
        }

    }

}
