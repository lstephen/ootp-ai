package com.ljs.ootp.ai.roster;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.regression.Predictions;
import com.ljs.ootp.ai.roster.Roster.Status;
import com.ljs.ootp.ai.selection.HitterSelectionFactory;
import com.ljs.ootp.ai.selection.Mode;
import com.ljs.ootp.ai.selection.PitcherSelectionFactory;
import com.ljs.ootp.ai.selection.Selections;
import com.ljs.ootp.ai.value.TradeValue;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class FourtyManRoster {

    private final Roster roster;

    private final Predictions predictions;

    private final TradeValue value;

    private ImmutableSet<Player> desired25Man;

    private ImmutableSet<Player> desired40Man;

    public FourtyManRoster(Roster roster, Predictions ps, TradeValue value) {
        this.roster = roster;
        this.predictions = ps;
        this.value = value;
    }

    public void printReport(OutputStream out) {
        printReport(new PrintWriter(out));
    }

    public void printReport(PrintWriter w) {
        w.println();
        w.println(
            String.format(
                "40 man: %d/40, Remove: %d, Add: %d",
                getCurrent40ManSize(),
                getNumberToRemove(),
                Iterables.size(getPlayersToAdd())));
        w.flush();
    }

    private Integer getCurrent40ManSize() {
        return Iterables.size(Selections.onlyOn40Man(roster.getAllPlayers()));
    }

    public Integer getNumberToRemove() {
        return Math.max(
            getCurrent40ManSize()
                + Iterables.size(getPlayersToAdd())
                - getMaxDesiredOn40Man(),
            0);
    }

    private Integer getMaxDesiredOn40Man() {
        return (getDesired40ManRoster().size() + 40) / 2;
    }


    public ImmutableSet<Player> getDesired40ManRoster() {
        if (desired40Man != null) {
            return desired40Man;
        }

        Set<Player> fourtyMan = Sets.newHashSet();

        Iterable<Player> available = Iterables.filter(
            roster.getAllPlayers(),
            new Predicate<Player>() {
                public boolean apply(Player p) {
                    return roster.getStatus(p) != Status.DL || p.getOn40Man().or(true);
                }
            });


        fourtyMan.addAll(
            Selections
                .select(
                    HitterSelectionFactory
                        .using(predictions)
                        .create(Mode.EXPANDED),
                    Selections.onlyHitters(available))
                .values());

        fourtyMan.addAll(
            Selections
                .select(
                    PitcherSelectionFactory
                        .using(predictions)
                        .create(Mode.EXPANDED),
                    Selections.onlyPitchers(available))
                .values());

        Integer sizeWillBe = fourtyMan.size() + (40 - fourtyMan.size()) / 3;

        for (Player p : Ordering.natural().reverse().onResultOf(value.getTradeTargetValue()).compound(Player.byAge()).sortedCopy(roster.getAllPlayers())) {
            if (!fourtyMan.contains(p) && p.getYearsOfProService().or(0) >= 3) {
                fourtyMan.add(p);
            }

            if (fourtyMan.size() >= sizeWillBe) {
                break;
            }
        }

        desired40Man = ImmutableSet.copyOf(fourtyMan);

        return desired40Man;
    }

    private ImmutableSet<Player> getDesired25ManRoster() {
        if (desired25Man != null) {
            return desired25Man;
        }

        Set<Player> ml = Sets.newHashSet();

        ml.addAll(
            Selections
                .select(
                    HitterSelectionFactory
                        .using(predictions)
                        .create(Mode.REGULAR_SEASON),
                    Selections.onlyHitters(roster.getAllPlayers()))
                .values());

        ml.addAll(
            Selections
                .select(
                    PitcherSelectionFactory
                        .using(predictions)
                        .create(Mode.REGULAR_SEASON),
                    Selections.onlyPitchers(roster.getAllPlayers()))
                .values());

        desired25Man = ImmutableSet.copyOf(ml);
        return desired25Man;
    }

    public Iterable<Player> getPlayersToWaive() {
        Set<Player> toWaive = Sets.newHashSet();

        for (Player p : Selections.onlyOn40Man(roster.getAllPlayers())) {
            if (value.getCurrentValueVsReplacement(p) <= 0
                && value.getFutureValueVsReplacement(p) <= 0
                && !p.getClearedWaivers().or(Boolean.TRUE)
                && !getDesired25ManRoster().contains(p)) {

                toWaive.add(p);
            }
        }

        return toWaive;
    }

    public Iterable<Player> getPlayersToRemove() {
        return
            Iterables.limit(
                Ordering
                    .natural()
                    .onResultOf(value.getTradeTargetValue())
                    .sortedCopy(
                        ImmutableSet.copyOf(
                            Sets.difference(
                                Selections.onlyOn40Man(roster.getAllPlayers()),
                                getDesired40ManRoster())))
                , Math.max(getNumberToRemove(), 0));
    }

    public Iterable<Player> getPlayersToAdd() {
        Iterable<Player> toAdd = Sets.difference(
            getDesired40ManRoster(),
            Selections.onlyOn40Man(getDesired40ManRoster()));

        final ImmutableSet<Player> ml = getDesired25ManRoster();

        return Iterables.filter(
            toAdd,
            new Predicate<Player>() {
                public boolean apply(Player p) {
                    return p.getAge() > 25
                        || roster.getStatus(p) == Status.ML
                        || ml.contains(p)
                        || p.getRuleFiveEligible().or(Boolean.FALSE);
                }
            });
    }

}
