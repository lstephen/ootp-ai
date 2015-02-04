// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
package com.ljs.ootp.ai.selection.rotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.site.SiteHolder;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.SplitStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public final class Rotation {

    public static enum Role { SP, MR, SU, CL, NONE };

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

    private static enum BullpenOption { ENDURANCE, CLUTCH, CONSISTENCY }

    private Double scoreBullpen(TeamStats<PitchingStats> predictions, PitcherOverall overall) {
      Double mrs = score(get(Role.MR), predictions, overall, EnumSet.of(BullpenOption.ENDURANCE));
      Double sus = score(get(Role.SU), predictions, overall, EnumSet.of(BullpenOption.CONSISTENCY));
      Double cls = score(get(Role.CL), predictions, overall, EnumSet.of(BullpenOption.CLUTCH, BullpenOption.CONSISTENCY));

      return (mrs + (14.0 / 9.0) * sus + (14.0 / 5.0) * cls) / 3.0;
    }

    private Double score(
        ImmutableList<Player> players,
        TeamStats<PitchingStats> predictions,
        PitcherOverall overall,
        EnumSet<BullpenOption> options) {

      Double score = 0.0;
      Double vsL = 0.0;
      Double vsR = 0.0;

      Integer factor = 5;

      for (Player p : players) {
        SplitStats<PitchingStats> stats = predictions.getSplits(p);

        Double endFactor = 0.0;

        if (options.contains(BullpenOption.ENDURANCE)) {
          Integer end = p.getPitchingRatings().getVsRight().getEndurance();
          endFactor = (1000.0 - Math.pow(10 - end, 3)) / 2000.0;
        }

        Double clutchFactor = 0.0;

        if (options.contains(BullpenOption.CLUTCH) && p.getClutch().isPresent()) {
          switch (p.getClutch().get()) {
            case SUFFERS:
              clutchFactor = -0.25;
              break;
            case NORMAL:
              clutchFactor = 0.0;
              break;
            case GREAT:
              clutchFactor = 0.25;
              break;
            default:
              throw new IllegalArgumentException();
          }
        }

        Double consistencyFactor = 0.0;

        if (options.contains(BullpenOption.CONSISTENCY) && p.getConsistency().isPresent()) {
          switch (p.getConsistency().get()) {
            case VERY_INCONSISTENT:
              consistencyFactor = -0.25;
              break;
            case AVERAGE:
              consistencyFactor = 0.0;
              break;
            case GOOD:
              consistencyFactor = 0.25;
              break;
            default:
              throw new IllegalArgumentException();
          }
        }


        Double f = factor.doubleValue() + endFactor + clutchFactor + consistencyFactor;

        score += f * overall.getPlus(stats.getOverall());
        vsL += f * overall.getPlus(stats.getVsLeft());
        vsR += f * overall.getPlus(stats.getVsRight());

        if (factor > 1) {
            factor--;
        }
      }

      Double maxBalance = Math.pow((vsL + vsR) / 2.0, 2.0);
      Double actBalance = vsL * vsR;

      Double balanceFactor = Math.pow(actBalance / maxBalance, players.size());

      return score * balanceFactor;
    }

    public Boolean isValid() {
      List<Player> all = Lists.newArrayList();
      for (Role r : Role.values()) {
        if (rotation.get(r) != null) {
          all.addAll(rotation.get(r));
        }
      }

      if (getAll().size() != all.size()) {
        return false;
      }

      if (rotation.containsKey(Role.MR) && rotation.get(Role.MR).size() > 4) { return false; }
      if (rotation.containsKey(Role.SU) && rotation.get(Role.SU).size() > 2) { return false; }
      if (rotation.containsKey(Role.CL) && rotation.get(Role.CL).size() > 1) { return false; }

      if (SiteHolder.get().getName().equals("CBL")) {
          for (Player p : rotation.get(Role.SP)) {
              if (!p.getSlots().contains(Slot.SP)) {
                  return false;
              }
          }
      }

      return true;
    }

    public Rotation substitute(Player in, Player out) {
        Map<Role, ImmutableList<Player>> newRotation = Maps.newHashMap(rotation);

        for (Role r : Role.values()) {
            if (!rotation.containsKey(r)) {
              continue;
            }

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

    public Rotation move(Player p, Role r, int idx) {
      Map<Role, ImmutableList<Player>> newRotation = Maps.newHashMap(remove(p).rotation);

      if (newRotation.containsKey(r)) {
        List<Player> ps = Lists.newArrayList(newRotation.get(r));
        if (idx > ps.size()) {
          ps.add(p);
        } else {
          ps.add(idx, p);
        }
        newRotation.put(r, ImmutableList.copyOf(ps));
      } else {
        newRotation.put(r, ImmutableList.of(p));
      }

      Rotation next = create(newRotation);
      //print(System.out);
      //System.out.println(p.getShortName() + "/" + r + "/" + idx);
      //next.print(System.out);

      return next;
    }

    private Rotation remove(Player p) {
      Map<Role, ImmutableList<Player>> newRotation = Maps.newHashMap(rotation);

      for (Role r : newRotation.keySet()) {
        if (newRotation.get(r).contains(p)) {
          List<Player> ps = Lists.newArrayList(rotation.get(r));

          ps.remove(p);

          newRotation.put(r, ImmutableList.copyOf(ps));
        }
      }

      return create(newRotation);
    }



    public Rotation swap(Player lhs, Player rhs) {
        Map<Role, ImmutableList<Player>> newRotation = Maps.newHashMap(rotation);

        for (Role r : Role.values()) {
            if (!rotation.containsKey(r)) {
              continue;
            }

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

    public ImmutableList<Player> get(Role... roles) {
        List<Player> result = Lists.newArrayList();

        for (Role r : roles) {
          if (rotation.containsKey(r)) {
            result.addAll(rotation.get(r));
          }
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
        return get(Role.MR, Role.SU, Role.CL, Role.NONE);
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
        ImmutableList<Player> sus = get(Role.SU);
        ImmutableList<Player> cls = get(Role.CL);
        ImmutableList<Player> none = get(Role.NONE);

        for(int i = 0; i < 5; i++)
            w.println(String.format("%d. %-15s %-15s %-15s %-15s %-15s %-15s", new Object[] {
                Integer.valueOf(i + 1),
                i >= sps.size() ? "" : sps.get(i).getShortName(),
                "",
                mrs != null && i >= mrs.size() ? "" : mrs.get(i).getShortName(),
                sus != null && i >= sus.size() ? "" : sus.get(i).getShortName(),
                cls != null && i >= cls.size() ? "" : cls.get(i).getShortName(),
                none != null && i >= none.size() ? "" : none.get(i).getShortName()
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
