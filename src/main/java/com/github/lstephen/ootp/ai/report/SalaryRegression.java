package com.github.lstephen.ootp.ai.report;

import com.google.common.base.Strings;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.value.SalaryPredictor;
import com.github.lstephen.ootp.ai.value.TradeValue;
import java.io.PrintWriter;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author lstephen
 */
public class SalaryRegression implements SalaryPredictor, Printable {

    private final TradeValue trade;

    private final Site site;

    private final SimpleRegression vsOverall = new SimpleRegression();

    private final SimpleRegression vsReplacement = new SimpleRegression(false);

    private final SimpleRegression vsAcquisition = new SimpleRegression();

    public SalaryRegression(TradeValue trade, Site site) {
        this.trade = trade;
        this.site = site;
    }

    private Integer leagueMinimum() {
        if (site.getName().equals("BTH")) {
            return 500000;
        }
        if (site.getName().equals("PSD")) {
            return 248045;
        }
        return 300000;
    }

    public void add(Player p) {
        if (p != null
            && !Strings.isNullOrEmpty(p.getSalary())
            && !p.getSalary().endsWith("a")
            && !p.getSalary().endsWith("r")) {

            addData(p, site.getCurrentSalary(p) - leagueMinimum());
        }
    }

    public void add(Iterable<Player> ps) {
        for (Player p : ps) {
            add(p);
        }
    }

    private void addData(Player p, Integer salary) {
        vsOverall.addData(trade.getOverall(p), salary);
        vsReplacement.addData(trade.getCurrentValueVsReplacement(p), salary);
        vsAcquisition.addData(trade.getTradeTargetValue(p), salary);
    }

    public Integer predict(Player p) {
        Integer salary  = Math.max(
            0,
            (int) vsReplacement.predict(trade.getCurrentValueVsReplacement(p)) + leagueMinimum());

        return salary > 0 && salary < leagueMinimum()
            ? leagueMinimum()
            : salary;
    }

    private Integer getErrorRange(Player p) {
        Double pctError = Math.pow(1.0 - vsReplacement.getRSquare(), 2);

        return (int) (predict(p) * pctError);
    }

    public Integer predictMinimum(Player p) {
        Integer min = predict(p) - getErrorRange(p);

        return min > 0 && min < leagueMinimum()
            ? leagueMinimum()
            : min;
    }

    public Integer predictMaximum(Player p) {
        Integer max = predict(p) + getErrorRange(p);

        return max > 0 && max < leagueMinimum()
            ? leagueMinimum()
            : max;
    }

    @Override
    public void print(PrintWriter w) {

        w.println();

        w.println("  | vsOvr | vsRpl | vsAcq |");
        w.println(
            String.format(
                "R2| %-9.3f | %-9.3f | %-9.3f |",
                vsOverall.getRSquare(),
                vsReplacement.getRSquare(),
                vsAcquisition.getRSquare()));
        w.println(
            String.format(
                "R2| %9.3e | %9.3e | %9.3e |",
                vsOverall.getMeanSquareError(),
                vsReplacement.getMeanSquareError(),
                vsAcquisition.getMeanSquareError()));
    }

}
