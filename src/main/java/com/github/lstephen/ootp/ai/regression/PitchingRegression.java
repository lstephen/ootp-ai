package com.github.lstephen.ootp.ai.regression;

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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/** @author lstephen */
public final class PitchingRegression {

  private enum Predicting {
    HITS,
    DOUBLES,
    TRIPLES,
    STRIKEOUTS,
    WALKS,
    HOME_RUNS
  }

  private static final Long DEFAULT_PLATE_APPEARANCES = 700L;

  private TeamStats<PitchingStats> stats;

  private final Map<Predicting, Regression> regressions = new HashMap<>();

  private final SimpleRegression era = new SimpleRegression();

  private final Site site;

  private final PitchingStats leaguePitching;

  private PitchingRegression(Site site) {
    this.site = site;
    this.leaguePitching = site.getLeaguePitching();
  }

  public Boolean isEmpty() {
    return getRegression(Predicting.WALKS).getN() == 0;
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

  private Regression getRegression(Predicting p) {
    if (!regressions.containsKey(p)) {
      regressions.put(p, new Regression(p.toString(), "Pitching"));
    }
    return regressions.get(p);
  }

  private void addData(PitchingStats stats, PitchingRatings ratings) {
    for (int i = 0; i < stats.getPlateAppearances(); i++) {
      getRegression(Predicting.HITS).addPitchingData(ratings, stats.getHitsPerPlateAppearance());
      getRegression(Predicting.DOUBLES)
          .addPitchingData(ratings, stats.getDoublesPerPlateAppearance());
      getRegression(Predicting.TRIPLES)
          .addPitchingData(ratings, stats.getTriplesPerPlateAppearance());
      getRegression(Predicting.STRIKEOUTS)
          .addPitchingData(ratings, stats.getStrikeoutsPerPlateAppearance());
      getRegression(Predicting.WALKS).addPitchingData(ratings, stats.getWalksPerPlateAppearance());
      getRegression(Predicting.HOME_RUNS)
          .addPitchingData(ratings, stats.getHomeRunsPerPlateAppearance());
    }
  }

  private double predict(Predicting predicting, PitchingRatings<?> rating) {
    return Math.max(0, getRegression(predicting).predictPitching(rating));
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
    Long paToProject = DEFAULT_PLATE_APPEARANCES;

    SplitStats<PitchingStats> base = predict(p.getPitchingRatings(), paToProject * 100);
    PitchingStats vsLeft = base.getVsLeft();
    PitchingStats vsRight = base.getVsRight();

    return SplitStats.create(vsLeft, vsRight);
  }

  public SplitStats<PitchingStats> predictFuture(Player p) {
    return SplitStats.create(
        predict(p.getPitchingPotentialRatings().getVsLeft()),
        predict(p.getPitchingPotentialRatings().getVsRight()));
  }

  public TeamStats<PitchingStats> predictFuture(Iterable<Player> ps) {
    Map<Player, SplitStats<PitchingStats>> results = Maps.newHashMap();

    for (Player p : ps) {
      if (p.hasPitchingRatings()) {
        results.put(p, predictFuture(p));
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
        predict(ratings.getVsLeft(), vsLeftPa), predict(ratings.getVsRight(), vsRightPa));
  }

  public PitchingStats predict(PitchingRatings<?> ratings) {
    return predict(ratings, DEFAULT_PLATE_APPEARANCES);
  }

  public PitchingStats predict(PitchingRatings<?> ratings, Long plateAppearances) {
    long predictedStrikeouts =
        Math.round(plateAppearances * predict(Predicting.STRIKEOUTS, ratings));

    long predictedHomeRuns = Math.round(plateAppearances * predict(Predicting.HOME_RUNS, ratings));

    long predictedWalks = Math.round(plateAppearances * predict(Predicting.WALKS, ratings));

    long predictedHits = Math.round(plateAppearances * predict(Predicting.HITS, ratings));
    long predictedDoubles = Math.round(plateAppearances * predict(Predicting.DOUBLES, ratings));
    long predictedTriples = Math.round(plateAppearances * predict(Predicting.TRIPLES, ratings));

    PitchingStats predicted = new PitchingStats();
    predicted.setLeaguePitching(leaguePitching);
    predicted.setAtBats((int) (plateAppearances - predictedWalks));
    predicted.setHits(predictedHits);
    predicted.setDoubles(predictedDoubles);
    predicted.setTriples(predictedTriples);
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

    Iterable<TeamStats<PitchingStats>> all = Iterables.concat(ImmutableList.of(stats), historical);

    for (TeamStats<PitchingStats> tss : all) {
      for (Player p : tss.getPlayers()) {
        if (!p.isPitcher()) {
          continue;
        }

        SplitStats<PitchingStats> splits = tss.getSplits(p);
        SplitStats<PitchingStats> predicted = predict(p.getPitchingRatings());

        for (int i = 0; i < splits.getVsLeft().getPlateAppearances(); i++) {
          if (splits.getVsLeft().getInningsPitched() > 0) {
            era.addData(splits.getVsLeft().getBaseRuns(), predicted.getVsLeft().getBaseRuns());
          }
        }
        for (int i = 0; i < splits.getVsRight().getPlateAppearances(); i++) {
          if (splits.getVsRight().getInningsPitched() > 0) {
            era.addData(splits.getVsRight().getBaseRuns(), predicted.getVsRight().getBaseRuns());
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
      Map<Predicting, Regression> r = regression.regressions;

      if (r.isEmpty()) {
        return;
      }

      w.println();

      w.println(r.get(Predicting.STRIKEOUTS).format());
      w.println(r.get(Predicting.WALKS).format());
      w.println(r.get(Predicting.HOME_RUNS).format());
      w.println(r.get(Predicting.HITS).format());
      w.println(r.get(Predicting.DOUBLES).format());
      w.println(r.get(Predicting.TRIPLES).format());

      w.format("-----|%n");
      w.format("  ERA| %.3f%n", regression.era.getRSquare());
      w.format("     | %.3f%n", Math.sqrt(regression.era.getMeanSquareError()));

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
