package com.ljs.ootp.ai.draft;

import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.value.TradeValue;
import java.io.PrintWriter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author lstephen
 */
public final class RoundValue {

    private final TradeValue value;

    private final DescriptiveStatistics overall = new DescriptiveStatistics();

    private final DescriptiveStatistics acquisition = new DescriptiveStatistics();

    private final DescriptiveStatistics historicalOverall = new DescriptiveStatistics();

    private final DescriptiveStatistics historicalAcquisition = new DescriptiveStatistics();

    private RoundValue(TradeValue value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return overall.getN() == 0 && historicalOverall.getN() == 0;
    }

    public void add(Player p) {
        overall.addValue(value.getOverall(p));
        acquisition.addValue(value.getTradeTargetValue(p));
        addHistorical(p);
    }

    public void add(Iterable<Player> ps) {
        for (Player p : ps) {
            add(p);
        }
    }

    public void addHistorical(Player p) {
        historicalOverall.addValue(value.getOverall(p));
        historicalAcquisition.addValue(value.getTradeTargetValue(p));
    }

    public void addHistorical(Iterable<Player> ps) {
        for (Player p : ps) {
            addHistorical(p);
        }
    }

    private DescriptiveStatistics getOverallStats() {
        return overall;
    }

    private DescriptiveStatistics getAcquisitionStats() {
        return acquisition;
    }

    public static RoundValue create(TradeValue value) {
        return new RoundValue(value);
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
