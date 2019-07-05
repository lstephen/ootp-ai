package com.github.lstephen.ootp.ai.selection.lineup;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LineupOrdering {

  private final Predictor predictor;

  public LineupOrdering(Predictor predictor) {
    this.predictor = predictor;
  }

  public ImmutableList<Player> order(Lineup.VsHand vs, Iterable<Player> ps) {
    Preconditions.checkArgument(Iterables.size(ps) == 8 || Iterables.size(ps) == 9);
    Preconditions.checkArgument(Iterables.size(ps) == ImmutableSet.copyOf(ps).size());

    Function<Function<BattingStats, Double>, Ordering<Player>> byStat =
        f -> Ordering.natural().onResultOf(p -> f.apply(vs.getStats(predictor, p)));

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

    return ImmutableList.copyOf(lineup);
  }

  private Player select(Function<Iterable<Player>, Player> f, Collection<Player> ps) {
    Player selected = f.apply(ps);
    ps.remove(selected);
    return selected;
  }
}
