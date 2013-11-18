package com.ljs.ootp.ai.ootp5.site;

import com.google.common.collect.ImmutableMap;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.site.TeamBatting;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.scratch.util.ElementsUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class TeamBattingImpl
    extends SingleTeamStats<BattingStats>
    implements TeamBatting {

    private static final int HOMERUN_IDX = 5;
    private static final int ATBAT_IDX = 1;
    private static final int HITS_IDX = 2;
    private static final int DOUBLES_IDX = 3;
    private static final int TRIPLES_IDX = 4;
    private static final int WALKS_IDX = 8;

    private final BattingStats leagueBatting;

    public TeamBattingImpl(Site site, Id<Team> team) {
        super(site.extractTeam(), site.getPage("team" + team.get() + "b.html"));

        leagueBatting = site.getLeagueBatting();
    }


    @Override
    protected BattingStats zero() {
        BattingStats battingStats = new BattingStats();
        battingStats.setLeagueBatting(leagueBatting);
        return battingStats;
    }

    @Override
    protected BattingStats extractStatsRow(Elements data) {
        BattingStats batting = zero();
        batting.setHomeRuns(ElementsUtil.getInteger(data, HOMERUN_IDX));
        batting.setAtBats(ElementsUtil.getInteger(data, ATBAT_IDX));
        batting.setHits(ElementsUtil.getInteger(data, HITS_IDX));
        batting.setDoubles(ElementsUtil.getInteger(data, DOUBLES_IDX));
        batting.setTriples(ElementsUtil.getInteger(data, TRIPLES_IDX));
        batting.setWalks(ElementsUtil.getInteger(data, WALKS_IDX));
        return batting;
    }

    @Override
    protected ImmutableMap<Player, BattingStats> extractStatsVsLeft(
        Document doc) {

        Elements vLhpStats = doc.select("tr:has(td:contains(vs. LHP)) + tr");
        return extractStats(vLhpStats);
    }

    @Override
    protected ImmutableMap<Player, BattingStats> extractStatsVsRight(
        Document doc) {

        Elements vRhpStats = doc.select("tr:has(td:contains(vs. RHP)) + tr");
        return extractStats(vRhpStats);
    }



}