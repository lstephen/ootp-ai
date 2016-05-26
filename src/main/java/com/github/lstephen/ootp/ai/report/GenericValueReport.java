package com.github.lstephen.ootp.ai.report;

import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.io.Printables;
import com.github.lstephen.ootp.ai.io.SalaryFormat;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.BattingRegression;
import com.github.lstephen.ootp.ai.regression.PitchingRegression;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.selection.Selections;
import com.github.lstephen.ootp.ai.selection.lineup.PlayerDefenseScore$;
import com.github.lstephen.ootp.ai.value.FutureValue$;
import com.github.lstephen.ootp.ai.value.NowValue$;
import com.github.lstephen.ootp.ai.value.OverallValue$;
import com.github.lstephen.ootp.ai.value.PlayerValue;
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

    private Function<Player, Integer> custom;

    private boolean reverse;

    private final BattingRegression batting;

    private final PitchingRegression pitching;

    private final SalaryPredictor salary;

    private final Predictor predictor;

    public GenericValueReport(
        Iterable<Player> ps, Predictions predictions, BattingRegression batting, PitchingRegression pitching, SalaryPredictor salary) {

        predictor = new Predictor(batting, pitching, predictions.getPitcherOverall());

        this.batting = batting;
        this.pitching = pitching;
        this.salary = salary;

        this.playerValue = new PlayerValue(predictions, batting, pitching);
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

    public Long getValue(Player p) {
      return Math.round(OverallValue$.MODULE$.apply(p, predictor).score());
    }

    public void setCustomValueFunction(Function<Player, Integer> custom) {
        this.custom = custom;
    }

    public void useDefaultValueFunction() {
      custom = null;
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
            .onResultOf(new Function<Player, Long>() {
                public Long apply(Player p) {
                    return custom == null ? getValue(p) : custom.apply(p).longValue();
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
            Long value = custom == null ? getValue(p) : custom.apply(p).longValue();

            Integer current = playerValue.getNowValue(p);
            Integer ceiling = playerValue.getCeilingValue(p);
            Integer future = playerValue.getFutureValue(p);

            w.println(
                String.format(
                    "%2s %-25s %2d| %s | %s | %s | %-8s | %s %9s | %7s/%7s | %5s | %-20s | %s",
                    p.getListedPosition().or(""),
                    StringUtils.abbreviate(p.getName(), 25),
                    p.getAge(),
                    NowValue$.MODULE$.apply(p, predictor).format(),
                    FutureValue$.MODULE$.apply(p, predictor).format(),
                    OverallValue$.MODULE$.apply(p, predictor).format(),
                    Selections.isHitter(p)
                      ? p.getDefensiveRatings().getPositionScores()
                      : "",
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

    private String roundRating(Integer rating) {
        Long rounded = Math.round(rating / 10.0);

        return rounded >= 10 ? "T" : rounded.toString();
    }

}
