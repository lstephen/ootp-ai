package com.ljs.scratch.ootp.ootpx.site;

import com.google.common.collect.Iterables;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.roster.Team;
import org.fest.assertions.api.Assertions;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public final class TeamExtraction {

    private final Site site;

    private TeamExtraction(Site site) {
        Assertions.assertThat(site).isNotNull();

        this.site = site;
    }

    public Team extractTeam(Id<Team> id) {
        Team team = Team.create(
            Iterables.concat(
                PlayerList.ratingsReport(site, id).extract(),
                PlayerList.minorLeagues(site, id).extract()));

        team.addInjury(extractInjuries(id));

        return team;

    }

    private Iterable<Player> extractInjuries(Id<Team> id) {
        Document doc = Pages.team(site, id).load();

        throw new RuntimeException();

    }

    public static TeamExtraction create(Site site) {
        return new TeamExtraction(site);
    }

}
