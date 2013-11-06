package com.ljs.scratch.ootp.ootp5.site;

import com.google.common.base.CharMatcher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.site.SingleTeam;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.html.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Team;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class SingleTeamImpl implements SingleTeam {

    private final Page page;

    private final Site site;

    private final Id<Team> teamId;

    private static Cache<String, Set<PlayerId>> injuriesCache =
        CacheBuilder.newBuilder().build();


    public SingleTeamImpl(Site site, Id<Team> team) {
        this.site = site;
        this.teamId = team;

        page = site.getPage("team" + team.get() + ".html");
    }

    @Override
    public String getName() {
        Document doc = page.load();

        return CharMatcher.WHITESPACE.trimAndCollapseFrom(
            StringUtils.substringBefore(
                doc.select("title").text(), "Clubhouse"),
            ' ');
    }

    @Override
    public Roster getRoster() {
        Document doc = page.load();

        Team team = new TeamRatings(site, teamId).extractTeam();

        Roster roster = Roster.create(team);

        Elements activeRoster =
            doc.select("tr:has(td:contains(Active Roster)) + tr");

        activeRoster.addAll(
            doc.select("tr:has(td:contains(Active Roster)) + tr + tr"));

        Elements activePlayers = activeRoster.select("a");

        for (Element el : activePlayers) {
            PlayerId id = new PlayerId(el.attr("href").replaceAll(".html", ""));

            if (team.containsPlayer(id)) {
                roster.assign(Roster.Status.ML, id);
            } else {
                Player p = site.getPlayer(id);

                if (p != null) {
                    roster.assign(Roster.Status.ML, p);
                }
            }
        }

        new MinorLeagues(site, teamId).assignTo(roster);

        roster.assign(Roster.Status.DL, roster.getUnassigned());

        return roster;
    }

    public Iterable<Player> extractPlayers() {
        return getRoster().getAllPlayers();
    }

    public Iterable<PlayerId> extractInjuries() {
        Set<PlayerId> results = injuriesCache.getIfPresent(site.getName() + teamId);

        if (results != null) {
            return results;
        }

        Document doc = page.load();

        results = Sets.newHashSet();

        Elements injuriesTable =
            doc.select("tr:has(td:contains(Injuries)) + tr");

        Elements injuries = injuriesTable.select("tr.g:has(a), tr.g2:has(a)");

        for (Element injury : injuries) {
            Element el = injury.select("a").first();
            PlayerId id =
                new PlayerId(el.attr("href").replaceAll(".html", ""));

            if (injury.child(1).text().contains("OUT")
                && !injury.child(2).text().contains("days")) {

                results.add(id);
            }

            if (!injury.child(3).text().equals("0 days")) {
                results.add(id);
            }
        }

        injuriesCache.put(site.getName() + teamId, results);

        return results;
    }

}
