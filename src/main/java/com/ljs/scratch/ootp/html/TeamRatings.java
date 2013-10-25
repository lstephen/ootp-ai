package com.ljs.scratch.ootp.html;

import com.google.common.collect.ImmutableSet;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.roster.TeamId;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class TeamRatings {

    private final Site site;

    private final Page page;

    public TeamRatings(Site site, TeamId team) {
        this.site = site;
        page = site.getPage("team" + team.unwrap() + "rr.html");
    }

    public Team extractTeam() {
        ImmutableSet.Builder<Player> result =
            ImmutableSet.builder();

        for (PlayerId id : extractPlayerIds()) {
            Player p = site.getPlayer(id).extract();

            if (p != null) {
                result.add(p);
            }
        }

        Team team = Team.create(result.build());

        team.addInjury(site.getPlayers(site.getSingleTeam().extractInjuries()));

        return team;
    }

    private ImmutableSet<PlayerId> extractPlayerIds() {
        Document doc = page.load();

        Elements els = doc.select("td a");

        ImmutableSet.Builder<PlayerId> ids = ImmutableSet.builder();

        for (Element el : els) {
            ids.add(new PlayerId(el.attr("href").replaceAll(".html", "")));
        }

        return ids.build();
    }

}
