package com.ljs.scratch.ootp.ootp5.site;

import com.google.common.collect.ImmutableMap;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.TeamPitching;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.util.ElementsUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class TeamPitchingImpl
    extends SingleTeamStats<PitchingStats>
    implements TeamPitching {

    private static final int ATBAT_IDX = 1;
    private static final int HITS_IDX = 2;
    private static final int DOUBLES_IDX = 3;
    private static final int TRIPLES_IDX = 4;
    private static final int STRIKEOUT_IDX = 8;
    private static final int WALKS_IDX = 7;
    private static final int HOMERUN_IDX = 5;

    public TeamPitchingImpl(Site site, Id<Team> team) {
        super(site.extractTeam(), site.getPage("team" + team.get() + "p.html"));
    }

    @Override
    protected PitchingStats zero() {
        return new PitchingStats();
    }

    @Override
    protected PitchingStats extractStatsRow(Elements data) {
        PitchingStats pitching = new PitchingStats();
        pitching.setAtBats(ElementsUtil.getInteger(data, ATBAT_IDX));
        pitching.setHits(ElementsUtil.getInteger(data, HITS_IDX));
        pitching.setDoubles(ElementsUtil.getInteger(data, DOUBLES_IDX));
        pitching.setTriples(ElementsUtil.getInteger(data, TRIPLES_IDX));
        pitching.setStrikeouts(ElementsUtil.getInteger(data, STRIKEOUT_IDX));
        pitching.setWalks(ElementsUtil.getInteger(data, WALKS_IDX));
        pitching.setHomeRuns(ElementsUtil.getInteger(data, HOMERUN_IDX));
        return pitching;
    }


    @Override
    protected ImmutableMap<Player, PitchingStats> extractStatsVsLeft(
        Document doc) {

        Elements vLhpStats = doc.select("tr:has(td:contains(vs. LHB)) + tr");
        return extractStats(vLhpStats);
    }

    @Override
    protected ImmutableMap<Player, PitchingStats> extractStatsVsRight(
        Document doc) {

        Elements vRhpStats = doc.select("tr:has(td:contains(vs. RHB)) + tr");
        return extractStats(vRhpStats);
    }



}
