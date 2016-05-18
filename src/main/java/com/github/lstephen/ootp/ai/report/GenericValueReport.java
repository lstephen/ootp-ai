package com.github.lstephen.ootp.ai.report;

import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.io.Printables;
import com.github.lstephen.ootp.ai.io.SalaryFormat;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.BattingRegression;
import com.github.lstephen.ootp.ai.regression.PitchingRegression;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.selection.Selections;
import com.github.lstephen.ootp.ai.selection.lineup.PlayerDefenseScore$;
import com.github.lstephen.ootp.ai.value.PlayerValue;
import com.github.lstephen.ootp.ai.value.ReplacementValue;
import com.github.lstephen.ootp.ai.value.SalaryPredictor;

import java.io.OutputStream;
import java.io.PrintWriter;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public class GenericValueReport implements Printable {

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

    private final SalaryPredictor salary;

    private final Predictions now;

    private Optional<Double> multiplier = Optional.absent();

    public GenericValueReport(
        Iterable<Player> ps, Predictions predictions, BattingRegression batting, PitchingRegression pitching, SalaryPredictor salary) {

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
        this.players = ImmutableSet.copyOf(Iterables.filter(players, Predicates.notNull()));
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
        Printables.print(this).to(out);
    }

    public void print(PrintWriter w) {
        w.println();
        w.format("--- %s ---%n", title);

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
                    "%2s %-25s %2d| %3d/%3d%4s %3d/%3d %3d/%3d | %3d%s | %-8s %s | %s %9s | %7s | %7s | %5s | %-20s | %s",
                    p.getListedPosition().or(""),
                    StringUtils.abbreviate(p.getName(), 25),
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
                    Selections.isHitter(p)
                      ? p.getDefensiveRatings().getPositionScores()
                      : (p.getListedPosition().or("").equals(p.getPosition()) ? "" : p.getPosition()),
                    Selections.isHitter(p)
                      ? String.format("%3.0f", PlayerDefenseScore$.MODULE$.atBestPosition(p, true).score())
                      : "   ",
                    p.getRosterStatus(),
                    StringUtils.abbreviate(p.getSalary(), 9),
                    salary == null ? "" : SalaryFormat.prettyPrint(salary.predictNow(p)),
                    salary == null ? "" : SalaryFormat.prettyPrint(salary.predictNext(p)),
                    p.getId().unwrap(),
                    StringUtils.abbreviate(p.getTeam() == null ? "" : p.getTeam(), 20),
                    p.getStars().isPresent() ? p.getStars().get().getFormattedText() : ""));
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
                return batting.predict(p).getVsLeft().getWobaPlus();
            } else  {
                return now.getPitcherOverall().getPlus(pitching.predict(p).getVsLeft());
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
                return batting.predict(p).getVsRight().getWobaPlus();
            } else  {
                return now.getPitcherOverall().getPlus(pitching.predict(p).getVsRight());
            }
        }
    }

    private String roundRating(Integer rating) {
        Long rounded = Math.round(rating / 10.0);

        return rounded >= 10 ? "T" : rounded.toString();
    }


    public void printReplacementLevelReport(OutputStream out) {
      Printables.print(this::printReplacementLevelReport).to(out);
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
