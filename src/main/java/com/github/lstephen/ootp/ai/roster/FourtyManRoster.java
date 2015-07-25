package com.github.lstephen.ootp.ai.roster;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.roster.Roster.Status;
import com.github.lstephen.ootp.ai.selection.HitterSelectionFactory;
import com.github.lstephen.ootp.ai.selection.Mode;
import com.github.lstephen.ootp.ai.selection.PitcherSelectionFactory;
import com.github.lstephen.ootp.ai.selection.Selections;
import com.github.lstephen.ootp.ai.value.TradeValue;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class FourtyManRoster implements Printable {

    private final Roster roster;

    private final Predictions predictions;

    private final TradeValue value;

    private Optional<Changes> changes = Optional.absent();

    private ImmutableSet<Player> desired25Man;

    private ImmutableSet<Player> desired40Man;

    public FourtyManRoster(Roster roster, Predictions ps, TradeValue value) {
        this.roster = roster;
        this.predictions = ps;
        this.value = value;
    }

    public void setChanges(Changes changes) {
        this.changes = Optional.of(changes);
    }

    public void print(PrintWriter w) {
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

        System.out.println("Selecting Desired 40 man...");

        Set<Player> fourtyMan = Sets.newHashSet();

        Iterable<Player> available = Iterables.filter(
            roster.getAllPlayers(),
            new Predicate<Player>() {
                public boolean apply(Player p) {
                    return roster.getStatus(p) != Status.DL
                        || p.getOn40Man().or(true)
                        || (changes.isPresent() && changes.get().get(Changes.ChangeType.FOURTY_MAN).contains(p));
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
            if (!fourtyMan.contains(p) && (p.getYearsOfProService().or(0) >= 3 || p.isUpcomingFreeAgent())) {
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
            Integer current = value.getCurrentValueVsReplacement(p);
            Integer future = value.getFutureValueVsReplacement(p);

            boolean belowReplacement = 
              Math.min(current, future) < 0
              && Math.max(current, future) <= 0;

            if (p.getAge() > 25
                && belowReplacement
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