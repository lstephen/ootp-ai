// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LineupOrdering.java

package com.ljs.ootp.ai.selection.lineup;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.hillclimbing.HillClimbing;
import com.ljs.ai.search.hillclimbing.RepeatedHillClimbing;
import com.ljs.ai.search.hillclimbing.Validators;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ai.search.hillclimbing.action.SequencedAction;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class LineupOrdering {

    private final TeamStats<BattingStats> predictions;

    public LineupOrdering(TeamStats<BattingStats> predictions) {
        this.predictions = predictions;
    }

    public ImmutableList<Player> order(final Lineup.VsHand vs, final Iterable<Player> ps) {
        final Set<Action<Lineup>> actions = Sets.newHashSet();

        for (final Player lhs : ps) {
            for (final Player rhs : ps) {
                if (lhs != rhs) {
                    actions.add(new Action<Lineup>() {
                        public Lineup apply(Lineup l) {
                            return l.swap(lhs, rhs);
                        }
                    });
                }
            }
        }

        actions.addAll(SequencedAction.allPairs(actions));

        HillClimbing.Builder<Lineup> builder = HillClimbing
            .<Lineup>builder()
            .validator(Validators.<Lineup>alwaysTrue())
            .heuristic(Ordering.natural().onResultOf(new Function<Lineup, Double>() {
                public Double apply(Lineup l) {
                    return l.score(vs, predictions);
                }
            }))
            .actionGenerator(new ActionGenerator<Lineup>() {
                public Iterable<Action<Lineup>> apply(Lineup l) {
                    return actions;
                }
            });


        return new RepeatedHillClimbing<Lineup>(
            new Callable<Lineup>() {
                public Lineup call() {
                    List<Player> order = Lists.newArrayList(ps);
                    Collections.shuffle(order);
                    Lineup l = new Lineup();
                    l.setOrder(order);
                    l.setDefense(new DefenseSelection().select(ps));
                    return l;
                }
            },
            builder)
            .search()
            .getOrder();

    }


}
