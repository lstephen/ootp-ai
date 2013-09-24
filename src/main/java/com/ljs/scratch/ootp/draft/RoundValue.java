package com.ljs.scratch.ootp.draft;

import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.value.TradeValue;
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

    private RoundValue(TradeValue value) {
        this.value = value;
    }

    public void add(Player p) {
        overall.addValue(value.getOverall(p));
        acquisition.addValue(value.getTradeTargetValue(p));
    }

    public void add(Iterable<Player> ps) {
        for (Player p : ps) {
            add(p);
        }
    }

    public DescriptiveStatistics getOverallStats() {
        return overall;
    }

    public DescriptiveStatistics getAcquisitionStats() {
        return acquisition;
    }

    public static RoundValue create(TradeValue value) {
        return new RoundValue(value);
    }

    public void print(PrintWriter w, String label) {
        w.println(
            String.format(
            "%2s | %3.0f (%3.0f-%3.0f) | %3.0f (%3.0f-%3.0f) | %3d/%3d",
            label,
            getOverallStats().getPercentile(50),
            getOverallStats().getMin(),
            getOverallStats().getMax(),
            getAcquisitionStats().getPercentile(50),
            getAcquisitionStats().getMin(),
            getAcquisitionStats().getMax(),
            getOverallStats().getN(),
            getAcquisitionStats().getN()));
        w.flush();
    }



}
