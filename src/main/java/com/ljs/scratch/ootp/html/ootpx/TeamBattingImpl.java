package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.collect.Maps;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.TeamBatting;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.SplitStats;
import com.ljs.scratch.ootp.stats.TeamStats;
import com.ljs.scratch.util.ElementsUtil;
import java.util.Map;
import org.fest.assertions.api.Assertions;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class TeamBattingImpl implements TeamBatting {

    private Site site;

    private Team team;

    private TeamBattingImpl(Site site, Team team) {
        Assertions.assertThat(site).isNotNull();
        Assertions.assertThat(team).isNotNull();

        this.site = site;
        this.team = team;
    }

    @Override
    public Integer getYear() {
        return 1999;
    }

    @Override
    public TeamStats<BattingStats> extract() {
        Map<Player, SplitStats<BattingStats>> stats = Maps.newHashMap();

        for (Player p : team) {
            SplitStats<BattingStats> s = extractStats(p);

            if (s != null) {
                stats.put(p, s);
            }
        }

        return TeamStats.create(stats);
    }

    private SplitStats<BattingStats> extractStats(Player p) {
        Document doc = Pages.player(site, p).load();

        Elements mlStats = doc.select("table > tbody > tr.title2:contains(MLB) + tr");

        if (!mlStats.isEmpty()) {
            Elements vsLeftLine = mlStats.select("tbody > tr:contains(Versus Left)");
            Elements vsRightLine = mlStats.select("tbody > tr:contains(Versus Right)");

            if (vsLeftLine.isEmpty() || vsRightLine.isEmpty()) {
                return null;
            }
            
            return SplitStats.create(extractStats(vsLeftLine.get(0).children()), extractStats(vsRightLine.get(0).children()));
        }

        return null;
    }

    private BattingStats extractStats(Elements line) {
        BattingStats stats = new BattingStats();
        stats.setAtBats(ElementsUtil.getInteger(line, 2));
        stats.setHits(ElementsUtil.getInteger(line, 3));
        stats.setDoubles(ElementsUtil.getInteger(line, 4));
        stats.setTriples(ElementsUtil.getInteger(line, 5));
        stats.setHomeRuns(ElementsUtil.getInteger(line, 6));
        stats.setWalks(ElementsUtil.getInteger(line, 9));
        return stats;
    }

    public static TeamBattingImpl create(Site site, Id<Team> team) {
        return create(site, TeamExtraction.create(site).extractTeam(team));
    }

    public static TeamBattingImpl create(Site site, Team team) {
        return new TeamBattingImpl(site, team);
    }

}
