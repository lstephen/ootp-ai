package com.ljs.ootp.ai.report;

import com.google.common.base.Strings;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.value.SalaryPredictor;
import com.ljs.ootp.ai.value.TradeValue;
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
