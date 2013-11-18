package com.ljs.ootp.ai.report;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.io.SalaryFormat;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.regression.BattingRegression;
import com.ljs.ootp.ai.regression.PitchingRegression;
import com.ljs.ootp.ai.regression.Predictions;
import com.ljs.ootp.ai.selection.Selections;
import com.ljs.ootp.ai.value.PlayerValue;
import com.ljs.ootp.ai.value.ReplacementValue;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public class GenericValueReport {

    private String title;

    private ImmutableSet<Player> players;

    private Integer limit;

    private final PlayerValue playerValue;

    private final ReplacementValue replacementValue;

    private final ReplacementValue futureReplacementValue;

    private Function<Player, Integer> custom;

    private boolean reverse;

    private final BattingRegression batting;

    private final PitchingRegression pitching;

    private final SalaryRegression salary;

    private final Predictions now;

    private Optional<Double> multiplier = Optional.absent();

    public GenericValueReport(
        Iterable<Player> ps, Predictions predictions, BattingRegression batting, PitchingRegression pitching, SalaryRegression salary) {

        this.batting = batting;
        this.pitching = pitching;
        this.salary = salary;

        now =
            Predictions
                .predict(ps)
                .using(batting, pitching, predictions.getPitcherOverall());

        Predictions future =
            Predictions
                .predictFuture(ps)
                .using(batting, pitching, predictions.getPitcherOverall());

        this.playerValue = new PlayerValue(predictions, batting, pitching);
        this.replacementValue = new ReplacementValue(
            now,
            new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return playerValue.getNowValue(p);
                }},
            new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return playerValue.getNowAbility(p);
                }}
            );


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
                }
            );
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPlayers(Iterable<Player> players) {
        this.players = ImmutableSet.copyOf(players);
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public Integer getValue(Player p) {
        return Math.max(
            replacementValue.getValueVsReplacement(p),
            futureReplacementValue.getValueVsReplacement(p));
    }

    public Integer getOverallValue(Player p) {
        return Math.max(
            playerValue.getNowValue(p),
            playerValue.getFutureValue(p));
    }

    public void setCustomValueFunction(Function<Player, Integer> custom) {
        this.custom = custom;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = Optional.of(multiplier);
    }

    public void clearMultiplier() {
        this.multiplier = Optional.absent();
    }

    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {
        w.println();
        w.println(String.format("**%-16s** | %7s %7s %7s ", StringUtils.abbreviate(title, 16), "OVR", "vL/vR", "vRpl"));

        Iterable<Player> ps = Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return custom == null ? getValue(p) : custom.apply(p);
                }
            })
            .compound(Player.byTieBreak())
            .sortedCopy(players);

        if (reverse) {
            ps = ImmutableList.copyOf(ps).reverse();
        }

        if (limit != null) {
            ps = Iterables.limit(ps, limit);
        }

        for (Player p : ps) {
            Integer value = custom == null ? getValue(p) : custom.apply(p);

            String mv = "";

            if (multiplier.isPresent()) {
                mv = String.format(" (%3.0f)", multiplier.get() * value);
            }

            Integer current = playerValue.getNowValue(p);
            Integer ceiling = playerValue.getCeilingValue(p);
            Integer future = playerValue.getFutureValue(p);

            w.println(
                String.format(
                    "%2s %-15s %2d| %3d/%3d%4s %3d/%3d %3d/%3d | %3d%s | %8s | %-13s |%s %8s | %6s | %2s %5s | %s",
                    p.getPosition(),
                    StringUtils.abbreviate(p.getShortName(), 15),
                    p.getAge(),
                    current,
                    ceiling,
                    ceiling.equals(future) ? "" : String.format("/%3d", future),
                    getNowVsLeft(p),
                    getNowVsRight(p),
                    replacementValue.getValueVsReplacement(p),
                    futureReplacementValue.getValueVsReplacement(p),
                    value,
                    mv,
                    Selections.isHitter(p) ? p.getDefensiveRatings().getPositionScores() : "",
                    Joiner.on(',').join(p.getSlots()),
                    p.getRosterStatus(),
                    StringUtils.abbreviate(p.getSalary(), 8),
                    SalaryFormat.prettyPrint(salary.predict(p)),
                    p.getListedPosition().or("").equals(p.getPosition()) ? "" : p.getListedPosition().or(""),
                    p.getId().unwrap(),
                    p.getTeam() == null ? "" : p.getTeam()));
        }

        w.flush();
    }

    private Integer getNowVsLeft(Player p) {
        if (now.containsPlayer(p)) {
            if (Selections.isHitter(p)) {
                return now.getAllBatting().getSplits(p).getVsLeft().getWobaPlus();
            } else {
                return now
                    .getPitcherOverall()
                    .getPlus(now.getAllPitching().getSplits(p).getVsLeft());
            }
        } else {
            if (Selections.isHitter(p)) {
                return batting.predict(p.getBattingRatings().getVsLeft()).getWobaPlus();
            } else  {
                return now.getPitcherOverall().getPlus(pitching.predict(p.getPitchingRatings().getVsLeft()));
            }
        }
    }

    private Integer getNowVsRight(Player p) {
        if (now.containsPlayer(p)) {
            if (Selections.isHitter(p)) {
                return now.getAllBatting().getSplits(p).getVsRight().getWobaPlus();
            } else {
                return now
                    .getPitcherOverall()
                    .getPlus(now.getAllPitching().getSplits(p).getVsRight());
            }
        } else {
            if (Selections.isHitter(p)) {
                return batting.predict(p.getBattingRatings().getVsRight()).getWobaPlus();
            } else  {
                return now.getPitcherOverall().getPlus(pitching.predict(p.getPitchingRatings().getVsRight()));
            }
        }
    }



    public void printReplacementLevelReport(OutputStream out) {
        printReplacementLevelReport(new PrintWriter(out));
    }

    public void printReplacementLevelReport(PrintWriter w) {
        w.println();
        w.println("Replacement Level");
        w.print("Current:");
        replacementValue.print(w);
        w.print("Future: ");
        futureReplacementValue.print(w);
        w.flush();
    }

}
