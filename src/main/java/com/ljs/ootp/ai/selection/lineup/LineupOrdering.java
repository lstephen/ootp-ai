package com.ljs.ootp.ai.selection.lineup;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.hillclimbing.HillClimbing;
import com.ljs.ai.search.hillclimbing.RepeatedHillClimbing;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ai.search.hillclimbing.action.SequencedAction;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.selection.lineup.Lineup.VsHand;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.tuple.Pair;

public class LineupOrdering {

    private static final int WEIGHT_1B = 0;
    private static final int WEIGHT_2B = 1;
    private static final int WEIGHT_3B = 2;
    private static final int WEIGHT_HR = 3;
    private static final int WEIGHT_BB = 4;
    private static final int WEIGHT_K = 7;
    private static final int WEIGHT_OUT = 8;

    private static final double[][] WEIGHTS = {
        //  1B     2B    3B     HR    NIBB  HBP   RBOE      K     OUT
        { .515, .806, 1.121, 1.421, .385, .411, .542, -.329, -.328 },
        { .515, .799, 1.100, 1.450, .366, .396, .536, -.322, -.324 },
        { .493, .779, 1.064, 1.452, .335, .369, .514, -.317, -.315 },
        { .517, .822, 1.117, 1.472, .345, .377, .541, -.332, -.327 },
        { .513, .809, 1.106, 1.438, .346, .381, .530, -.324, -.323 },
        { .482, .763, 1.050, 1.376, .336, .368, .504, -.306, -.306 },
        { .464, .738, 1.014, 1.336, .323, .353, .486, -.296, -.296 },
        { .451, .714,  .980, 1.293, .312, .340, .470, -.287, -.286 },
        { .436, .689,  .948, 1.249, .302, .329, .454, -.278, -.277 }
    };

    private static final double[] RBIS_NO_DH = { 0.072, 0.090, 0.125, 0.133, 0.117, 0.108, 0.103, 0.083, 0.058 };
    private static final double[] RBIS_DH = { 0.080, 0.098, 0.135, 0.126, 0.119, 0.103, 0.097, 0.093, 0.086 };

    private static final double[] RISP = { .208, .255, .291, .305, .283, .266, .270, .270, .264 };

    private static final double[] PAS = { 4.63, 4.52, 4.42, 4.32, 4.22, 4.11, 3.99, 3.88, 3.75 };

    private static final Map<Pair<Lineup.VsHand, ImmutableSet<Player>>, ImmutableList<Player>> CACHE
        = Maps.newConcurrentMap();

    private final TeamStats<BattingStats> predictions;

    public LineupOrdering(TeamStats<BattingStats> predictions) {
        this.predictions = predictions;
    }

    private Boolean isValid(Order order) {
        return order.get().size() == ImmutableSet.copyOf(order.get()).size();
    }

    private Double score(Order order) {
      Double vsL = score(order, VsHand.VS_LHP);
      Double vsR = score(order, VsHand.VS_RHP);

      return (vsL + vsR) * getBalanceFactor(order);
    }

    private Double getBalanceFactor(Order order) {
      Double vsL = score(order, VsHand.VS_LHP) + 1;
      Double vsR = score(order, VsHand.VS_RHP) + 1;

      Double maxBalance = Math.pow((vsL + vsR) / 2.0, 2.0);
      Double actBalance = vsL * vsR;

      return Math.pow(actBalance / maxBalance, order.size());
    }

    private Double score(Order order, Lineup.VsHand vs) {
        Double score = 0.0;
        int pos = 0;

        for (Player p : order.get()) {
            score += score(p, pos, vs);
            pos++;
        }

        return score;
    }

    private Double score(Player p, int pos, Lineup.VsHand vs) {
        Double score = 0.0;

        double[] ws = WEIGHTS[pos];

        BattingStats ps = vs.getStats(predictions, p);

        score += ws[WEIGHT_1B] * ps.getSinglesPerPlateAppearance();
        score += ws[WEIGHT_2B] * ps.getDoublesPerPlateAppearance();
        score += ws[WEIGHT_3B] * ps.getTriplesPerPlateAppearance();
        score += ws[WEIGHT_HR] * ps.getHomeRunsPerPlateAppearance();
        score += ws[WEIGHT_BB] * ps.getWalksPerPlateAppearance();
        score += ws[WEIGHT_K] * ps.getKsPerPlateAppearance();
        score += ws[WEIGHT_OUT] * (ps.getOutsPerPlateAppearance() - ps.getKsPerPlateAppearance());

        return score;
    }

    private Double getClutchScore(Order o) {
      //double[] rbis = o.size() > 8 ? RBIS_DH : RBIS_NO_DH;
      double[] rbis = RISP;

      Double score = 0.0;

      for (int i = 0; i < 9; i++) {
        Optional<Player> p = o.get(i);
        if (p.isPresent() && p.get().getClutch().isPresent()) {
          switch (p.get().getClutch().get()) {
            case GREAT:
              score += rbis[i];
              break;
            case SUFFERS:
              score -= rbis[i];
              break;
            default:
              // do nothing
          }
        }
      }
      return score / 10.0;
    }

    private Double getConsistencyScore(Order o) {
      Double score = 0.0;

      for (int i = 0; i < 9; i++) {
        Optional<Player> p = o.get(i);
        if (p.isPresent() && p.get().getConsistency().isPresent()) {
          switch (p.get().getConsistency().get()) {
            case GOOD:
              score += PAS[i] / 100.0;
              break;
            case VERY_INCONSISTENT:
              score += PAS[i] / 100.0;
              break;
            default:
              // do nothing
          }
        }
      }

      return score / 10.0;
    }

    private Ordering<Order> byScore(final Lineup.VsHand vs) {
        return Ordering
            .natural()
            .onResultOf(
                new Function<Order, Double>() {
                    public Double apply(Order o) {
                        Double con = getConsistencyScore(o);
                        Double clu = getClutchScore(o);
                        Double ovs = score(o, vs);
                        Double ovb = score(o);

                        Double early = ovs + con;
                        Double middle = ovs + clu + con;
                        Double late = ovb + clu;

                        return early + middle + late;
                    }
                });
    }

    public ImmutableList<Player> order(Lineup.VsHand vs, Iterable<Player> ps) {
        Preconditions.checkArgument(Iterables.size(ps) == 8 || Iterables.size(ps) == 9);
        Preconditions.checkArgument(Iterables.size(ps) == ImmutableSet.copyOf(ps).size());

        Pair<Lineup.VsHand, ImmutableSet<Player>> key = Pair.of(vs, ImmutableSet.copyOf(ps));

        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }

        HillClimbing.Builder<Order> builder = HillClimbing
            .<Order>builder()
            .heuristic(byScore(vs))
            .actionGenerator(new OrderActions());

        Order result = new RepeatedHillClimbing<Order>(new RandomGenerator(ps), builder).search();

        CACHE.put(key, result.get());

        return result.get();
    }

    private static final class Order {

        private final ImmutableList<Player> order;

        private Order(Iterable<Player> ps) {
            this.order = ImmutableList.copyOf(ps);
        }

        public ImmutableList<Player> get() {
            return order;
        }

        public Optional<Player> get(Integer idx) {
            return idx >= 0 && idx < size()
                ? Optional.of(order.get(idx))
                : Optional.<Player>absent();
        }

        public int size() {
            return order.size();
        }

        public Order swap(int i, int j) {
            List<Player> ps = Lists.newArrayList(order);
            Collections.swap(ps, i, j);
            return create(ps);
        }

        public static Order create(Iterable<Player> ps) {
            return new Order(ps);
        }
    }

    private static class RandomGenerator implements Callable<Order> {

        private List<Player> ps;

        public RandomGenerator(Iterable<Player> ps) {
            this.ps = Lists.newArrayList(ps);
        }

        public Order call() {
            Collections.shuffle(ps);
            return Order.create(ps);
        }

    }

    private static class OrderActions implements ActionGenerator<Order> {
        public Iterable<Action<Order>> apply(Order o) {
            Set<Action<Order>> actions = Sets.newHashSet();

            for (int i = 0; i < o.size(); i++) {
                for (int j = i + 1; j < o.size(); j++) {
                    actions.add(new Swap(i, j));
                }
            }

            return Iterables.concat(actions, SequencedAction.allPairs(actions));
        }
    }

    private static class Swap implements Action<Order> {
        private final int i, j;

        public Swap(int i, int j) {
            this.i = i;
            this.j = j;
        }

        public Order apply(Order o) {
            return o.swap(i, j);
        }
    }

}
