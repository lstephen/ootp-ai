package com.ljs.ootp.ai.report;

import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.report.SalaryRegression;
import com.ljs.ootp.ai.value.TradeValue;

import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

/**
 *
 * @author lstephen
 */
public final class Trade implements Iterable<Player> {

    private final Player send;

    private final Player receive;

    private Trade(Player send, Player receive) {
        this.send = send;
        this.receive = receive;
    }

    public Integer getValue(TradeValue tv, Site site, SalaryRegression salary) {
        int sendSalaryFactor = (site.getCurrentSalary(send) - salary.predict(send)) / 750000;
        int receiveSalaryFactor = (site.getCurrentSalary(receive) - salary.predict(receive)) / 750000;

        int salaryValue = sendSalaryFactor - receiveSalaryFactor;

        int playerValue = tv.getTradeTargetValue(receive) - tv.getTradeTargetValue(send);

        return playerValue + salaryValue;
    }


    private boolean isFeasible(TradeValue tv) {
        boolean isExpectedReturnReasonable =
            tv.getExpectedReturn(send) > tv.getOverallWithoutAging(receive);

        boolean isValue =
            tv.getRequiredValue(send) * 1.1 < tv.getTradeTargetValue(receive);

        return isExpectedReturnReasonable && isValue;
    }

    @Override
    public Iterator<Player> iterator() {
        return ImmutableList.of(send, receive).iterator();
    }

    public static final Trade create(Player send, Player receive) {
        return new Trade(send, receive);
    }

    public static Iterable<Trade> getTopTrades(
        TradeValue tv,
        Site site,
        SalaryRegression salary,
        Iterable<Player> toSend,
        Iterable<Player> allPlayers) {

        Set<Trade> allTrades = Sets.newHashSet();

        System.out.println("Sorting all players...");

        Iterable<Player> sortedAllPlayers = Ordering
            .natural()
            .reverse()
            .onResultOf(tv.getTradeTargetValue())
            .sortedCopy(allPlayers);

        for (Player send : toSend) {
            System.out.print(send.getShortName());

            int requiredValue = tv.getRequiredValue(send);

            for (Player receive : sortedAllPlayers) {
                if (tv.getTradeTargetValue(receive) <= requiredValue * 1.1) {
                    break;
                }

                Trade trade = create(send, receive);

                if (trade.isFeasible(tv)) {
                    allTrades.add(trade);
                    System.out.print('.');
                }
            }
            System.out.println();
        }

        return Ordering
            .natural()
            .reverse()
            .onResultOf((Trade t) -> t.getValue(tv, site, salary))
            .sortedCopy(allTrades);
    }

    public static Predicate<Trade> isFeasiblePredicate(final TradeValue tv) {
        return new Predicate<Trade>() {

            @Override
            public boolean apply(Trade trade) {
                return trade.isFeasible(tv);
            }
        };
    }

}
