package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.collect.Iterables;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.roster.Team;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public class TeamExtraction {

    private final Site site;

    private TeamExtraction(Site site) {
        Assertions.assertThat(site).isNotNull();

        this.site = site;
    }

    public Team extractTeam(Id<Team> id) {
        return Team.create(
            Iterables.concat(
                PlayerList.ratingsReport(site, id).extract(),
                PlayerList.minorLeagues(site, id).extract()));

    }

    public static TeamExtraction create(Site site) {
        return new TeamExtraction(site);
    }

}
