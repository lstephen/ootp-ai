// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RosterSelection.java

package com.ljs.scratch.ootp.selection;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.Files;
import com.ljs.scratch.ootp.core.*;
import com.ljs.scratch.ootp.regression.BattingRegression;
import com.ljs.scratch.ootp.regression.PitchingRegression;
import com.ljs.scratch.ootp.selection.pitcher.PitcherSelection;
import com.ljs.scratch.ootp.stats.*;
import java.io.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

// Referenced classes of package com.ljs.scratch.ootp.selection:
//            Slot, Selections, Selection, Mode, 
//            SlotSelection

public final class RosterSelection
{

    private RosterSelection(Team team, TeamStats batting, TeamStats pitching, PitcherOverall pitcherSelectionMethod)
    {
        this.team = team;
        this.batting = batting;
        this.pitching = pitching;
        hitterSelectionFactory = HitterSelectionFactory.using(batting);
        pitchers = new PitcherSelection(pitching, pitcherSelectionMethod);
    }

    public void remove(Player p)
    {
        team.remove(p);
    }

    public void setPrevious(Roster previous)
    {
        this.previous = previous;
    }

    public Roster select(Mode mode, File changes)
    {
        return null;
    }

    private Roster select(File changes, Selection hitting, Selection pitching)
    {
        Roster roster = new Roster(team);
        Iterable forced = getForced(changes);
        assignToDisabledList(roster, team.getInjuries());
        roster.assign(com.ljs.scratch.ootp.core.Roster.Status.ML, hitting.select(Selections.onlyHitters(forced), Selections.onlyHitters(Selections.onlyOn40Man(roster.getUnassigned()))).values());
        roster.assign(com.ljs.scratch.ootp.core.Roster.Status.ML, pitching.select(Selections.onlyPitchers(forced), Selections.onlyPitchers(Selections.onlyOn40Man(roster.getUnassigned()))).values());
        assignMinors(roster);
        return roster;
    }

    public Roster select(File changes)
    {
        Roster roster = new Roster(team);
        Iterable forced = getForced(changes);
        assignToDisabledList(roster, team.getInjuries());
        roster.assign(com.ljs.scratch.ootp.core.Roster.Status.ML, hitterSelectionFactory.create(Mode.REGULAR_SEASON).select(Selections.onlyHitters(forced), Selections.onlyHitters(Selections.onlyOn40Man(roster.getUnassigned()))).values());
        roster.assign(com.ljs.scratch.ootp.core.Roster.Status.ML, pitchers.selectMajorLeagueSquad(Selections.onlyPitchers(forced), Selections.onlyPitchers(Selections.onlyOn40Man(roster.getUnassigned()))).values());
        assignMinors(roster);
        return roster;
    }

    public Roster selectedExpanded(File changes)
    {
        Roster roster = new Roster(team);
        Iterable forced = getForced(changes);
        assignToDisabledList(roster, team.getInjuries());
        roster.assign(com.ljs.scratch.ootp.core.Roster.Status.ML, hitterSelectionFactory.create(Mode.EXPANDED).select(Selections.onlyHitters(forced), Selections.onlyHitters(Selections.onlyOn40Man(roster.getUnassigned()))).values());
        roster.assign(com.ljs.scratch.ootp.core.Roster.Status.ML, pitchers.selectExpandedMajorLeagueSquad(Selections.onlyPitchers(forced), Selections.onlyPitchers(Selections.onlyOn40Man(roster.getUnassigned()))).values());
        assignMinors(roster);
        return roster;
    }

    private void assignToDisabledList(Roster r, Iterable ps)
    {
        Iterator i$ = ps.iterator();
        do
        {
            if(!i$.hasNext())
                break;
            Player p = (Player)i$.next();
            if(((Boolean)p.getOn40Man().or(Boolean.TRUE)).booleanValue())
                r.assign(com.ljs.scratch.ootp.core.Roster.Status.DL, new Player[] {
                    p
                });
        } while(true);
    }

    public void assignMinors(Roster roster)
    {
        com.ljs.scratch.ootp.core.Roster.Status level;
        for(List remainingLevels = Lists.newArrayList(new com.ljs.scratch.ootp.core.Roster.Status[] {
    com.ljs.scratch.ootp.core.Roster.Status.AAA, com.ljs.scratch.ootp.core.Roster.Status.AA, com.ljs.scratch.ootp.core.Roster.Status.A
}); !remainingLevels.isEmpty(); remainingLevels.remove(level))
        {
            ImmutableSet availableHitters = Selections.onlyHitters(roster.getUnassigned());
            ImmutableSet availablePitchers = Selections.onlyPitchers(roster.getUnassigned());
            int hittersSize = ((availableHitters.size() + remainingLevels.size()) - 1) / remainingLevels.size();
            int pitchersSize = ((availablePitchers.size() + remainingLevels.size()) - 1) / remainingLevels.size();
            Multiset slots = HashMultiset.create();
            Slot arr$[] = Slot.values();
            int len$ = arr$.length;
            for(int i$ = 0; i$ < len$; i$++)
            {
                Slot s = arr$[i$];
                int toAdd = ((Slot.countPlayersWithPrimary(roster.getUnassigned(), s) + remainingLevels.size()) - 1) / remainingLevels.size();
                for(int i = 0; i < toAdd; i++)
                    slots.add(s);

            }

            level = (com.ljs.scratch.ootp.core.Roster.Status)remainingLevels.get(0);
            roster.assign(
                level,
                SlotSelection
                    .builder()
                    .slots(slots)
                    .size(Integer.valueOf(hittersSize))
                    .ordering(hitterSelectionFactory.byOverall())
                    .build()
                    .select(ImmutableSet.<Player>of(), availableHitters)
                    .values());

            roster.assign(level, pitchers.select(ImmutableSet.<Player>of(), availablePitchers, slots, pitchersSize).values());
        }

    }

    private Iterable getForced(File changes)
    {
        Set forced = Sets.newHashSet();
        if(changes != null && changes.exists())
            try
            {
                Iterator i$ = Files.readLines(changes, Charsets.UTF_8).iterator();
                do
                {
                    if(!i$.hasNext())
                        break;
                    String s = (String)i$.next();
                    PlayerId id = new PlayerId(StringUtils.substringAfter(s, ","));
                    if(s.charAt(0) == 'm' && team.containsPlayer(id))
                        forced.add(team.getPlayer(id));
                } while(true);
            }
            catch(IOException e)
            {
                throw Throwables.propagate(e);
            }
        if(previous != null)
        {
            Iterator i$ = previous.getPlayers(com.ljs.scratch.ootp.core.Roster.Status.ML).iterator();
            do
            {
                if(!i$.hasNext())
                    break;
                Player p = (Player)i$.next();
                if(((Boolean)p.getOutOfOptions().or(Boolean.FALSE)).booleanValue() && !((Boolean)p.getClearedWaivers().or(Boolean.TRUE)).booleanValue())
                    forced.add(p);
            } while(true);
        }
        return forced;
    }

    public void printBattingSelectionTable(OutputStream out, File changes)
    {
        printBattingSelectionTable(new PrintWriter(out), changes);
    }

    public void printBattingSelectionTable(PrintWriter w, File changes)
    {
        w.println();
        Roster roster = select(changes);
        Player p;
        for(Iterator i$ = hitterSelectionFactory.byOverall().sortedCopy(Selections.onlyHitters(batting.getPlayers())).iterator(); i$.hasNext(); w.println(String.format("%-2s %-15s%s %3s %2d | %15s %3d | %15s %3d | %3d | %8s | %s", new Object[] {
    p.getPosition(), p.getShortName(), p.getRosterStatus(), roster.getStatus(p) != null ? roster.getStatus(p) : "", Integer.valueOf(p.getAge()), ((BattingStats)batting.getSplits(p).getVsLeft()).getSlashLine(), Integer.valueOf(((BattingStats)batting.getSplits(p).getVsLeft()).getWobaPlus()), ((BattingStats)batting.getSplits(p).getVsRight()).getSlashLine(), Integer.valueOf(((BattingStats)batting.getSplits(p).getVsRight()).getWobaPlus()), Integer.valueOf(((BattingStats)batting.getOverall(p)).getWobaPlus()), 
    p.getDefensiveRatings().getPositionScores(), Joiner.on(',').join(Slot.getPlayerSlots(p))
})))
            p = (Player)i$.next();

        w.flush();
    }

    public void printPitchingSelectionTable(OutputStream out, File changes)
    {
        printPitchingSelectionTable(new PrintWriter(out), changes);
    }

    public void printPitchingSelectionTable(PrintWriter w, File changes)
    {
        w.println();
        Roster roster = select(changes);
        Player p;
        for(Iterator i$ = pitchers.byOverall().sortedCopy(Selections.onlyPitchers(pitching.getPlayers())).iterator(); i$.hasNext(); w.println(String.format("%-2s %-15s%s %3s %2d | %3d %3d | %3d %3s | %s", new Object[] {
    p.getPosition(), p.getShortName(), p.getRosterStatus(), roster.getStatus(p) != null ? roster.getStatus(p) : "", Integer.valueOf(p.getAge()), pitchers.getMethod().getPlus((PitchingStats)pitching.getSplits(p).getVsLeft()), pitchers.getMethod().getPlus((PitchingStats)pitching.getSplits(p).getVsRight()), pitchers.getMethod().getPlus((PitchingStats)pitching.getOverall(p)), p.getPosition().equals("MR") ? Integer.toString((int)(0.86499999999999999D * (double)pitchers.getMethod().getPlus((PitchingStats)pitching.getOverall(p)).intValue())) : "", Joiner.on(',').join(Slot.getPlayerSlots(p))
})))
            p = (Player)i$.next();

        w.flush();
    }

    public static RosterSelection ootp6(Team team, BattingRegression batting, PitchingRegression pitching)
    {
        return new RosterSelection(team, batting.predict(team), pitching.predict(team), PitcherOverall.FIP);
    }

    public static RosterSelection ootp5(Team team, BattingRegression batting, PitchingRegression pitching)
    {
        return new RosterSelection(team, batting.predict(team), pitching.predict(team), PitcherOverall.WOBA_AGAINST);
    }

    private final Team team;
    private final HitterSelectionFactory hitterSelectionFactory;
    private final PitcherSelection pitchers;
    private final TeamStats batting;
    private final TeamStats pitching;
    private Roster previous;
}
