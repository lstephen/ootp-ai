package com.ljs.ootp.ai.report;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Salary;
import com.ljs.ootp.ai.site.Site;
import java.io.PrintWriter;
import java.text.NumberFormat;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
@ParametersAreNonnullByDefault
public class SalaryReport implements Printable {

    private final Team team;

    private final Salary salary;

    private Integer total;

    public SalaryReport(Team team, Site site) {
        this.team = team;
        this.salary = site.getSalary();
    }

    public Integer getCurrentSalary(Player p) {
        return salary.getCurrentSalary(p);
    }

    public Integer getCurrentTotal() {
        if (total == null) {
            total = 0;

            for (Player p : team) {
                total += salary.getCurrentSalary(p);
            }
        }

        return total;
    }

    @Override
    public void print(PrintWriter w) {
        w.println();

        int nextTotal = 0;

        for (Player p : byCurrentSalary().reverse().sortedCopy(team)) {
            Integer s = salary.getCurrentSalary(p);

            if (s == 0) {
                continue;
            }

            Integer nextS = salary.getNextSalary(p);

            nextTotal += nextS;

            w.format(
                "%2s %-15s %2d| %11s %11s%n",
                p.getPosition(),
                StringUtils.abbreviate(p.getShortName(), 15),
                p.getAge(),
                NumberFormat.getIntegerInstance().format(s),
                nextS == 0
                    ? ""
                    : NumberFormat.getIntegerInstance().format(nextS));
        }

        w.println(
            String.format(
                "%21s| %11s %11s",
                "",
                NumberFormat.getIntegerInstance().format(getCurrentTotal()),
                NumberFormat.getIntegerInstance().format(nextTotal)));

        w.flush();
    }

    private Ordering<Player> byCurrentSalary() {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Integer>() {
                @Override
                public Integer apply(Player p) {
                    return salary.getCurrentSalary(p);
                }
            });
    }

}
