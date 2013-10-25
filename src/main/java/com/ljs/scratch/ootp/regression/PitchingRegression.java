package com.ljs.scratch.ootp.regression;

import com.google.common.collect.Maps;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.TeamPitching;
import com.ljs.scratch.ootp.ratings.PitchingRatings;
import com.ljs.scratch.ootp.ratings.Splits;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.SplitStats;
import com.ljs.scratch.ootp.stats.TeamStats;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author lstephen
 */
public final class PitchingRegression {

    private enum Predicting { HITS, DOUBLES, STRIKEOUTS, WALKS, HOME_RUNS }

    private static final int MAX_RATING = 20;

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
        this.leaguePitching = site.getLeaguePitching().extractTotal();
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

    public void printExpectedValues(OutputStream out) {
        printExpectedValues(new PrintWriter(out));
    }

    public void printExpectedValues(PrintWriter w) {
        printCorrelations(w);

        w.println("--+-------+-------+-------+-------+-------+");

        for (int i = MAX_RATING; i > 0; i--) {
            w.println(
                String.format(
                    "%2d| %.3f | %.3f | %.3f | %.3f | %.3f |",
                    i,
                    predict(Predicting.STRIKEOUTS, i),
                    predict(Predicting.WALKS, i),
                    predict(Predicting.HOME_RUNS, i),
                    predict(Predicting.HITS, i),
                    predict(Predicting.DOUBLES, i)));
        }

        w.flush();
    }

    public void printCorrelations(OutputStream out) {
        printCorrelations(new PrintWriter(out));
    }

    public void printCorrelations(PrintWriter w) {
        w.println("  |   K%  |  BB%  |  HR%  |   H%  |  2B%  |");

        w.println(
            String.format(
                "R2| %.3f | %.3f | %.3f | %.3f | %.3f |",
                strikeouts.getRSquare(),
                walks.getRSquare(),
                homeRuns.getRSquare(),
                hits.getRSquare(),
                doubles.getRSquare()));

        w.flush();
    }

    private void runRegression(Site site) {
        TeamPitching teamPitching = site.getTeamPitching();

        TeamStats<PitchingStats> pitchingStats =
            teamPitching.extract();

        addData(pitchingStats);

        History history = new History();

        int season = teamPitching.extractDate().getYear();

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

}
