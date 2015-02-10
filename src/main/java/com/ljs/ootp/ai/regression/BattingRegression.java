package com.ljs.ootp.ai.regression;

import com.google.common.base.Optional;
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
import java.util.HashMap;
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

    private final TeamStats<BattingStats> stats;

    private final Map<Predicting, SimpleRegression> regressions = new HashMap<>();

    private final SimpleRegression woba = new SimpleRegression();

    private final BattingStats leagueBatting;

    private Boolean ignoreStatsInPredictions = Boolean.FALSE;

    private BattingRegression(BattingStats leagueBatting, TeamStats<BattingStats> stats) {
        super();
        this.leagueBatting = leagueBatting;
        this.stats = stats;
    }

    public BattingRegression ignoreStatsInPredictions() {
        this.ignoreStatsInPredictions = true;
        return this;
    }

    private SimpleRegression getRegression(Predicting predicting) {
      return getRegression(regressions, predicting);
    }

    private SimpleRegression getRegression(Map<Predicting, SimpleRegression> r, Predicting p) {
      if (!r.containsKey(p)) {
        r.put(p, new SimpleRegression());
      }
      return r.get(p);
    }

    public Boolean isEmpty() {
        return getRegression(Predicting.HITS).getN() == 0;
    }

    private void addData(TeamStats<BattingStats> teamStats) {
       for (Player p : teamStats.getPlayers()) {
           Splits<BattingStats> stats = teamStats.getSplits(p);
           Splits<BattingRatings<?>> ratings = p.getBattingRatings();

           addData(regressions, stats.getVsLeft(), ratings.getVsLeft());
           addData(regressions, stats.getVsRight(), ratings.getVsRight());
       }
    }

    private void addData(Map<Predicting, SimpleRegression> r, BattingStats stats, BattingRatings<?> ratings) {
        for (int i = 0; i < stats.getPlateAppearances(); i++) {
            getRegression(r, Predicting.HITS).addData(
                ratings.getContact(), stats.getHitsPerPlateAppearance());
            getRegression(r, Predicting.EXTRA_BASE_HITS).addData(
                ratings.getGap(), stats.getExtraBaseHitsPerPlateAppearance());

            getRegression(r, Predicting.HOME_RUNS).addData(
                ratings.getPower(), stats.getHomeRunsPerPlateAppearance());
            getRegression(r, Predicting.WALKS).addData(ratings.getEye(), stats.getWalksPerPlateAppearance());

            if (ratings.getK().isPresent()) {
                getRegression(r, Predicting.KS).addData(ratings.getK().get(), stats.getKsPerPlateAppearance());
            }
        }
    }

    private double predict(Predicting predicting, int rating) {
        return Math.max(0, getRegression(predicting).predict(rating));
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
        return predict(
            p.getBattingRatings(),
            !ignoreStatsInPredictions && stats.contains(p)
                ? Optional.of(stats.getSplits(p))
                : Optional.<SplitStats<BattingStats>>absent());
    }

    private SplitStats<BattingStats> predict(
        Splits<? extends BattingRatings<?>> ratings,
        Optional<SplitStats<BattingStats>> stats) {

        Long paToProject = Math.round(DEFAULT_PLATE_APPEARANCES + DEFAULT_PLATE_APPEARANCES * woba.getRSquare());

        Long vsRightPa = Math.round(paToProject * SplitPercentagesHolder.get().getVsRhpPercentage());
        Long vsLeftPa = paToProject - vsRightPa;

        BattingStats vsLeft = predict(ratings.getVsLeft(), vsLeftPa * 100);
        BattingStats vsRight = predict(ratings.getVsRight(), vsRightPa * 100);

        if (stats.isPresent()) {
            SplitStats<BattingStats> splits = stats.get();

            vsLeft = vsLeft.add(splits.getVsLeft().multiply(100.0));
            vsRight = vsRight.add(splits.getVsRight().multiply(100.0));
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

    public SplitStats<BattingStats> predict(Splits<? extends BattingRatings<?>> ratings) {
        Long paToProject = DEFAULT_PLATE_APPEARANCES;

        Long vsRightPa = Math.round(paToProject * SplitPercentagesHolder.get().getVsRhpPercentage());
        Long vsLeftPa = paToProject - vsRightPa;

        return SplitStats.create(
            predict(ratings.getVsLeft(), vsLeftPa * 100),
            predict(ratings.getVsRight(), vsRightPa * 100));
    }

    public BattingStats predict(BattingRatings<?> ratings) {
        return predict(ratings, DEFAULT_PLATE_APPEARANCES);
    }

    public BattingStats predict(
        BattingRatings<?> ratings, Long plateAppearances) {

        long predictedHits =
            Math.round(plateAppearances
                * predict(Predicting.HITS, ratings.getContact()));

        long predictedExtraBaseHits =
            Math.round(plateAppearances
                * predict(Predicting.EXTRA_BASE_HITS, ratings.getGap()));

        double triplesPercentage = (double) leagueBatting.getTriples()
            / (leagueBatting.getDoubles() + leagueBatting.getTriples());
        long predictedTriples = Math.round(predictedExtraBaseHits * triplesPercentage);
        long predictedDoubles = predictedExtraBaseHits - predictedTriples;

        long predictedHomeRuns =
             Math.round(plateAppearances
                * predict(Predicting.HOME_RUNS, ratings.getPower()));

        long predictedWalks =
            Math.round(plateAppearances
                * predict(Predicting.WALKS, ratings.getEye()));

        long predictedKs = ratings.getK().isPresent()
            ? Math.round(plateAppearances
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
        return run(site, site.getTeamBatting());
    }


    private static BattingRegression run(Site site, TeamStats<BattingStats> stats) {
        BattingRegression regression = new BattingRegression(site.getLeagueBatting(), stats);
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

            print(w, "R2", regression.regressions);
        }

        private void print(PrintWriter w, String label, Map<Predicting, SimpleRegression> r) {
            w.println(
                String.format(
                "%2s| %.3f | %.3f | %.3f | %.3f | %.3f | %.3f |",
                label,
                r.get(Predicting.HITS).getRSquare(),
                r.get(Predicting.EXTRA_BASE_HITS).getRSquare(),
                r.get(Predicting.HOME_RUNS).getRSquare(),
                r.get(Predicting.WALKS).getRSquare(),
                r.get(Predicting.KS).getRSquare(),
                regression.woba.getRSquare()));
        }

        public static CorrelationReport create(BattingRegression regression) {
            return new CorrelationReport(regression);
        }

    }
}
