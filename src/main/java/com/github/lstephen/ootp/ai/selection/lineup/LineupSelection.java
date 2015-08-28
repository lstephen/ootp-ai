// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
package com.github.lstephen.ootp.ai.selection.lineup;

import com.google.common.collect.ImmutableSet;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictions;

public class LineupSelection {

    private final Predictions predictions;

    private boolean requireBackupCatcher = true;

    public LineupSelection(Predictions predictions) {
        this.predictions = predictions;
    }

    public LineupSelection dontRequireBackupCatcher() {
        requireBackupCatcher = false;
        return this;
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

        StarterSelection ss = new StarterSelection(predictions);

        //if (!requireBackupCatcher) {
        //    ss.dontRequireBackupCatcher();
        //}

        ImmutableSet<Player> withoutDhStarters =
            ImmutableSet.copyOf(ss.select(vs, available, requireBackupCatcher));

        return arrange(vs, available, withoutDhStarters);
    }

    private Lineup selectWithDh(Lineup.VsHand vs, Iterable<Player> available) {
        StarterSelection ss = new StarterSelection(predictions);

        ImmutableSet<Player> withDhStarters =
            ImmutableSet.copyOf(ss.selectWithDh(vs, available, requireBackupCatcher));

        return arrange(vs, available, withDhStarters);
    }

    private Lineup arrange(Lineup.VsHand vs, Iterable<Player> available, Iterable<Player> selected) {
        Lineup lineup = new Lineup();
        lineup.setOrder(new LineupOrdering(predictions.getAllBatting()).order(vs, selected));
        lineup.setDefense(new DefenseSelection().select(selected));
        return lineup;
    }

}
