package com.ljs.ootp.ai.value;

import com.google.common.base.Function;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.regression.BattingRegression;
import com.ljs.ootp.ai.regression.PitchingRegression;
import com.ljs.ootp.ai.regression.Predictions;
import com.ljs.ootp.ai.site.Site;

/**
 *
 * @author lstephen
 */
public class TradeValue {

    private final PlayerValue playerValue;

    private final ReplacementValue replacementValue;

    private final ReplacementValue futureReplacementValue;

    public TradeValue(Iterable<Player> team, Predictions predictions, BattingRegression batting, PitchingRegression pitching) {
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

                int intangibles = 0;

                if (p.getClutch().isPresent()) {
                  switch (p.getClutch().get()) {
                    case GREAT:
                      intangibles += 1;
                      break;
                    case SUFFERS:
                      intangibles += -1;
                      break;
                    default:
                      // do nothing
                  }
                }

                if (p.getConsistency().isPresent()) {
                  switch (p.getConsistency().get()) {
                    case VERY_INCONSISTENT:
                      intangibles -= 1;
                      break;
                    case GOOD:
                      intangibles += 1;
                      break;
                    default:
                      // do nothing
                  }
                }

                return Math.max(now, future) - getAgingFactor(p) + intangibles;
            }
        };
    }

    private int getAgingFactor(Player p) {
        int factor = 0;
        
        int age = p.getAge();
        if (age > 33) {
            factor = (int) Math.pow(age - 33, 2);
        } else if (age < 25) {
            factor = age - 25;
        } else {
            factor = 0;
        }

        factor += Math.max(Math.min(33 - age, 6), 0);

        return factor;
    }

    public Function<Player, Integer> getTradeBaitValue(
        final Site site, final SalaryPredictor salary) {

        return new Function<Player, Integer>() {
            @Override
            public Integer apply(Player p) {
                return getTradeBaitValue(p, site, salary);
            }
        };
    }

    private Integer getTradeBaitValue(Player p, Site site, SalaryPredictor salary) {
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

    public Integer getOverallWithoutAging(Player p) {
        return Math.max(
            playerValue.getNowValue(p),
            playerValue.getFutureValue(p));
    }

    public Function<Player, Integer> getOverallWithoutAging() {
        return new Function<Player, Integer>() {
            @Override
            public Integer apply(Player p) {
                return getOverallWithoutAging(p);
            }
        };
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

    public Function<Player, Integer> getOverallNow() {
        return new Function<Player, Integer>() {
            public Integer apply(Player p) {
                return getOverallNow(p);
            }
        };
    }

    public Integer getCurrentValueVsReplacement(Player p) {
        return replacementValue.getValueVsReplacement(p);
    }

    public Integer getFutureValueVsReplacement(Player p) {
        return futureReplacementValue.getValueVsReplacement(p);
    }

    public Integer getExpectedReturn(Player p) {
        return getOverall(p);
    }

}
