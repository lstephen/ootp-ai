package com.ljs.ootp.ai.ootp5.site;

import com.google.common.collect.ImmutableSet;
import com.ljs.ootp.extract.html.Page;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Site;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class TeamRatings {

    private final Site site;

    private final Id<Team> team;

    private final Page page;

    public TeamRatings(Site site, Id<Team> team) {
        this.site = site;
        this.team = team;
        page = site.getPage("team" + team.get() + "rr.html");
    }

    public Team extractTeam() {
        ImmutableSet.Builder<Player> result =
            ImmutableSet.builder();

        for (PlayerId id : extractPlayerIds()) {
            Player p = site.getPlayer(id);

            if (p != null) {
                result.add(p);
            }
        }

        Team team = Team.create(result.build());

        team.addInjury(site.getPlayers(new SingleTeamImpl(site, this.team).getInjuries()));

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
