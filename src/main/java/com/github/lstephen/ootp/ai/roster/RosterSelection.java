package com.github.lstephen.ootp.ai.roster;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.github.lstephen.ootp.ai.io.Printables;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.ratings.Position;
import com.github.lstephen.ootp.ai.regression.BattingRegression;
import com.github.lstephen.ootp.ai.regression.PitchingRegression;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.roster.Changes.ChangeType;
import com.github.lstephen.ootp.ai.roster.Roster.Status;
import com.github.lstephen.ootp.ai.selection.BestStartersSelection;
import com.github.lstephen.ootp.ai.selection.Mode;
import com.github.lstephen.ootp.ai.selection.HitterSelectionFactory;
import com.github.lstephen.ootp.ai.selection.PitcherSelectionFactory;
import com.github.lstephen.ootp.ai.selection.Selection;
import com.github.lstephen.ootp.ai.selection.Selections;
import com.github.lstephen.ootp.ai.selection.Tiered;
import com.github.lstephen.ootp.ai.selection.lineup.Defense;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.PitcherOverall;
import com.github.lstephen.ootp.ai.stats.PitchingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;
import com.github.lstephen.ootp.ai.value.JavaAdapter;
import com.github.lstephen.ootp.ai.value.PlayerValue;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RosterSelection {

    private static final int MINIMUM_ML_ROSTER_SIZE = 25;

    private static final Double MR_CONSTANT = .865;

    private final Team team;

    private final PitcherSelectionFactory pitcherSelectionFactory;

    private Roster previous;

    private final BattingRegression batting;

    private final PitchingRegression pitching;

    private final Predictor predictor;

    private List<Status> remainingLevels =
        Lists.newArrayList(Status.AAA, Status.AA, Status.A);

    private FourtyManRoster fourtyManRoster;

    private RosterSelection(
        Team team, BattingRegression batting, PitchingRegression pitching) {

        this.team = team;
        this.batting = batting;
        this.pitching = pitching;

        this.predictor = new Predictor(team, batting, pitching);

        pitcherSelectionFactory = new PitcherSelectionFactory(predictor);
    }

    public void remove(Player p) {
        team.remove(p);
    }

    public void setPrevious(Roster previous) {
        this.previous = previous;
    }

    private Iterable<Player> getAvailableHitters(Roster roster, Mode mode) {
      if (mode == Mode.IDEAL) {
        return Sets.newHashSet(Selections.onlyHitters(roster.getUnassigned()));
      } else {
        Set<Player> available = Sets.newHashSet(
                Selections.onlyHitters(
                    Selections.onlyOn40Man(roster.getUnassigned())));

        available.removeAll(getToBeRemoved(available));

        return available;
      }
    }

    private Iterable<Player> getAvailablePitchers(Roster roster, Mode mode) {
      if (mode == Mode.IDEAL) {
        return Sets.newHashSet(Selections.onlyPitchers(roster.getUnassigned()));
      } else {
          Set<Player> available = Sets.newHashSet(Selections.onlyPitchers(Selections.onlyOn40Man(roster.getUnassigned())));
          available.removeAll(getToBeRemoved(available));
          return available;
      }
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
            Roster roster = Roster.create(previous);

            for (Player p : previous.getAllPlayers()) {
                if (!team.containsPlayer(p)) {
                    roster.release(p);
                }
            }

            fourtyManRoster =
                new FourtyManRoster(
                    team,
                    roster,
                    predictor);
        }
        return fourtyManRoster;
    }

    public Roster select(Mode mode) {
      return select(mode, Changes.empty());
    }

    public Roster select(Mode mode, Changes changes) {
        return select(
            mode,
            changes,
            pitcherSelectionFactory.create(mode));
    }

    private Roster select(Mode mode, Changes changes, Selection pitching) {
        Selection hitting = new HitterSelectionFactory(predictor).create(mode);

        Roster roster = Roster.create(team);
        Set<Player> forced = mode == Mode.IDEAL ? new HashSet<>() : Sets.newHashSet(getForced(changes));
        Set<Player> ml = Sets.newHashSet();

        for (Player p : changes.get(ChangeType.FORCE_ML)) {
            if (!previous.contains(p)) {
                previous.assign(Status.UNK, p);
            }
        }

        for (Player p : changes.get(ChangeType.FORCE_ML)) {
          if (changes.get(ChangeType.FORCE_FORCE_ML).contains(p)) {
            System.out.println("Really forced: " + p.getShortName());
            continue;
          }

          FourtyManRoster fourtyMan = getFourtyManRoster();
          fourtyMan.setChanges(changes);

          if (!fourtyMan.getDesired40ManRoster().contains(p)) {
              System.out.println("Not on desired 40 Man: " + p.getShortName());
              roster.release(p);
              forced.remove(p);
          }
        }

        if (mode != Mode.IDEAL) {
          assignToDisabledList(roster, team.getInjuries());
        }

        forced.removeAll(roster.getPlayers(Status.DL));

        ml.addAll(
            hitting.select(
                Selections.onlyHitters(forced),
                getAvailableHitters(roster, mode)).values());

        ml.addAll(
            pitching.select(
                Selections.onlyPitchers(forced),
                getAvailablePitchers(roster, mode)).values());

        while (ml.size() > mode.getMajorLeagueRosterLimit()
            && !Sets.difference(ml, forced).isEmpty()) {
            ml.remove(Ordering
                .natural()
                .onResultOf((Player p) -> JavaAdapter.overallValue(p, predictor).score())
                .min(Sets.difference(ml, forced)));
        }

        while (ml.size() > mode.getMajorLeagueRosterLimit()) {
            Player p = Ordering
                .natural()
                .onResultOf((Player ply) -> JavaAdapter.overallValue(ply, predictor).score())
                .min(ml);

            ml.remove(p);

            if (forced.contains(p)) {
                roster.release(p);
            }
        }

        while (ml.size() < Math.min(MINIMUM_ML_ROSTER_SIZE, mode.getMajorLeagueRosterLimit())) {
            Player p = Ordering
                .natural()
                .reverse()
                .onResultOf((Player ply) -> JavaAdapter.nowAbility(ply, predictor).score())
                .min(
                    Iterables.filter(
                      Iterables.concat(getAvailableHitters(roster, mode), getAvailablePitchers(roster, mode)),
                      pl -> !ml.contains(pl) && pl.getAge() > 25));

            ml.add(p);
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
                r.assign(com.github.lstephen.ootp.ai.roster.Roster.Status.DL, p);
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

            Status level = remainingLevels.get(0);

            roster.assign(
                level,
                new Tiered(Position.hitting(), predictor).takeAsJava(availableHitters, hittersSize));

            roster.assign(
                level,
                new Tiered(Position.pitching(), predictor).takeAsJava(availablePitchers, pitchersSize));

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

    public void printPitchingSelectionTable(OutputStream out, Roster roster, TeamStats<PitchingStats> stats) {
        Printables.print((w) -> printPitchingSelectionTable(w, roster, stats)).to(out);
    }

    public void printPitchingSelectionTable(PrintWriter w, Roster roster, TeamStats<PitchingStats> stats) {
        PlayerValue value = new PlayerValue(predictor, batting, pitching);
        w.println();
        Integer statusLength = Iterables.getFirst(roster.getAllPlayers(), null).getRosterStatus().length();

        w.format("%" + (25 + statusLength) + "s |  H/9  K/9 BB/9 HR/9 |%n", "");

        for (Player p : Ordering.natural().reverse().onResultOf((Player ply) -> value.getNowValue(ply))
            .sortedCopy(Selections.onlyPitchers(roster.getAllPlayers()))) {

            PitchingStats prediction = predictor.predictPitching(p).vsBoth();

            w.println(
                String.format(
                    "%-2s %-15s%s %3s %2d | %4.1f %4.1f %4.1f %4.1f | %3d %3s | %3d %3s | %2s || %3d |",
                    p.getPosition(),
                    p.getShortName(),
                    p.getRosterStatus(),
                    roster.getStatus(p) == null ? "" : roster.getStatus(p),
                    Integer.valueOf(p.getAge()),
                    prediction.getHitsPerNine(),
                    prediction.getStrikeoutsPerNine(),
                    prediction.getWalksPerNine(),
                    prediction.getHomeRunsPerNine(),
                    predictor.predictPitching(p).vsLeft().getBaseRunsPlus(),
                    stats.contains(p) ? Math.max(0, Math.min(stats.getSplits(p).getVsLeft().getBaseRunsPlus(), 999)) : "",
                    predictor.predictPitching(p).vsRight().getBaseRunsPlus(),
                    stats.contains(p) ? Math.max(0, Math.min(stats.getSplits(p).getVsRight().getBaseRunsPlus(), 999)) : "",
                    p.getIntangibles(),
                    value.getNowValue(p)
                ));
        }

        w.flush();
    }

    public static RosterSelection ootp6(Team team, BattingRegression batting,
        PitchingRegression pitching) {

        return new RosterSelection(
            team,
            batting,
            pitching);
    }

    public static RosterSelection ootp5(Team team, BattingRegression batting,
        PitchingRegression pitching) {

        return new RosterSelection(
            team,
            batting,
            pitching);
    }

}
