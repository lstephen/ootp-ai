package com.github.lstephen.ootp.ai.regression;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.ratings.BattingRatings;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.splits.Splits;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.History;
import com.github.lstephen.ootp.ai.stats.SplitPercentagesHolder;
import com.github.lstephen.ootp.ai.stats.SplitStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;
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

    private enum Predicting { HITS, DOUBLES, TRIPLES, HOME_RUNS, WALKS, KS }

    private static final Long DEFAULT_PLATE_APPEARANCES = 700L;

    private final TeamStats<BattingStats> stats;

    private final Map<Predicting, Regression> regressions = new HashMap<>();

    private final SimpleRegression woba = new SimpleRegression();

    private final BattingStats leagueBatting;

    private BattingRegression(BattingStats leagueBatting, TeamStats<BattingStats> stats) {
        super();
        this.leagueBatting = leagueBatting;
        this.stats = stats;
    }

    private Regression getRegression(Predicting p) {
      if (!regressions.containsKey(p)) {
        regressions.put(p, new Regression(p.toString(), "Batting"));
      }
      return regressions.get(p);
    }

    public Boolean isEmpty() {
        return getRegression(Predicting.HITS).getN() == 0;
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
            getRegression(Predicting.HITS).addBattingData(
                ratings, stats.getHitsPerPlateAppearance());
            getRegression(Predicting.DOUBLES).addBattingData(
                ratings, stats.getDoublesPerPlateAppearance());
            getRegression(Predicting.TRIPLES).addBattingData(
                ratings, stats.getTriplesPerPlateAppearance());

            getRegression(Predicting.HOME_RUNS).addBattingData(
                ratings, stats.getHomeRunsPerPlateAppearance());
            getRegression(Predicting.WALKS).addBattingData(ratings, stats.getWalksPerPlateAppearance());

            if (ratings.getK().isPresent()) {
                getRegression(Predicting.KS).addBattingData(ratings, stats.getKsPerPlateAppearance());
            }
        }
    }

    private double predict(Predicting predicting, BattingRatings<?> rating) {
        return Math.max(0, getRegression(predicting).predictBatting(rating));
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
        return predict(p.getBattingRatings());
    }

    public SplitStats<BattingStats> predictFuture(Player p) {
        return SplitStats.create(
            predict(p.getBattingPotentialRatings().getVsLeft()),
            predict(p.getBattingPotentialRatings().getVsRight()));
    }

    public TeamStats<BattingStats> predictFuture(Iterable<Player> ps) {
        Map<Player, SplitStats<BattingStats>> results = Maps.newHashMap();

        for (Player p : ps) {
            if (p.getBattingRatings() != null) {
                results.put(p, predictFuture(p));
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
                * predict(Predicting.HITS, ratings));

        long predictedDoubles =
            Math.round(plateAppearances
                * predict(Predicting.DOUBLES, ratings));

        long predictedTriples =
            Math.round(plateAppearances
                * predict(Predicting.TRIPLES, ratings));

        long predictedHomeRuns =
             Math.round(plateAppearances
                * predict(Predicting.HOME_RUNS, ratings));

        long predictedWalks =
            Math.round(plateAppearances
                * predict(Predicting.WALKS, ratings));

        long predictedKs = ratings.getK().isPresent()
            ? Math.round(plateAppearances
                * predict(Predicting.KS, ratings))
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
            print(w, regression.regressions);
        }

        private void print(PrintWriter w, Map<Predicting, Regression> r) {
            if (r.isEmpty()) { return; }

            w.println();

            w.println(r.get(Predicting.HITS).format());
            w.println(r.get(Predicting.DOUBLES).format());
            w.println(r.get(Predicting.TRIPLES).format());
            w.println(r.get(Predicting.HOME_RUNS).format());
            w.println(r.get(Predicting.WALKS).format());
            w.println(r.get(Predicting.KS).format());
            w.format("------%n");
            w.format(" wOBA| %.3f%n", regression.woba.getRSquare());
            w.format("     | %.3f%n", Math.sqrt(regression.woba.getMeanSquareError()));
        }

        public static CorrelationReport create(BattingRegression regression) {
            return new CorrelationReport(regression);
        }

    }
}
