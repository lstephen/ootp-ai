package com.github.lstephen.ootp.ai.draft;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.value.JavaAdapter;

import java.io.PrintWriter;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author lstephen
 */
public final class RoundValue {

  private final Predictor predictor;

  private final DescriptiveStatistics overall = new DescriptiveStatistics();

  private final DescriptiveStatistics acquisition = new DescriptiveStatistics();

  private final DescriptiveStatistics historicalOverall = new DescriptiveStatistics();

  private final DescriptiveStatistics historicalAcquisition = new DescriptiveStatistics();

  private RoundValue(Predictor predictor) {
    this.predictor = predictor;
  }

  public boolean isEmpty() {
    return overall.getN() == 0 && historicalOverall.getN() == 0;
  }

  public void add(Player p) {
    overall.addValue(JavaAdapter.futureValue(p, predictor).score());
    acquisition.addValue(JavaAdapter.overallValue(p, predictor).score());
    addHistorical(p);
  }

  public void add(Iterable<Player> ps) {
    ps.forEach(this::add);
  }

  public void addHistorical(Player p) {
    historicalOverall.addValue(JavaAdapter.futureValue(p, predictor).score());
    historicalAcquisition.addValue(JavaAdapter.overallValue(p, predictor).score());
  }

  public void addHistorical(Iterable<Player> ps) {
    ps.forEach(this::addHistorical);
  }

  private DescriptiveStatistics getOverallStats() {
    return overall;
  }

  private DescriptiveStatistics getAcquisitionStats() {
    return acquisition;
  }

  public static RoundValue create(Predictor predictor) {
    return new RoundValue(predictor);
  }

  public void print(PrintWriter w, String label) {
    w.print(
        String.format(
          "%2s | %3.0f (%3.0f-%3.0f) | %3.0f (%3.0f-%3.0f) %3.0f/%3.0f | %3d/%3d |",
          label,
          getOverallStats().getPercentile(50),
          getOverallStats().getMin(),
          getOverallStats().getMax(),
          getAcquisitionStats().getPercentile(50),
          getAcquisitionStats().getMin(),
          getAcquisitionStats().getMax(),
          getAcquisitionStats().getPercentile(50) / 1.1,
          getAcquisitionStats().getPercentile(50) * 1.1,
          getOverallStats().getN(),
          getAcquisitionStats().getN()));

    w.print(
        String.format(
          "| %3.0f (%3.0f-%3.0f) | %3.0f (%3.0f-%3.0f) %3.0f/%3.0f | %3d/%3d",
          historicalOverall.getPercentile(50),
          historicalOverall.getMin(),
          historicalOverall.getMax(),
          historicalAcquisition.getPercentile(50),
          historicalAcquisition.getMin(),
          historicalAcquisition.getMax(),
          historicalAcquisition.getPercentile(50) / 1.1,
          historicalAcquisition.getPercentile(50) * 1.1,
          historicalOverall.getN(),
          historicalAcquisition.getN()));
    w.println();
    w.flush();
  }



}
