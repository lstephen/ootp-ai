package com.ljs.ootp.ai.roster;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.regression.BattingRegression;
import com.ljs.ootp.ai.regression.PitchingRegression;
import com.ljs.ootp.ai.regression.Predictions;
import com.ljs.ootp.ai.roster.Roster.Status;
import com.ljs.ootp.ai.selection.HitterSelectionFactory;
import com.ljs.ootp.ai.selection.Mode;
import com.ljs.ootp.ai.selection.PitcherSelectionFactory;
import com.ljs.ootp.ai.selection.Selection;
import com.ljs.ootp.ai.selection.Selections;
import com.ljs.ootp.ai.selection.SlotSelection;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import com.ljs.ootp.ai.value.TradeValue;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

public final class RosterSelection {

    private static final Double MR_CONSTANT = .865;

    private final Team team;

    private final HitterSelectionFactory hitterSelectionFactory;

    private final PitcherSelectionFactory pitcherSelectionFactory;

    private final Predictions predictions;

    private final TradeValue value;

    private Roster previous;

    private BattingRegression batting;

    private PitchingRegression pitching;

    private List<Status> remainingLevels =
        Lists.newArrayList(Status.AAA, Status.AA, Status.A);

    private FourtyManRoster fourtyManRoster;

    private RosterSelection(
        Team team, Predictions predictions, TradeValue value) {

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
            ImmutableSet<Player> toRemoveFromFourtyMan = null;

            for (Player p : available) {
                if (p.getClearedWaivers().or(Boolean.FALSE)) {
					if (toRemoveFromFourtyMan == null) {
						toRemoveFromFourtyMan =
							ImmutableSet.copyOf(getFourtyManRoster().getPlayersToRemove());
					}

                    if (toRemoveFromFourtyMan.contains(p)) {
						toRemove.add(p);
					}
                }
            }

        }
        return toRemove;
    }

    private FourtyManRoster getFourtyManRoster() {
        if (fourtyManRoster == null) {
            fourtyManRoster =
                new FourtyManRoster(
                    previous,
                    Predictions
                        .predict(previous.getAllPlayers())
                        .using(batting, pitching, predictions.getPitcherOverall()),
                value);
        }
        return fourtyManRoster;
    }

    public Roster select(Mode mode, Changes changes) {
        return select(
            mode,
            changes,
            hitterSelectionFactory.create(mode),
            pitcherSelectionFactory.create(mode));
    }

    private Roster select(Mode mode, Changes changes, Selection hitting, Selection pitching) {
        Roster roster = Roster.create(team);
        Set<Player> forced = Sets.newHashSet(getForced(changes));
        Set<Player> ml = Sets.newHashSet();
        assignToDisabledList(roster, team.getInjuries());

        forced.removeAll(roster.getPlayers(Status.DL));

        ml.addAll(
            hitting.select(
                Selections.onlyHitters(forced),
                getAvailableHitters(roster)).values());

        ml.addAll(
            pitching.select(
                Selections.onlyPitchers(forced),
                getAvailablePitchers(roster)).values());

        while (ml.size() > mode.getMajorLeagueRosterLimit()) {
            ml.remove(Ordering
                .natural()
                .onResultOf(value.getTradeTargetValue())
                .min(Sets.difference(ml, forced)));
        }

        roster.assign(Roster.Status.ML, ml);
        assignMinors(roster);

        return roster;
    }

    private void assignToDisabledList(Roster r, Iterable<Player> ps) {
        for (Player p : ps) {
            boolean isOn40Man = p.getOn40Man().or(Boolean.TRUE);
            boolean isOnDl = previous != null && previous.getStatus(p) == Status.DL;

            if (isOn40Man || isOnDl) {
                r.assign(com.ljs.ootp.ai.roster.Roster.Status.DL, p);
            };
        }
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

    private ImmutableSet<Player> getForced(Changes changes) {
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
        return ImmutableSet.copyOf(forced);
    }

    public void printBattingSelectionTable(OutputStream out, Roster roster, TeamStats<BattingStats> stats) {
        printBattingSelectionTable(new PrintWriter(out), roster, stats);
    }

    public void printBattingSelectionTable(PrintWriter w, Roster roster, TeamStats<BattingStats> stats) {
        w.println();
        TeamStats<BattingStats> batting = predictions.getAllBatting();

        for (Player p :
            hitterSelectionFactory.byOverall().sortedCopy(Selections.onlyHitters(batting.getPlayers()))) {

            w.println(
                String.format(
                    "%-2s %-15s%s %3s %2d | %14s %3d %3s | %14s %3d %3s || %3d | %8s | %s ",
                    p.getPosition(),
                    p.getShortName(),
                    p.getRosterStatus(),
                    roster.getStatus(p) != null ? roster.getStatus(p) : "",
                    p.getAge(),
                    batting.getSplits(p).getVsLeft().getSlashLine(),
                    batting.getSplits(p).getVsLeft().getWobaPlus(),
                    stats.contains(p) ? stats.getSplits(p).getVsLeft().getWobaPlus() : "",
                    batting.getSplits(p).getVsRight().getSlashLine(),
                    batting.getSplits(p).getVsRight().getWobaPlus(),
                    stats.contains(p) ? stats.getSplits(p).getVsRight().getWobaPlus() : "",
                    batting.getOverall(p).getWobaPlus(),
                    p.getDefensiveRatings().getPositionScores(),
                    Joiner.on(',').join(Slot.getPlayerSlots(p))));
        }

        w.flush();
    }

    public void printPitchingSelectionTable(OutputStream out, Roster roster, TeamStats<PitchingStats> stats) {
        printPitchingSelectionTable(new PrintWriter(out), roster, stats);
    }

    public void printPitchingSelectionTable(PrintWriter w, Roster roster, TeamStats<PitchingStats> stats) {
        w.println();
        TeamStats<PitchingStats> pitching = predictions.getAllPitching();
        PitcherOverall method = predictions.getPitcherOverall();

        for (Player p : pitcherSelectionFactory
            .byOverall()
            .sortedCopy(Selections.onlyPitchers(pitching.getPlayers()))) {

            w.println(
                String.format(
                    "%-2s %-15s%s %3s %2d | %3d %3s | %3d %3s || %3d %3s | %5.2f | %s",
                    p.getPosition(),
                    p.getShortName(),
                    p.getRosterStatus(),
                    roster.getStatus(p) == null ? "" : roster.getStatus(p),
                    Integer.valueOf(p.getAge()),
                    method.getPlus(pitching.getSplits(p).getVsLeft()),
                    stats.contains(p) ? Math.min(method.getPlus(stats.getSplits(p).getVsLeft()), 999) : "",
                    method.getPlus(pitching.getSplits(p).getVsRight()),
                    stats.contains(p) ? Math.min(method.getPlus(stats.getSplits(p).getVsRight()), 999) : "",
                    method.getPlus(pitching.getOverall(p)),
                    p.getPosition().equals("MR")
                        ? (int) (MR_CONSTANT * method.getPlus(pitching.getOverall(p)))
                        : "",
                    method.getEraEstimate(pitching.getOverall(p)),
                    Joiner.on(',').join(Slot.getPlayerSlots(p))
                ));
        }

        w.flush();
    }

    public static RosterSelection ootpx(Team team, BattingRegression batting,
        PitchingRegression pitching, TradeValue value) {

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
        PitchingRegression pitching, TradeValue value) {

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
        PitchingRegression pitching, TradeValue value) {

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
