package com.ljs.scratch.ootp.value;

import com.google.common.base.Function;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.Team;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.regression.BattingRegression;
import com.ljs.scratch.ootp.regression.PitchingRegression;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.report.SalaryRegression;

/**
 *
 * @author lstephen
 */
public class TradeValue {

    private Predictions predictions;

    private final PlayerValue playerValue;

    private final ReplacementValue replacementValue;

    private final ReplacementValue futureReplacementValue;

    public TradeValue(Team team, Predictions predictions, BattingRegression batting, PitchingRegression pitching) {
        this.predictions = predictions;
        this.playerValue = new PlayerValue(predictions, batting, pitching);
        this.replacementValue = new ReplacementValue(
            predictions,
            new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return playerValue.getNowValue(p);
                }},
            new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return playerValue.getNowAbility(p);
                }}
            );

        Predictions future =
            Predictions
                    .predictFuture(team)
                    .using(batting, pitching, predictions.getPitcherOverall());

        this.futureReplacementValue =
            new ReplacementValue(
                future,
                new Function<Player, Integer>() {
                    public Integer apply(Player p) {
                        return playerValue.getFutureValue(p);
                    }
                },
                new Function<Player, Integer>() {
                    public Integer apply(Player p) {
                        return playerValue.getFutureAbility(p);
                    }
                });
    }

    public Integer getTradeTargetValue(Player p) {
        return getTradeTargetValue().apply(p);
    }

    public Function<Player, Integer> getTradeTargetValue() {
        return new Function<Player, Integer>() {
            @Override
            public Integer apply(Player p) {
                int nowRepl = p.getAge() < 27
                    ? Math.max(0, replacementValue.getValueVsReplacement(p))
                    : replacementValue.getValueVsReplacement(p);

                int now = playerValue.getNowValue(p) + nowRepl;

                int futureRepl = p.getAge() < 27
                    ? Math.max(0, futureReplacementValue.getValueVsReplacement(p))
                    : futureReplacementValue.getValueVsReplacement(p);

                int future = playerValue.getFutureValue(p) + futureRepl;

                return Math.max(now, future) - getAgingFactor(p);
            }
        };
    }

    private int getAgingFactor(Player p) {
        int age = p.getAge();
        if (age > 33) {
            return (int) Math.pow(age - 33, 2);
        } else if (age < 25) {
            return age - 25;
        } else {
            return 0;
        }
    }

    public Function<Player, Integer> getTradeBaitValue(
        final Site site, final SalaryRegression salary) {

        return new Function<Player, Integer>() {
            @Override
            public Integer apply(Player p) {
                return getTradeBaitValue(p, site, salary);
            }
        };
    }

    private Integer getTradeBaitValue(Player p, Site site, SalaryRegression salary) {
        int salaryFactor = (site.getCurrentSalary(p) - salary.predict(p)) / 750000;

        return salaryFactor
            + Math.min(
              playerValue.getFutureValue(p)
                - 2 * Math.max(0, futureReplacementValue.getValueVsReplacement(p))
                + 2 * getAgingFactor(p),
              playerValue.getNowValue(p)
                - 2 * Math.max(0, replacementValue.getValueVsReplacement(p))
                + getAgingFactor(p));
    }

    public Integer getRequiredValue(Player p) {
        return getTradeTargetValue(p);
    }

    public Integer getOverall(Player p) {
        return Math.max(
                playerValue.getNowValue(p),
                playerValue.getFutureValue(p))
            - getAgingFactor(p);
    }

    public Function<Player, Integer> getOverall() {
        return new Function<Player, Integer>() {
            @Override
            public Integer apply(Player input) {
                return getOverall(input);
            }
        };
    }

    public Integer getOverallNow(Player p) {
        return playerValue.getNowValue(p) - getAgingFactor(p);
    }

    public Integer getCurrentValueVsReplacement(Player p) {
        return replacementValue.getValueVsReplacement(p);
    }

    public Integer getExpectedReturn(Player p) {
        return getOverall(p);
    }

}
