// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LineupSelection.java

package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.collect.ImmutableSet;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.TeamStats;

// Referenced classes of package com.ljs.scratch.ootp.selection.lineup:
//            AllLineups, StarterSelection, Lineup, LineupOrdering, 
//            DefenseSelection

public class LineupSelection {

    private final TeamStats<BattingStats> predictions;

    public LineupSelection(TeamStats<BattingStats> predictions) {
        this.predictions = predictions;
    }

    public AllLineups select(Iterable available)
    {
        AllLineups all = new AllLineups();
        all.setVsRhp(selectWithoutDh(Lineup.VsHand.VS_RHP, available));
        all.setVsRhpPlusDh(selectWithDh(Lineup.VsHand.VS_RHP, available));
        all.setVsLhp(selectWithoutDh(Lineup.VsHand.VS_LHP, available));
        all.setVsLhpPlusDh(selectWithDh(Lineup.VsHand.VS_LHP, available));
        return all;
    }

    private Lineup selectWithoutDh(Lineup.VsHand vs, Iterable<Player> available) {
        StarterSelection ss = new StarterSelection(predictions);

        ImmutableSet withoutDhStarters =
            ImmutableSet.copyOf(ss.select(vs, available));

        return arrange(vs, withoutDhStarters);
    }

    private Lineup selectWithDh(Lineup.VsHand vs, Iterable<Player> available) {
        StarterSelection ss = new StarterSelection(predictions);

        ImmutableSet withDhStarters =
            ImmutableSet.copyOf(ss.selectWithDh(vs, available));

        return arrange(vs, withDhStarters);
    }

    private Lineup arrange(Lineup.VsHand vs, Iterable selected)
    {
        Lineup lineup = new Lineup();
        lineup.setOrder((new LineupOrdering(predictions)).order(vs, selected));
        lineup.setDefense((new DefenseSelection()).select(selected));
        return lineup;
    }

}
