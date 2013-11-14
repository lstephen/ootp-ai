package com.ljs.scratch.ootp.regression;

import com.ljs.scratch.ootp.stats.History;
import com.google.common.collect.Maps;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.ratings.PitchingRatings;
import com.ljs.scratch.ootp.splits.Splits;
import com.ljs.scratch.ootp.io.Printable;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.TeamPitching;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.SplitStats;
import com.ljs.scratch.ootp.stats.TeamStats;
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
        TeamPitching teamPitching = site.getTeamPitching();

        TeamStats<PitchingStats> pitchingStats =
            teamPitching.extract();

        addData(pitchingStats);

        History history = new History();

        int season = teamPitching.getYear();

        history.savePitching(pitchingStats, site, season);

        for (TeamStats<PitchingStats> h : history.loadPitching(site, season, 5)) {
            addData(h);
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
            w.println("  |   K%  |  BB%  |  HR%  |   H%  |  2B%  |");

            w.println(
                String.format(
                    "R2| %.3f | %.3f | %.3f | %.3f | %.3f |",
                    regression.strikeouts.getRSquare(),
                    regression.walks.getRSquare(),
                    regression.homeRuns.getRSquare(),
                    regression.hits.getRSquare(),
                    regression.doubles.getRSquare()));
        }

        public static CorrelationReport create(PitchingRegression regression) {
            return new CorrelationReport(regression);
        }

    }

}
