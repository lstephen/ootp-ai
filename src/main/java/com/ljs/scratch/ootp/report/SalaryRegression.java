package com.ljs.scratch.ootp.report;

import com.google.common.base.Strings;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.value.TradeValue;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import org.apache.commons.math3.stat.regression.MillerUpdatingRegression;
import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.stat.regression.UpdatingMultipleLinearRegression;

/**
 *
 * @author lstephen
 */
public class SalaryRegression {

    private static final Integer NUMBER_OF_VARIABLES = 6;

    private final TradeValue trade;

    private final Site site;

    private final SimpleRegression vsOverall = new SimpleRegression();

    private final SimpleRegression vsReplacement = new SimpleRegression(false);

    private final SimpleRegression vsAcquisition = new SimpleRegression();

    private final UpdatingMultipleLinearRegression regression = new MillerUpdatingRegression(NUMBER_OF_VARIABLES, true);

    public SalaryRegression(TradeValue trade, Site site) {
        this.trade = trade;
        this.site = site;
    }

    private Integer leagueMinimum() {
        if (site.getName().equals("BTH")) {
            return 500000;
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

        regression.addObservation(
            new double[] {
                p.getAge(),
                trade.getOverall(p),
                //Math.pow(trade.getOverall(p), 1.5),
                //Math.pow(trade.getOverall(p), 2.0),
                //Math.pow(trade.getOverall(p), 3.0),
                trade.getOverallNow(p),
                //Math.pow(trade.getOverallNow(p), 2.0),
                //Math.pow(trade.getOverallNow(p), 3.0),
                trade.getCurrentValueVsReplacement(p),
                //Math.pow(trade.getCurrentValueVsReplacement(p), 2.0) * trade.getCurrentValueVsReplacement(p) < 0 ? -1 : 1,
                //Math.pow(trade.getCurrentValueVsReplacement(p), 3.0),
                trade.getTradeTargetValue(p),
                //Math.pow(trade.getTradeTargetValue(p), 2.0),
                //Math.pow(trade.getTradeTargetValue(p), 3.0),
                trade.getOverall(p) + trade.getCurrentValueVsReplacement(p),
                //Math.pow(trade.getOverall(p) + trade.getCurrentValueVsReplacement(p), 2.0),
                //Math.pow(trade.getOverall(p) + trade.getCurrentValueVsReplacement(p), 3.0),

            },
            salary.doubleValue());
    }

    public Integer predict(Player p) {
        Integer salary  = Math.max(
            0,
            (int) vsReplacement.predict(trade.getCurrentValueVsReplacement(p)) + leagueMinimum());

        return salary > 0 && salary < leagueMinimum()
            ? leagueMinimum()
            : salary;

        /*RegressionResults r = regression.regress();

        RealMatrix b = new Array2DRowRealMatrix(r.getParameterEstimates());

        double[][] obs = new double[][] {
            {
                1.0,
                p.getAge(),
                //trade.getOverall(p).doubleValue(),
                //Math.pow(trade.getOverall(p), 1.5),
                //Math.pow(trade.getOverall(p).doubleValue(), 2.0),
                //Math.pow(trade.getOverall(p).doubleValue(), 3.0),
                trade.getOverallNow(p),
                //Math.pow(trade.getOverallNow(p), 2.0),
                //Math.pow(trade.getOverallNow(p), 3.0),
                //trade.getCurrentValueVsReplacement(p),
                //Math.pow(trade.getCurrentValueVsReplacement(p), 2.0) * trade.getCurrentValueVsReplacement(p) < 0 ? -1 : 1,
                //Math.pow(trade.getCurrentValueVsReplacement(p), 3.0),
                trade.getTradeTargetValue(p),
                //Math.pow(trade.getTradeTargetValue(p), 2.0),
                //Math.pow(trade.getTradeTargetValue(p), 3.0),
                //trade.getOverall(p) + trade.getCurrentValueVsReplacement(p),
                //Math.pow(trade.getOverall(p) + trade.getCurrentValueVsReplacement(p), 2.0),
                //Math.pow(trade.getOverall(p) + trade.getCurrentValueVsReplacement(p), 3.0),
            }
        };

        RealMatrix x = new Array2DRowRealMatrix(obs);

        RealMatrix y = x.multiply(b);

        return (int) y.getEntry(0, 0);*/
    }

    public void printCoefficients(OutputStream out) {
        printCoefficients(new PrintWriter(out));
    }

    public void printCoefficients(PrintWriter w) {
        RegressionResults r = regression.regress();

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


        w.println(String.format("OLS: %.3f/%9.3e", r.getRSquared(), r.getMeanSquareError()));
        w.println("b:" + Arrays.toString(r.getParameterEstimates()));
        w.flush();
    }

}
