package com.ljs.scratch.ootp.html.ootpFiveAndSix;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.stats.SplitStats;
import com.ljs.scratch.ootp.stats.Stats;
import com.ljs.scratch.ootp.stats.TeamStats;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public abstract class SingleTeamStats<S extends Stats<S>> {

    private final Team team;

    private final Page page;

    protected SingleTeamStats(Team team, Page page) {
        this.team = team;
        this.page = page;
    }

    public Integer getYear() {
        return extractDate().getYear();
    }

    public LocalDate extractDate() {
        Document doc = page.load();

        return LocalDate.parse(
            StringUtils.substringAfterLast(doc.select("td.title").text(), ",").trim(),
            DateTimeFormat.forPattern("MM/dd/YYYY"));
    }

    public TeamStats<S> extract() {
        Document doc = page.load();

        ImmutableMap<Player, S> vLhp = extractStatsVsLeft(doc);
        ImmutableMap<Player, S> vRhp = extractStatsVsRight(doc);

        return combineSplits(vLhp, vRhp);
    }

    protected ImmutableMap<Player, S> extractStats(Elements els) {
        ImmutableMap.Builder<Player, S> stats =
            ImmutableMap.builder();

        Elements statsLines = els.select("tbody tr:has(a)");

        for (Element row : statsLines) {
            PlayerId id =
                new PlayerId(row
                        .select("a[href]")
                        .get(0)
                        .attr("href")
                        .replaceAll(".html", ""));


            stats.put(team.get(id), extractStatsRow(row.children()));
        }

        return stats.build();
    }

    protected abstract S extractStatsRow(Elements els);

    protected abstract ImmutableMap<Player, S>
        extractStatsVsLeft(Document doc);

    protected abstract ImmutableMap<Player, S>
        extractStatsVsRight(Document doc);

    protected abstract S zero();

    protected TeamStats<S> combineSplits(
        ImmutableMap<Player, S> vLhp,
        ImmutableMap<Player, S> vRhp) {

        Map<Player, SplitStats<S>> splits = Maps.newHashMap();

        Set<Player> ps = Sets.union(vLhp.keySet(), vRhp.keySet());

        for (Player p : ps) {
            S vsLeft =
                vLhp.containsKey(p) ? vLhp.get(p) : zero();

            S vsRight =
                vRhp.containsKey(p) ? vRhp.get(p) : zero();

            splits.put(p, SplitStats.create(vsLeft, vsRight));
        }

        return TeamStats.create(splits);
    }

}
