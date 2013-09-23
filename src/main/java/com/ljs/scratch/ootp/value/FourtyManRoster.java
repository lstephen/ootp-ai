package com.ljs.scratch.ootp.value;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.Roster;
import com.ljs.scratch.ootp.core.Roster.Status;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.selection.Mode;
import com.ljs.scratch.ootp.selection.pitcher.PitcherSelection;
import com.ljs.scratch.ootp.selection.Selections;
import com.ljs.scratch.ootp.selection.HitterSelectionFactory;
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

    public FourtyManRoster(Roster roster, Predictions ps) {
        this.roster = roster;
        this.predictions = ps;
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
        return getCurrent40ManSize()
            + Iterables.size(getPlayersToAdd())
            - getMaxDesiredOn40Man();
    }

    private Integer getMaxDesiredOn40Man() {
        return (getDesired40ManRoster().size() + 40) / 2;
    }


    private ImmutableSet<Player> getDesired40ManRoster() {
        Set<Player> fourtyMan = Sets.newHashSet();

        fourtyMan.addAll(
            Selections
                .select(
                    HitterSelectionFactory
                        .using(predictions)
                        .create(Mode.EXPANDED),
                    Selections.onlyHitters(roster.getAllPlayers()))
                .values());

        fourtyMan.addAll(PitcherSelection
            .using(predictions)
            .selectExpandedMajorLeagueSquad(
                Selections.onlyPitchers(roster.getAllPlayers()))
            .values());

        return ImmutableSet.copyOf(fourtyMan);
    }

    private ImmutableSet<Player> getDesired25ManRoster() {
        Set<Player> ml = Sets.newHashSet();

        ml.addAll(
            Selections
                .select(
                    HitterSelectionFactory
                        .using(predictions)
                        .create(Mode.REGULAR_SEASON),
                    Selections.onlyHitters(roster.getAllPlayers()))
                .values());

        ml.addAll(PitcherSelection
            .using(predictions)
            .selectMajorLeagueSquad(
                Selections.onlyPitchers(roster.getAllPlayers()))
            .values());

        return ImmutableSet.copyOf(ml);
    }

    public Iterable<Player> getPlayersToRemove() {
        return Sets.difference(
            Selections.onlyOn40Man(roster.getAllPlayers()),
            getDesired40ManRoster());
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
                        || ml.contains(p);
                }
            });
    }

}
