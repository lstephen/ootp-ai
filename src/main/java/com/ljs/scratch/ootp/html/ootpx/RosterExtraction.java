package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.collect.ImmutableSet;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Roster.Status;
import com.ljs.scratch.ootp.roster.Team;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public class RosterExtraction {

    private Site site;

    private RosterExtraction(Site site) {
        Assertions.assertThat(site).isNotNull();

        this.site = site;
    }


    public Roster extract(Id<Team> id) {
        Team team = Team.create(ImmutableSet.<Player>of());

        Roster roster = Roster.create(team);

        roster.assign(Status.ML, PlayerList.ratingsReport(site, id).extract());

        return roster;
    }


    public static RosterExtraction create(Site site) {
        return new RosterExtraction(site);
    }

}
