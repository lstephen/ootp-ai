package com.ljs.scratch.ootp.roster;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.config.Changes;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.regression.BattingRegression;
import com.ljs.scratch.ootp.regression.PitchingRegression;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.roster.Roster.Status;
import com.ljs.scratch.ootp.selection.HitterSelectionFactory;
import com.ljs.scratch.ootp.selection.Mode;
import com.ljs.scratch.ootp.selection.PitcherSelectionFactory;
import com.ljs.scratch.ootp.selection.Selection;
import com.ljs.scratch.ootp.selection.Selections;
import com.ljs.scratch.ootp.selection.Slot;
import com.ljs.scratch.ootp.selection.SlotSelection;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.TeamStats;
import com.ljs.scratch.ootp.value.FourtyManRoster;
import com.ljs.scratch.ootp.value.PlayerValue;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class RosterSelection {

    private final Team team;

    private final HitterSelectionFactory hitterSelectionFactory;

    private final PitcherSelectionFactory pitcherSelectionFactory;

    private final Predictions predictions;

    private final Function<Player, Integer> value;

    private Roster previous;

    private BattingRegression batting;

    private PitchingRegression pitching;

    private List<Status> remainingLevels =
        Lists.newArrayList(Status.AAA, Status.AA, Status.A);

    private RosterSelection(
        Team team, Predictions predictions, Function<Player, Integer> value) {

        this.team = team;
        this.predictions = predictions;
        this.value = value;

        hitterSelectionFactory = HitterSelectionFactory.using(predictions);
        pitcherSelectionFactory = PitcherSelectionFactory.using(predictions);
    }

    public void remove(Player p) {
        team.remove(p);
    }

    public void setPrevious(Roster previous) {
        this.previous = previous;
    }

    private Iterable<Player> getAvailableHitters(Roster roster) {
        Set<Player> available = Sets.newHashSet(
                Selections.onlyHitters(
                    Selections.onlyOn40Man(roster.getUnassigned())));

        available.removeAll(getToBeRemoved(available));

        return available;
    }

    private Iterable<Player> getAvailablePitchers(Roster roster) {
        Set<Player> available = Sets.newHashSet(Selections.onlyPitchers(Selections.onlyOn40Man(roster.getUnassigned())));
        available.removeAll(getToBeRemoved(available));
        return available;
    }

    private Set<Player> getToBeRemoved(Iterable<Player> available) {
        Set<Player> toRemove = Sets.newHashSet();
        if (previous != null) {
            FourtyManRoster fourtyMan =
                new FourtyManRoster(
                    previous,
                    Predictions
                        .predict(previous.getAllPlayers())
                        .using(batting, pitching, predictions.getPitcherOverall()),
                value);

            ImmutableSet<Player> toRemoveFromFourtyMan =
                ImmutableSet.copyOf(fourtyMan.getPlayersToRemove());

            for (Player p : available) {
                if (p.getClearedWaivers().or(Boolean.FALSE)
                    && toRemoveFromFourtyMan.contains(p)) {

                    toRemove.add(p);
                }
            }

        }
        return toRemove;
    }

    public Roster select(Mode mode, Changes changes) {
        return select(
            changes,
            hitterSelectionFactory.create(mode),
            pitcherSelectionFactory.create(mode));
    }

    private Roster select(Changes changes, Selection hitting, Selection pitching) {
        Roster roster = Roster.create(team);
        Iterable<Player> forced = getForced(changes);
        assignToDisabledList(roster, team.getInjuries());

        roster.assign(
            Roster.Status.ML,
            hitting.select(
                Selections.onlyHitters(forced),
                getAvailableHitters(roster)).values());

        roster.assign(
            Roster.Status.ML,
            pitching.select(
                Selections.onlyPitchers(forced),
                getAvailablePitchers(roster)).values());

        assignMinors(roster);

        return roster;
    }

    private void assignToDisabledList(Roster r, Iterable ps) {
        Iterator i$ = ps.iterator();
        do {
            if (!i$.hasNext()) {
                break;
            }
            Player p = (Player) i$.next();
            if (((Boolean) p.getOn40Man().or(Boolean.TRUE)).booleanValue()) {
                r.assign(com.ljs.scratch.ootp.roster.Roster.Status.DL,
                    new Player[]{
                    p
                });
            }
        } while (true);
    }

    public void assignMinors(Roster roster) {

        List<Status> remainingLevels = Lists.newArrayList(this.remainingLevels);

        while (!remainingLevels.isEmpty()) {
            ImmutableSet<Player> availableHitters =
                Selections.onlyHitters(roster.getUnassigned());

            ImmutableSet<Player> availablePitchers =
                Selections.onlyPitchers(roster.getUnassigned());

            int hittersSize =
                ((availableHitters.size() + remainingLevels.size()) - 1) /
                remainingLevels.size();
            int pitchersSize = ((availablePitchers.size() + remainingLevels
                .size()) - 1) / remainingLevels.size();

            Multiset<Slot> slots = HashMultiset.create();

            for (Slot s : Slot.values()) {
                int toAdd = ((Slot.countPlayersWithPrimary(roster
                    .getUnassigned(), s) + remainingLevels.size()) - 1) /
                    remainingLevels.size();
                for (int i = 0; i < toAdd; i++) {
                    slots.add(s);
                }

            }

            Status level = remainingLevels.get(0);

            roster.assign(
                level,
                SlotSelection
                    .builder()
                    .slots(slots)
                    .size(hittersSize)
                    .ordering(hitterSelectionFactory.byOverall())
                    .build()
                    .select(ImmutableSet.<Player>of(), availableHitters)
                    .values());

            roster.assign(
                level,
                SlotSelection
                    .builder()
                    .slots(slots)
                    .size(pitchersSize)
                    .ordering(pitcherSelectionFactory.byOverall())
                    .build()
                    .select(ImmutableSet.<Player>of(), availablePitchers)
                    .values());

            remainingLevels.remove(level);
        }

    }

    private Iterable<Player> getForced(Changes changes) {
        Set<Player> forced = Sets.newHashSet();

        for (Player p : changes.get(Changes.ChangeType.FORCE_ML)) {
            forced.add(p);
        }

        if (previous != null) {
            for (Player p : previous.getPlayers(Status.ML)) {
                if (team.containsPlayer(p)
                    && p.getOutOfOptions().or(Boolean.FALSE)
                    && !p.getClearedWaivers().or(Boolean.FALSE)
                    && !Iterables.contains(team.getInjuries(), p)) {

                    forced.add(p);
                }
            }
        }
        return forced;
    }

    public void printBattingSelectionTable(OutputStream out, Changes changes) {
        printBattingSelectionTable(new PrintWriter(out), changes);
    }

    public void printBattingSelectionTable(PrintWriter w, Changes changes) {
        w.println();
        Roster roster = select(Mode.REGULAR_SEASON, changes);
        TeamStats<BattingStats> batting = predictions.getAllBatting();

        for (Player p :
            hitterSelectionFactory.byOverall().sortedCopy(Selections.onlyHitters(batting.getPlayers()))) {

            w.println(
                String.format(
                    "%-2s %-15s%s %3s %2d | %14s %3d | %14s %3d | %3d | %8s | %s ",
                    p.getPosition(),
                    p.getShortName(),
                    p.getRosterStatus(),
                    roster.getStatus(p) != null ? roster.getStatus(p) : "",
                    p.getAge(),
                    batting.getSplits(p).getVsLeft().getSlashLine(),
                    batting.getSplits(p).getVsLeft().getWobaPlus(),
                    batting.getSplits(p).getVsRight().getSlashLine(),
                    batting.getSplits(p).getVsRight().getWobaPlus(),
                    batting.getOverall(p).getWobaPlus(),
                    p.getDefensiveRatings().getPositionScores(),
                    Joiner.on(',').join(Slot.getPlayerSlots(p))));

        }

        w.flush();
    }

    public void printPitchingSelectionTable(OutputStream out, Changes changes) {
        printPitchingSelectionTable(new PrintWriter(out), changes);
    }

    public void printPitchingSelectionTable(PrintWriter w, Changes changes) {
        w.println();
        Roster roster = select(Mode.REGULAR_SEASON, changes);
        TeamStats<PitchingStats> pitching = predictions.getAllPitching();
        PitcherOverall method = predictions.getPitcherOverall();

        for (Player p : pitcherSelectionFactory
            .byOverall()
            .sortedCopy(Selections.onlyPitchers(pitching.getPlayers()))) {

            w.println(
                String.format(
                    "%-2s %-15s%s %3s %2d | %3d %3d | %3d %3s | %5.2f | %s",
                    p.getPosition(),
                    p.getShortName(),
                    p.getRosterStatus(),
                    roster.getStatus(p) == null ? "" : roster.getStatus(p),
                    Integer.valueOf(p.getAge()),
                    method.getPlus(pitching.getSplits(p).getVsLeft()),
                    method.getPlus(pitching.getSplits(p).getVsRight()),
                    method.getPlus(pitching.getOverall(p)),
                    p.getPosition().equals("MR")
                        ? (int) (PlayerValue.MR_CONSTANT * method.getPlus(pitching.getOverall(p)))
                        : "",
                    method.getEraEstimate(pitching.getOverall(p)),
                    Joiner.on(',').join(Slot.getPlayerSlots(p))
                ));
        }

        w.flush();
    }

    public static RosterSelection ootpx(Team team, BattingRegression batting,
        PitchingRegression pitching, Function<Player, Integer> value) {

        RosterSelection selection = new RosterSelection(
            team,
            Predictions
                .predict(team)
                .using(batting, pitching, PitcherOverall.FIP),
            value);
        selection.batting = batting;
        selection.pitching = pitching;

        selection.remainingLevels = Lists.newArrayList(Status.AAA, Status.AA, Status.A, Status.SA, Status.R);

        return selection;
    }

    public static RosterSelection ootp6(Team team, BattingRegression batting,
        PitchingRegression pitching, Function<Player, Integer> value) {

        RosterSelection selection = new RosterSelection(
            team,
            Predictions
                .predict(team)
                .using(batting, pitching, PitcherOverall.FIP),
            value);
        selection.batting = batting;
        selection.pitching = pitching;

        return selection;
    }

    public static RosterSelection ootp5(Team team, BattingRegression batting,
        PitchingRegression pitching, Function<Player, Integer> value) {

        RosterSelection selection = new RosterSelection(
            team,
            Predictions
                .predict(team)
                .using(batting, pitching, PitcherOverall.WOBA_AGAINST),
            value);

        selection.batting = batting;
        selection.pitching = pitching;

        return selection;
    }

}
