package com.github.lstephen.ootp.ai.selection.lineup;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.site.SiteHolder;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LineupOrdering {

  private final Predictor predictor;

  private final TeamStats<BattingStats> teamBatting;

  public LineupOrdering(Predictor predictor) {
    this.predictor = predictor;
    this.teamBatting = SiteHolder.get().getTeamBatting();
  }

  public ImmutableList<Player> order(Lineup.VsHand vs, Iterable<Player> ps) {
    Preconditions.checkArgument(Iterables.size(ps) == 8 || Iterables.size(ps) == 9);
    Preconditions.checkArgument(Iterables.size(ps) == ImmutableSet.copyOf(ps).size());

    Function<Function<BattingStats, Double>, Ordering<Player>> byStat =
        f -> Ordering.natural().onResultOf(p -> f.apply(getBattingStats(p, vs)));

    Ordering<Player> byWoba = byStat.apply(BattingStats::getWoba);
    Ordering<Player> byObp = byStat.apply(BattingStats::getOnBasePercentage);
    Ordering<Player> byIso = byStat.apply(BattingStats::getIsoPower);

    Player[] lineup = new Player[9];

    Set<Player> available = new HashSet<>();
    Iterables.addAll(available, ps);

    // select top 5 for top 5 lineup spots
    List<Player> best5 = Lists.newArrayList(byWoba.greatestOf(available, 5));
    Iterables.removeAll(available, best5);

    // best hitter at 4
    lineup[3] = select(byWoba::max, best5); // best at 4
    lineup[2] = select(byIso::max, best5); // power at 3
    lineup[4] = select(byObp::min, best5); // lowest obp at 5
    lineup[1] = select(byIso::max, best5); // power at 2
    lineup[0] = select(byWoba::max, best5); // remaining at 1

    Preconditions.checkState(best5.isEmpty());

    // put the rest in descending woba order
    int pos = 5;
    while (!available.isEmpty()) {
      lineup[pos++] = select(byWoba::max, available);
    }

    for (int i = 0; i < lineup.length; i++) {
      if (lineup[i] != null) {
        System.out.printf(
            "%s-%d/", lineup[i].getShortName(), getBattingStats(lineup[i], vs).getWobaPlus());
      }
    }
    System.out.println();

    return ImmutableList.copyOf(
        Arrays.stream(lineup).filter(l -> l != null).collect(Collectors.toList()));
  }

  private Player select(Function<Iterable<Player>, Player> f, Collection<Player> ps) {
    Player selected = f.apply(ps);
    ps.remove(selected);
    return selected;
  }

  private BattingStats getBattingStats(Player p, Lineup.VsHand vs) {
    BattingStats prediction = vs.getStats(predictor, p);

    if (!teamBatting.contains(p)) {
      return prediction;
    }

    BattingStats current = vs.getStats(teamBatting, p);

    double factor =
        p.getConsistency()
            .transform(
                c -> {
                  switch (c) {
                    case GOOD:
                      return 0.5;
                    case AVERAGE:
                      return 1.0;
                    case VERY_INCONSISTENT:
                      return 2.0;
                    default:
                      throw new IllegalStateException("Unknown consistency:" + c);
                  }
                })
            .or(0.0);

    current = current.multiply(prediction.getPlateAppearances() / 700.0);

    return prediction.add(current.multiply(factor));
  }
}
