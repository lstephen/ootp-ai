package com.ljs.ootp.ai.regression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.PitchingRatings;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.splits.Splits;
import com.ljs.ootp.ai.stats.History;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.SplitStats;
import com.ljs.ootp.ai.stats.TeamStats;
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

    private static final int DEFAULT_PLATE_APPEARANCES = 700;

    private final SimpleRegression hits = new SimpleRegression();

    private final SimpleRegression doubles = new SimpleRegression();

    private final SimpleRegression strikeouts = new SimpleRegression();

    private final SimpleRegression walks = new SimpleRegression();

    private final SimpleRegression homeRuns = new SimpleRegression();

    private final SimpleRegression era = new SimpleRegression();

    private final Site site;

    private final PitchingStats leaguePitching;

    private PitchingRegression(Site site) {
        this.site = site;
        this.leaguePitching = site.getLeaguePitching();
    }

    public Boolean isEmpty() {
        return walks.getN() == 0;
    }

    private void addData(TeamStats<PitchingStats> teamStats) {
       for (Player p : teamStats.getPlayers()) {
           Splits<PitchingStats> stats = teamStats.getSplits(p);
           Splits<PitchingRatings> ratings = p.getPitchingRatings();

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
        return SplitStats.create(
            predict(p.getPitchingRatings().getVsLeft()),
            predict(p.getPitchingRatings().getVsRight()));

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

    public PitchingStats predict(PitchingRatings ratings) {
        return predict(ratings, DEFAULT_PLATE_APPEARANCES);
    }

    public PitchingStats predict(PitchingRatings ratings, int plateAppearances) {
        int predictedStrikeouts =
            (int) (plateAppearances
                * predict(Predicting.STRIKEOUTS, ratings.getStuff()));

        int predictedHomeRuns =
            (int) (plateAppearances
                * predict(Predicting.HOME_RUNS, ratings.getMovement()));

        int predictedWalks =
            (int) (plateAppearances
                * predict(Predicting.WALKS, ratings.getControl()));

        int predictedHits;
        int predictedDoubles;
        switch (site.getType()) {
            case OOTP5:
                predictedHits =
                    (int) (plateAppearances * predict(Predicting.HITS, ratings.getHits()));
                predictedDoubles =
                    (int) (plateAppearances * predict(Predicting.DOUBLES, ratings.getGap()));
                break;
            case OOTP6:
            case OOTPX:
                predictedDoubles = 0;
                predictedHits =
                    (int) (
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
        predicted.setAtBats(plateAppearances - predictedWalks);
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
        TeamStats<PitchingStats> pitchingStats = site.getTeamPitching();

        addData(pitchingStats);

        History history = History.create();

        int season = site.getDate().getYear();

        history.savePitching(pitchingStats, site, season);

        Iterable<TeamStats<PitchingStats>> historical = history.loadPitching(site, season, 5);

        for (TeamStats<PitchingStats> h : historical) {
            addData(h);
        }

        Iterable<TeamStats<PitchingStats>> all =
            Iterables.concat(ImmutableList.of(pitchingStats), historical);

        for (TeamStats<PitchingStats> tss : all) {
            for (Player p : tss.getPlayers()) {
                SplitStats<PitchingStats> splits = tss.getSplits(p);
                SplitStats<PitchingStats> predicted = predict(p);

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
        }

        public static CorrelationReport create(PitchingRegression regression) {
            return new CorrelationReport(regression);
        }

    }

}
