package com.ljs.scratch.ootp.selection;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.config.Changes;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.Roster;
import com.ljs.scratch.ootp.core.Roster.Status;
import com.ljs.scratch.ootp.core.Team;
import com.ljs.scratch.ootp.regression.BattingRegression;
import com.ljs.scratch.ootp.regression.PitchingRegression;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.TeamStats;
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

    private Roster previous;

    private RosterSelection(
        Team team, Predictions predictions) {

        this.team = team;
        this.predictions = predictions;

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
        return Selections.onlyHitters(Selections.onlyOn40Man(roster.getUnassigned()));
    }

    private Iterable<Player> getAvailablePitchers(Roster roster) {
        return Selections.onlyPitchers(Selections.onlyOn40Man(roster.getUnassigned()));
    }

    public Roster select(Mode mode, Changes changes) {
        return select(
            changes,
            hitterSelectionFactory.create(mode),
            pitcherSelectionFactory.create(mode));
    }

    private Roster select(Changes changes, Selection hitting, Selection pitching) {
        Roster roster = new Roster(team);
        Iterable forced = getForced(changes);
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
                r.assign(com.ljs.scratch.ootp.core.Roster.Status.DL,
                    new Player[]{
                    p
                });
            }
        } while (true);
    }

    public void assignMinors(Roster roster) {

        com.ljs.scratch.ootp.core.Roster.Status level;
        for (List<Status> remainingLevels = Lists.newArrayList(
            new com.ljs.scratch.ootp.core.Roster.Status[]{
            com.ljs.scratch.ootp.core.Roster.Status.AAA, com.ljs.scratch.ootp.core.Roster.Status.AA, com.ljs.scratch.ootp.core.Roster.Status.A
        }); !remainingLevels.isEmpty(); remainingLevels.remove(level)) {
            ImmutableSet<Player> availableHitters = Selections.onlyHitters(roster
                .getUnassigned());
            ImmutableSet<Player> availablePitchers = Selections.onlyPitchers(roster
                .getUnassigned());
            int hittersSize =
                ((availableHitters.size() + remainingLevels.size()) - 1) /
                remainingLevels.size();
            int pitchersSize = ((availablePitchers.size() + remainingLevels
                .size()) - 1) / remainingLevels.size();
            Multiset slots = HashMultiset.create();
            Slot arr$[] = Slot.values();
            int len$ = arr$.length;
            for (int i$ = 0; i$ < len$; i$++) {
                Slot s = arr$[i$];
                int toAdd = ((Slot.countPlayersWithPrimary(roster
                    .getUnassigned(), s) + remainingLevels.size()) - 1) /
                    remainingLevels.size();
                for (int i = 0; i < toAdd; i++) {
                    slots.add(s);
                }

            }

            level = remainingLevels.get(0);

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
        }

    }

    private Iterable<Player> getForced(Changes changes) {
        Set<Player> forced = Sets.newHashSet();

        for (Player p : changes.get(Changes.ChangeType.FORCE_ML)) {
            forced.add(p);
        }

        if (previous != null) {
            Iterator i$ = previous.getPlayers(
                com.ljs.scratch.ootp.core.Roster.Status.ML).iterator();
            do {
                if (!i$.hasNext()) {
                    break;
                }
                Player p = (Player) i$.next();
                if (((Boolean) p.getOutOfOptions().or(Boolean.FALSE))
                    .booleanValue() && !((Boolean) p.getClearedWaivers().or(
                    Boolean.TRUE)).booleanValue()) {
                    forced.add(p);
                }
            } while (true);
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

        Player p;
        for (Iterator i$ = hitterSelectionFactory.byOverall().sortedCopy(
            Selections.onlyHitters(batting.getPlayers())).iterator(); i$
            .hasNext(); w.println(String.format(
            "%-2s %-15s%s %3s %2d | %15s %3d | %15s %3d | %3d | %8s | %s",
            new Object[]{
            p.getPosition(), p.getShortName(), p.getRosterStatus(), roster
            .getStatus(p) != null ? roster.getStatus(p) : "", Integer.valueOf(p
            .getAge()), ((BattingStats) batting.getSplits(p).getVsLeft())
            .getSlashLine(), Integer.valueOf(((BattingStats) batting
            .getSplits(p).getVsLeft()).getWobaPlus()), ((BattingStats) batting
            .getSplits(p).getVsRight()).getSlashLine(), Integer.valueOf(
            ((BattingStats) batting.getSplits(p).getVsRight()).getWobaPlus()), Integer
            .valueOf(((BattingStats) batting.getOverall(p)).getWobaPlus()),
            p.getDefensiveRatings().getPositionScores(), Joiner.on(',').join(
            Slot.getPlayerSlots(p))
        }))) {
            p = (Player) i$.next();
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

        Player p;
        for (Iterator i$ = pitcherSelectionFactory.byOverall().sortedCopy(Selections
            .onlyPitchers(pitching.getPlayers())).iterator(); i$.hasNext(); w
            .println(String.format(
            "%-2s %-15s%s %3s %2d | %3d %3d | %3d %3s | %s", new Object[]{
            p.getPosition(), p.getShortName(), p.getRosterStatus(), roster
            .getStatus(p) != null ? roster.getStatus(p) : "", Integer.valueOf(p
            .getAge()), method.getPlus((PitchingStats) pitching
            .getSplits(p).getVsLeft()), method.getPlus(
            (PitchingStats) pitching.getSplits(p).getVsRight()), 
            method.getPlus((PitchingStats) pitching.getOverall(p)), p
            .getPosition().equals("MR") ? Integer.toString(
            (int) (0.86499999999999999D * (double) method.getPlus(
            (PitchingStats) pitching.getOverall(p)).intValue())) : "", Joiner
            .on(',').join(Slot.getPlayerSlots(p))
        }))) {
            p = (Player) i$.next();
        }

        w.flush();
    }

    public static RosterSelection ootp6(Team team, BattingRegression batting,
        PitchingRegression pitching) {

        return new RosterSelection(
            team,
            Predictions
                .predict(team)
                .using(batting, pitching, PitcherOverall.FIP));
    }

    public static RosterSelection ootp5(Team team, BattingRegression batting,
        PitchingRegression pitching) {

        return new RosterSelection(
            team,
            Predictions
                .predict(team)
                .using(batting, pitching, PitcherOverall.WOBA_AGAINST));
    }
    

}
