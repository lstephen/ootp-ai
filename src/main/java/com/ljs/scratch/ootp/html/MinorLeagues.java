package com.ljs.scratch.ootp.html;

import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.PlayerId;
import com.ljs.scratch.ootp.core.Roster;
import com.ljs.scratch.ootp.core.Team;
import com.ljs.scratch.ootp.core.TeamId;
import com.ljs.scratch.ootp.html.page.Page;
import java.util.Set;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class MinorLeagues {

    private static final Logger LOG =
        Logger.getLogger(MinorLeagues.class.getName());

    private final Site site;

    private final Page page;

    private final Team team;

    public MinorLeagues(Site site, TeamId team) {
        this.page = site.getPage("team" + team.unwrap() + "m.html");
        this.site = site;
        this.team = site.getTeamRatings(team).extractTeam();
    }

    public void assignTo(Roster roster) {
        Document doc = page.load();

        LOG.fine("Getting AAA players...");
        assignTo(roster, doc.select("tr:has(td:contains(AAA Team)) + tr"), Roster.Status.AAA);
        LOG.fine("Getting AA players...");
        assignTo(roster, doc.select("tr:has(td:contains(AAA Team)) ~ tr:has(td:contains(AA Team)) + tr"), Roster.Status.AA);
        LOG.fine("Getting A players...");
        assignTo(roster, doc.select("tr:has(td:contains(AAA Team)) ~ tr:has(td:contains(AA Team)) ~ tr:has(td:contains(A Team)) + tr"), Roster.Status.A);
    }

    public void assignTo(Roster roster, Elements els, Roster.Status level) {
        for (Element el : els.select("a")) {
            PlayerId id = new PlayerId(el.attr("href").replaceAll(".html", ""));

            Player p = site.getPlayer(id).extract();

            if (p != null) {
                roster.assign(level, p);
            }
        }
    }

    public Iterable<Player> extract() {
        Document doc = page.load();

        Set<Player> players = Sets.newHashSet();

        for (Element el : doc.select("tr:has(td:contains( AAA Team)) + tr, "
            + "tr:has(td:contains( AA Team)) + tr, "
            + "tr:has(td:contains( A Team)) + tr").select("a")) {

            PlayerId id = new PlayerId(el.attr("href").replaceAll(".html", ""));

            players.add(site.getPlayer(id).extract());
        }

        return players;
    }

}
