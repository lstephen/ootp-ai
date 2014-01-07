package com.ljs.ootp.ai.selection.search;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.ai.search.Action;
import com.ljs.ai.search.ActionsFunction;
import com.ljs.ai.search.RepeatedHillClimbing;
import com.ljs.ai.search.SequencedAction;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.selection.Selection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.fest.util.Lists;

/**
 *
 * @author lstephen
 */
public class HillClimbingSelection implements Selection {

    public ImmutableMultimap<Slot, Player> select(
        Iterable<Player> forced, Iterable<Player> available) {

        SelectedPlayers ps = new RepeatedHillClimbing<SelectedPlayers>(
            SelectedPlayers.class,
            initialStateFactory(forced, available),
            actionsFunction(forced, available))
            .search();


        return ps.selection();
    }

    private Callable<SelectedPlayers> initialStateFactory(final Iterable<Player> forced, final Iterable<Player> available) {

        return new Callable<SelectedPlayers>() {
            public SelectedPlayers call() {
                List<Player> ps = Lists.newArrayList(Sets.difference(ImmutableSet.copyOf(available), ImmutableSet.copyOf(forced)));
                Collections.shuffle(ps);

                return SelectedPlayers.create(Iterables.limit(Iterables.concat(forced, ps), 9));
            }
        };
    }

    private ActionsFunction<SelectedPlayers> actionsFunction(final Iterable<Player> forced, final Iterable<Player> available) {
        return new ActionsFunction<SelectedPlayers>() {

            @Override
            public Iterable<Action<SelectedPlayers>> getActions(SelectedPlayers state) {
                ImmutableSet<Add> adds = adds(state);
                ImmutableSet<Remove> removes = removes(state);

                return Iterables.concat(SequencedAction.merged(adds, removes), adds, removes);
            }


            public ImmutableSet<Add> adds(SelectedPlayers sps) {
                Set<Add> as = Sets.newHashSet();

                for (Player p : Player.byWeightedBattingRating().sortedCopy(available)) {
                    if (!sps.contains(p)) {
                        as.add(new Add(p));
                    }
                }

                return ImmutableSet.copyOf(as);
            }

            public ImmutableSet<Remove> removes(SelectedPlayers sps) {
                Set<Remove> rs = Sets.newHashSet();

                for (Player p : sps.players()) {
                    if (!Iterables.contains(forced, p)) {
                        rs.add(new Remove(p));
                    }
                }

                return ImmutableSet.copyOf(rs);
            }
        };
    }

    private static class Add extends Action<SelectedPlayers> {

        private Player p;

        public Add(Player p) {
            this.p = p;
        }

        public SelectedPlayers apply(SelectedPlayers sps) {
            return sps.with(p);
        }
    }

    private static class Remove extends Action<SelectedPlayers> {

        private Player p;

        public Remove(Player p) {
            this.p = p;
        }

        public SelectedPlayers apply(SelectedPlayers sps) {
            return sps.without(p);
        }
    }

}
