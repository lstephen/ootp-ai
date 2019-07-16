// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
package com.github.lstephen.ootp.ai.selection.rotation;

import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.regression.BattingPrediction;
import com.github.lstephen.ootp.ai.regression.PitchingPrediction;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.site.SiteHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

public final class Rotation implements Printable {

  public static enum Role {
    SP,
    LR,
    MR,
    SU,
    CL,
    NONE
  };

  private final ImmutableMap<Role, ImmutableList<Player>> rotation;

  private Rotation(Map<Role, ImmutableList<Player>> rotation) {
    this.rotation = ImmutableMap.copyOf(rotation);
  }

  public Double score(Predictor predictor) {
    return scoreRotation(predictor) + scoreBullpen(predictor);
  }

  private double scoreRotation(Predictor predictor) {
    double score = 0.0;
    int spFactor = 5;

    for (Player p : get(Role.SP)) {
      int end = p.getPitchingRatings().getVsRight().getEndurance();

      double endFactor = (1000.0 - Math.pow(10 - end, 3)) / 1000.0;

      score += spFactor * endFactor * predictor.predictPitching(p).overall();

      spFactor--;
    }

    return score;
  }

  private static enum BullpenOption {
    CLUTCH,
    ENDURANCE
  }

  private Double scoreBullpen(Predictor predictor) {
    Double lrs = score(get(Role.LR), predictor, EnumSet.of(BullpenOption.ENDURANCE));
    Double mrs = score(get(Role.MR), predictor, EnumSet.noneOf(BullpenOption.class));
    Double sus = score(get(Role.SU), predictor, EnumSet.noneOf(BullpenOption.class));
    Double cls = score(get(Role.CL), predictor, EnumSet.of(BullpenOption.CLUTCH));

    ToDoubleFunction<Player> getEndurance = p -> p.getPitchingRatings().getVsRight().getEndurance().doubleValue();

    double end = get(Role.LR).stream().mapToDouble(getEndurance).sum()
      + get(Role.MR).stream().mapToDouble(getEndurance).map(d -> d * 0.5).sum();

    double endFactor = (1000.0 - Math.pow(10 - Math.min(end, 10.0), 3)) / 1000.0;

    return endFactor * (0.7 * lrs + mrs + 1.5 * sus + 3.0 * cls) / 4.0;
  }

  private double score(
      ImmutableList<Player> players, Predictor predictor, EnumSet<BullpenOption> options) {

    double score = 0.0;
    double vsL = 0.0;
    double vsR = 0.0;

    int factor = 5;

    for (Player p : players) {
      PitchingPrediction stats = predictor.predictPitching(p);

      double clutchFactor = 0.0;

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

      double enduranceFactor = 0.865;

      if (options.contains(BullpenOption.ENDURANCE)) {
        enduranceFactor =
            (1000.0 - Math.pow(10 - p.getPitchingRatings().getVsRight().getEndurance(), 3))
                / 1000.0;
      }

      double f = factor + clutchFactor + (enduranceFactor - 0.865);

      score += f * stats.overall();
      vsL += f * stats.vsLeft().getBaseRunsPlus();
      vsR += f * stats.vsRight().getBaseRunsPlus();

      if (factor > 1) {
        factor--;
      }
    }

    double maxBalance = Math.pow((vsL + vsR) / 2.0, 2.0);
    double actBalance = vsL * vsR;

    double balanceFactor = Math.pow(actBalance / maxBalance, players.size());

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

    if (rotation.containsKey(Role.LR) && rotation.get(Role.LR).size() > 2) {
      return false;
    }
    if (rotation.containsKey(Role.MR) && rotation.get(Role.MR).size() > 4) {
      return false;
    }
    if (rotation.containsKey(Role.SU) && rotation.get(Role.SU).size() > 2) {
      return false;
    }
    if (rotation.containsKey(Role.CL) && rotation.get(Role.CL).size() > 1) {
      return false;
    }

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

    return next;
  }

  public Rotation remove(Player p) {
    Map<Role, ImmutableList<Player>> newRotation = Maps.newHashMap(rotation);

    for (Map.Entry<Role, ImmutableList<Player>> r : newRotation.entrySet()) {
      if (r.getValue().contains(p)) {
        List<Player> ps = Lists.newArrayList(rotation.get(r.getKey()));

        ps.remove(p);

        newRotation.put(r.getKey(), ImmutableList.copyOf(ps));
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
    return get(Role.LR, Role.MR, Role.SU, Role.CL, Role.NONE);
  }

  public BattingPrediction getPitcherHitting(Predictor predictor) {
    return getStarters().stream().map(predictor::predictBatting).reduce((l, r) -> l.add(r)).get();
  }

  public void print(PrintWriter w) {
    w.println();

    Stream.of(Role.values())
        .forEach(
            r -> {
              w.println(r);
              get(r).forEach(p -> w.println(p.getName()));
              w.println();
            });
  }

  public static final Rotation create(
      Iterable<Player> sps, Iterable<Player> mrs, Iterable<Player> rest) {
    return create(
        ImmutableMap.of(
            Role.SP, ImmutableList.copyOf(sps),
            Role.MR, ImmutableList.copyOf(mrs),
            Role.NONE, ImmutableList.copyOf(rest)));
  }

  private static final Rotation create(Map<Role, ImmutableList<Player>> rotation) {
    return new Rotation(rotation);
  }
}
