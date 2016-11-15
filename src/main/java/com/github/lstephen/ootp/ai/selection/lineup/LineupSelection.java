// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
package com.github.lstephen.ootp.ai.selection.lineup;

import com.google.common.collect.ImmutableSet;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictor;

public class LineupSelection {

    private final Predictor predictor;

    public LineupSelection(Predictor predictor) {
        this.predictor = predictor;
    }

    public AllLineups select(Iterable<Player> available) {
        return AllLineups.create(All
            .<Lineup>builder()
            .vsRhp(selectWithoutDh(Lineup.VsHand.VS_RHP, available))
            .vsRhpPlusDh(selectWithDh(Lineup.VsHand.VS_RHP, available))
            .vsLhp(selectWithoutDh(Lineup.VsHand.VS_LHP, available))
            .vsLhpPlusDh(selectWithDh(Lineup.VsHand.VS_LHP, available))
            .build());
    }

    private Lineup selectWithoutDh(
        Lineup.VsHand vs, Iterable<Player> available) {

        StarterSelection ss = new StarterSelection(predictor);

        ImmutableSet<Player> withoutDhStarters =
            ImmutableSet.copyOf(ss.select(vs, available));

        return arrange(vs, available, withoutDhStarters);
    }

    private Lineup selectWithDh(Lineup.VsHand vs, Iterable<Player> available) {
        StarterSelection ss = new StarterSelection(predictor);

        ImmutableSet<Player> withDhStarters =
            ImmutableSet.copyOf(ss.selectWithDh(vs, available));

        return arrange(vs, available, withDhStarters);
    }

    private Lineup arrange(Lineup.VsHand vs, Iterable<Player> available, Iterable<Player> selected) {
        Lineup lineup = new Lineup();
        lineup.setOrder(new LineupOrdering(predictor).order(vs, selected));
        lineup.setDefense(new DefenseSelection().select(selected));
        return lineup;
    }

}
