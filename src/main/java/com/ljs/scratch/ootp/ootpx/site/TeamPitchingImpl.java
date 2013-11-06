package com.ljs.scratch.ootp.ootpx.site;

import com.google.common.collect.Maps;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.TeamPitching;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.stats.PitchingStats;
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
public class TeamPitchingImpl implements TeamPitching {

    private Site site;

    private Team team;

    private TeamPitchingImpl(Site site, Team team) {
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
    public TeamStats<PitchingStats> extract() {
        Map<Player, SplitStats<PitchingStats>> stats = Maps.newHashMap();

        for (Player p : team) {
            SplitStats<PitchingStats> s = extractStats(p);

            if (s != null) {
                stats.put(p, s);
            }
        }

        return TeamStats.create(stats);
    }

    private SplitStats<PitchingStats> extractStats(Player p) {
        Document doc = Pages.player(site, p).load();

        Elements mlStats = doc.select("table > tbody > tr.title2:contains(MLB) + tr");

        if (!mlStats.isEmpty()) {
            Elements vsLeftLine = mlStats.select("tbody > tr:contains(vs. LHB)");
            Elements vsRightLine = mlStats.select("tbody > tr:contains(vs. RHB)");

            if (vsLeftLine.isEmpty() || vsRightLine.isEmpty()) {
                return null;
            }
            
            return SplitStats.create(extractStats(vsLeftLine.get(0).children()), extractStats(vsRightLine.get(0).children()));
        }

        return null;
    }

    private PitchingStats extractStats(Elements line) {
        PitchingStats stats = new PitchingStats();
        stats.setAtBats(ElementsUtil.getInteger(line, 1));
        stats.setHits(ElementsUtil.getInteger(line, 2));
        stats.setDoubles(ElementsUtil.getInteger(line, 3));
        stats.setTriples(ElementsUtil.getInteger(line, 4));
        stats.setHomeRuns(ElementsUtil.getInteger(line, 5));
        stats.setWalks(ElementsUtil.getInteger(line, 7));
        stats.setStrikeouts(ElementsUtil.getInteger(line, 8));
        return stats;
    }

    public static TeamPitchingImpl create(Site site, Id<Team> team) {
        return create(site, TeamExtraction.create(site).extractTeam(team));
    }

    public static TeamPitchingImpl create(Site site, Team team) {
        return new TeamPitchingImpl(site, team);
    }

}
