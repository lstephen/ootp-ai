package com.ljs.ootp.ai.regression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.ratings.BattingRatings;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.splits.Splits;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.History;
import com.ljs.ootp.ai.stats.SplitPercentagesHolder;
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
public final class BattingRegression {

    private enum Predicting { HITS, EXTRA_BASE_HITS, HOME_RUNS, WALKS, KS }

    private static final Long DEFAULT_PLATE_APPEARANCES = 700L;

    private TeamStats<BattingStats> stats;

    private final SimpleRegression hits = new SimpleRegression();

    private final SimpleRegression extraBaseHits = new SimpleRegression();

    private final SimpleRegression homeRuns = new SimpleRegression();

    private final SimpleRegression walks = new SimpleRegression();

    private final SimpleRegression ks = new SimpleRegression();

    private final SimpleRegression woba = new SimpleRegression();

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
           Splits<BattingRatings<?>> ratings = p.getBattingRatings();

           addData(stats.getVsLeft(), ratings.getVsLeft());
           addData(stats.getVsRight(), ratings.getVsRight());
       }
    }

    private void addData(BattingStats stats, BattingRatings<?> ratings) {
        for (int i = 0; i < stats.getPlateAppearances(); i++) {
            hits.addData(
                ratings.getContact(), stats.getHitsPerPlateAppearance());
            extraBaseHits.addData(
                ratings.getGap(), stats.getExtraBaseHitsPerPlateAppearance());
            homeRuns.addData(
                ratings.getPower(), stats.getHomeRunsPerPlateAppearance());
            walks.addData(ratings.getEye(), stats.getWalksPerPlateAppearance());

            if (ratings.getK().isPresent()) {
                ks.addData(ratings.getK().get(), stats.getKsPerPlateAppearance());
            }
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
            case KS:
                regression = ks;
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
        Long paToProject = Math.round(DEFAULT_PLATE_APPEARANCES + DEFAULT_PLATE_APPEARANCES * woba.getRSquare());

        Long vsRightPa = Math.round(paToProject * SplitPercentagesHolder.get().getVsRhpPercentage());
        Long vsLeftPa = paToProject - vsRightPa;

        BattingStats vsLeft = predict(p.getBattingRatings().getVsLeft(), vsLeftPa);
        BattingStats vsRight = predict(p.getBattingRatings().getVsRight(), vsRightPa);

        if (stats.contains(p)) {
            SplitStats<BattingStats> splits = stats.getSplits(p);

            vsLeft = vsLeft.add(splits.getVsLeft());
            vsRight = vsRight.add(splits.getVsRight());
        }

        return SplitStats.create(vsLeft, vsRight);
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

    public SplitStats<BattingStats> predict(Splits<BattingRatings<?>> ratings) {
        return SplitStats.create(
            predict(ratings.getVsLeft()),
            predict(ratings.getVsRight()));
    }

    public BattingStats predict(BattingRatings<?> ratings) {
        return predict(ratings, DEFAULT_PLATE_APPEARANCES);
    }

    public BattingStats predict(
        BattingRatings<?> ratings, Long plateAppearances) {

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

        int predictedKs = ratings.getK().isPresent()
            ? (int) (plateAppearances
                * predict(Predicting.KS, ratings.getK().get()))
            : 0;

        BattingStats predicted = new BattingStats();
        predicted.setLeagueBatting(leagueBatting);
        predicted.setAtBats((int) (plateAppearances - predictedWalks));
        predicted.setHits(predictedHits);
        predicted.setDoubles(predictedDoubles);
        predicted.setTriples(predictedTriples);
        predicted.setHomeRuns(predictedHomeRuns);
        predicted.setWalks(predictedWalks);
        predicted.setKs(predictedKs);

        return predicted;
    }

    public CorrelationReport correlationReport() {
        return CorrelationReport.create(this);
    }

    private void runRegression(Site site) {
        stats = site.getTeamBatting();

        addData(stats);

        History history = History.create();

        int currentSeason = site.getDate().getYear();

        history.saveBatting(stats, site, currentSeason);

        Iterable<TeamStats<BattingStats>> historical =
            history.loadBatting(site, currentSeason, 5);

        for (TeamStats<BattingStats> h : historical) {
            addData(h);
        }

        Iterable<TeamStats<BattingStats>> all =
            Iterables.concat(ImmutableList.of(stats), historical);

        for (TeamStats<BattingStats> tss : all) {
            for (Player p : tss.getPlayers()) {
                SplitStats<BattingStats> splits = tss.getSplits(p);
                SplitStats<BattingStats> predicted = predict(p.getBattingRatings());

                for (int i = 0; i < splits.getVsLeft().getPlateAppearances(); i++) {
                    woba.addData(splits.getVsLeft().getWoba(), predicted.getVsLeft().getWoba());
                }
                for (int i = 0; i < splits.getVsRight().getPlateAppearances(); i++) {
                    woba.addData(splits.getVsRight().getWoba(), predicted.getVsRight().getWoba());
                }
            }
        }
    }

    public static BattingRegression run(Site site) {
        BattingRegression regression = new BattingRegression(site.getLeagueBatting());
        regression.runRegression(site);
        return regression;
    }

    public static final class CorrelationReport implements Printable {

        private final BattingRegression regression;

        private CorrelationReport(@Nonnull BattingRegression regression) {
            this.regression = regression;
        }

        @Override
        public void print(PrintWriter w) {
            w.println("  |   H%  |  XB%  |  HR%  |  BB%  |   K%  |  wOBA |");

            w.println(
                String.format(
                "R2| %.3f | %.3f | %.3f | %.3f | %.3f | %.3f |",
                regression.hits.getRSquare(),
                regression.extraBaseHits.getRSquare(),
                regression.homeRuns.getRSquare(),
                regression.walks.getRSquare(),
                regression.ks.getRSquare(),
                regression.woba.getRSquare()));
        }

        public static CorrelationReport create(BattingRegression regression) {
            return new CorrelationReport(regression);
        }

    }
}
