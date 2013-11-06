package com.ljs.scratch.ootp.regression;

import com.google.common.collect.Maps;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.Splits;
import com.ljs.scratch.ootp.report.Report;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.TeamBatting;
import com.ljs.scratch.ootp.stats.BattingStats;
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
public final class BattingRegression {

    private enum Predicting { HITS, EXTRA_BASE_HITS, HOME_RUNS, WALKS }

    private static final int MAX_RATING = 20;

    private static final int DEFAULT_PLATE_APPEARANCES = 700;

    private final SimpleRegression hits = new SimpleRegression();

    private final SimpleRegression extraBaseHits = new SimpleRegression();

    private final SimpleRegression homeRuns = new SimpleRegression();

    private final SimpleRegression walks = new SimpleRegression();

    private final BattingStats leagueBatting;

    private BattingRegression(BattingStats leagueBatting) {
        super();
        this.leagueBatting = leagueBatting;
    }

    public Boolean isEmpty() {
        return hits.getN() == 0;
    }

    private void addData(TeamStats<BattingStats> teamStats) {
       for (Player p : teamStats.getPlayers()) {
           Splits<BattingStats> stats = teamStats.getSplits(p);
           Splits<BattingRatings> ratings = p.getBattingRatings();

           addData(stats.getVsLeft(), ratings.getVsLeft());
           addData(stats.getVsRight(), ratings.getVsRight());
       }
    }

    private void addData(BattingStats stats, BattingRatings ratings) {
        for (int i = 0; i < stats.getPlateAppearances(); i++) {
            hits.addData(
                ratings.getContact(), stats.getHitsPerPlateAppearance());
            extraBaseHits.addData(
                ratings.getGap(), stats.getExtraBaseHitsPerPlateAppearance());
            homeRuns.addData(
                ratings.getPower(), stats.getHomeRunsPerPlateAppearance());
            walks.addData(ratings.getEye(), stats.getWalksPerPlateAppearance());
        }
    }

    private double predict(Predicting predicting, int rating) {
        SimpleRegression regression;


        switch (predicting) {
            case HITS:
                regression = hits;
                break;
            case EXTRA_BASE_HITS:
                regression = extraBaseHits;
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

    public TeamStats<BattingStats> predict(Iterable<Player> ps) {
        Map<Player, SplitStats<BattingStats>> results = Maps.newHashMap();

        for (Player p : ps) {
            if (p.getBattingRatings() != null) {
                results.put(p, predict(p));
            }
        }

        return TeamStats.create(results);
    }

    public SplitStats<BattingStats> predict(Player p) {
        return SplitStats.create(
            predict(p.getBattingRatings().getVsLeft()),
            predict(p.getBattingRatings().getVsRight()));

    }

    public TeamStats<BattingStats> predictFuture(Iterable<Player> ps) {
        Map<Player, SplitStats<BattingStats>> results = Maps.newHashMap();

        for (Player p : ps) {
            if (p.getBattingRatings() != null) {
                SplitStats<BattingStats> prediction =
                    SplitStats.create(
                        predict(p.getBattingPotentialRatings().getVsLeft()),
                        predict(p.getBattingPotentialRatings().getVsRight()));

                results.put(p, prediction);
            }
        }

        return TeamStats.create(results);
    }

    public BattingStats predict(BattingRatings ratings) {
        return predict(ratings, DEFAULT_PLATE_APPEARANCES);
    }

    public BattingStats predict(BattingRatings ratings, int plateAppearances) {
        int predictedHits =
            (int) (plateAppearances
                * predict(Predicting.HITS, ratings.getContact()));

        int predictedExtraBaseHits =
            (int) (plateAppearances
                * predict(Predicting.EXTRA_BASE_HITS, ratings.getGap()));

        double triplesPercentage = (double) leagueBatting.getTriples()
            / (leagueBatting.getDoubles() + leagueBatting.getTriples());
        int predictedTriples = (int) (predictedExtraBaseHits * triplesPercentage);
        int predictedDoubles = predictedExtraBaseHits - predictedTriples;

        int predictedHomeRuns =
            (int) (plateAppearances
                * predict(Predicting.HOME_RUNS, ratings.getPower()));

        int predictedWalks =
            (int) (plateAppearances
                * predict(Predicting.WALKS, ratings.getEye()));

        BattingStats predicted = new BattingStats();
        predicted.setLeagueBatting(leagueBatting);
        predicted.setAtBats(plateAppearances - predictedWalks);
        predicted.setHits(predictedHits);
        predicted.setDoubles(predictedDoubles);
        predicted.setTriples(predictedTriples);
        predicted.setHomeRuns(predictedHomeRuns);
        predicted.setWalks(predictedWalks);

        return predicted;
    }

    public CorrelationReport correlationReport() {
        return CorrelationReport.create(this);
    }

    private void runRegression(Site site) {
        TeamBatting teamBatting = site.getTeamBatting();

        TeamStats<BattingStats> battingStats =
            teamBatting.extract();

        addData(battingStats);

        History history = new History();

        int currentSeason = teamBatting.getYear();

        history.saveBatting(battingStats, site, currentSeason);

        for (TeamStats<BattingStats> h : history.loadBatting(site, currentSeason, 5)) {
            addData(h);
        }
    }

    public static BattingRegression run(Site site) {
        BattingRegression regression = new BattingRegression(site.getLeagueBatting());
        regression.runRegression(site);
        return regression;
    }

    public static class CorrelationReport implements Report {

        private final BattingRegression regression;

        private CorrelationReport(@Nonnull BattingRegression regression) {
            this.regression = regression;
        }

        @Override
        public void print(PrintWriter w) {
            w.println("  |   H%  |  XB%  |  HR%  |  BB%  |");

            w.println(
                String.format(
                "R2| %.3f | %.3f | %.3f | %.3f |",
                regression.hits.getRSquare(),
                regression.extraBaseHits.getRSquare(),
                regression.homeRuns.getRSquare(),
                regression.walks.getRSquare()));
        }

        public static CorrelationReport create(BattingRegression regression) {
            return new CorrelationReport(regression);
        }

    }
}
