// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
package com.ljs.ootp.ai.selection.rotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.SplitStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Rotation {

    private static enum Role { SP, MR, NONE };

    private final ImmutableMap<Role, ImmutableList<Player>> rotation;

    private Rotation(Map<Role, ImmutableList<Player>> rotation) {
        this.rotation = ImmutableMap.copyOf(rotation);
    }

    public Double score(TeamStats<PitchingStats> predictions, PitcherOverall overall) {
        return scoreRotation(predictions, overall)
            + scoreBullpen(predictions, overall);
    }

    private Double scoreRotation(TeamStats<PitchingStats> predictions, PitcherOverall overall) {
        Double score = 0.0;
        Integer spFactor = 10;

        for (Player p : get(Role.SP)) {
            Integer end = p.getPitchingRatings().getVsRight().getEndurance();
            SplitStats<PitchingStats> stats = predictions.getSplits(p);

            Double endFactor = (1000.0 - Math.pow(10 - end, 3)) / 1000.0;

            score += spFactor * endFactor * overall.getPlus(stats.getOverall());

            spFactor--;
        }

        return score;
    }

    private Double scoreBullpen(TeamStats<PitchingStats> predictions, PitcherOverall overall) {
        Integer score = 0;
        Integer spFactor = 5;

        Integer vsL = 0;
        Integer vsR = 0;

        ImmutableList<Player> bp = get(Role.MR, Role.NONE);

        for (Player p : bp) {
            SplitStats<PitchingStats> stats = predictions.getSplits(p);

            score += spFactor * overall.getPlus(stats.getOverall());
            vsL += spFactor * overall.getPlus(stats.getVsLeft());
            vsR += spFactor * overall.getPlus(stats.getVsRight());

            if (spFactor > 1) {
                spFactor--;
            }
        }

        Double maxBalance = Math.pow((vsL + vsR) / 2.0, 2.0);
        Double actBalance = (double) (vsL * vsR);

        Double balanceFactor = Math.pow(actBalance / maxBalance, bp.size());

        return score * balanceFactor;
    }

    public Boolean isValid() {
        if (getAll().size() !=
            ImmutableList
                .copyOf(
                    Iterables.concat(
                        rotation.get(Role.SP),
                        rotation.get(Role.MR),
                        rotation.get(Role.NONE)))
                .size()) {

            return false;
        }

        return true;
    }

    public Rotation substitute(Player in, Player out) {
        Map<Role, ImmutableList<Player>> newRotation = Maps.newHashMap(rotation);

        for (Role r : Role.values()) {
            List<Player> ps = Lists.newArrayList(rotation.get(r));

            if (ps.contains(out)) {
                int index = ps.indexOf(out);
                ps.add(index, in);
                ps.remove(out);
            }

            newRotation.put(r, ImmutableList.copyOf(ps));
        }

        return create(newRotation);
    }

    public Rotation swap(Player lhs, Player rhs) {
        Map<Role, ImmutableList<Player>> newRotation = Maps.newHashMap(rotation);

        for (Role r : Role.values()) {
            List<Player> ps = Lists.newArrayList(rotation.get(r));

            int rindex = ps.indexOf(rhs);
            int lindex = ps.indexOf(lhs);

            if (rindex >= 0 && lindex >= 0) {
                Collections.swap(ps, lindex, rindex);
            } else {

                if (rindex >= 0) {
                    ps.remove(rindex);
                    ps.add(rindex, lhs);
                }

                if (lindex >= 0) {
                    ps.remove(lindex);
                    ps.add(lindex, rhs);
                }
            }

            newRotation.put(r, ImmutableList.copyOf(ps));
        }

        return create(newRotation);
    }

    private ImmutableList<Player> get(Role... roles) {
        List<Player> result = Lists.newArrayList();

        for (Role r : roles) {
            result.addAll(rotation.get(r));
        }

        return ImmutableList.copyOf(result);
    }

    public ImmutableSet<Player> getAll() {
        return ImmutableSet.copyOf(get(Role.values()));
    }

    public ImmutableList<Player> getStarters() {
        return get(Role.SP);
    }

    public ImmutableList<Player> getNonStarters() {
        return get(Role.MR, Role.NONE);
    }

    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {
        w.println();
        w.println(String.format("   %-15s %-15s %-15s %-15s %-15s %-15s", new Object[] {
            "SP", "LR", "MR", "SU", "CL", "NONE"
        }));

        ImmutableList<Player> sps = get(Role.SP);
        ImmutableList<Player> mrs = get(Role.MR);
        ImmutableList<Player> none = get(Role.NONE);

        for(int i = 0; i < 5; i++)
            w.println(String.format("%d. %-15s %-15s %-15s %-15s %-15s %-15s", new Object[] {
                Integer.valueOf(i + 1),
                i >= sps.size() ? "" : sps.get(i).getShortName(),
                "",
                i >= mrs.size() ? "" : mrs.get(i).getShortName(),
                "",
                "",
                i >= none.size() ? "" : none.get(i).getShortName()
            }));

        w.flush();
    }

    public static final Rotation create(Iterable<Player> sps, Iterable<Player> mrs, Iterable<Player> rest) {
        return create(ImmutableMap.of(
            Role.SP, ImmutableList.copyOf(sps),
            Role.MR, ImmutableList.copyOf(mrs),
            Role.NONE, ImmutableList.copyOf(rest)));
    }

    private static final Rotation create(Map<Role, ImmutableList<Player>> rotation) {
        return new Rotation(rotation);
    }

}
