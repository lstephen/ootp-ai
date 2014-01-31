package com.ljs.ootp.ai.ootpx.site;

import com.google.common.collect.ImmutableSet;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.roster.Roster;
import com.ljs.ootp.ai.roster.Roster.Status;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Site;
import java.util.Map;
import org.fest.assertions.api.Assertions;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class RosterExtraction {

    private final Site site;

    private RosterExtraction(Site site) {
        Assertions.assertThat(site).isNotNull();

        this.site = site;
    }


    public Roster extract(Id<Team> id) {
        Team team = Team.create(ImmutableSet.<Player>of());

        Roster roster = Roster.create(team);

        assignTeam(Status.ML, roster, id);

        assignMinorLeagues(roster, id);

        // Assign injured players to the DL.
        // If they're already on a roster they won't be added to the DL
        // But, this is the only place that DL'd players can be associated with
        // a team.
        roster.assign(Status.DL, PlayerList.roster(site, id).extract());
        roster.assign(Status.DL, PlayerList.minorLeagues(site, id).extract());

        return roster;
    }

    private void assignMinorLeagues(Roster roster, Id<Team> id) {
        for (Map.Entry<Id<Team>, Status> mls : TeamExtraction.create(site).getMinorLeagueTeams(id).entrySet()) {
            assignTeam(mls.getValue(), roster, mls.getKey());
        }
    }

    private void assignTeam(Status status, Roster roster, Id<Team> id) {
        roster.assign(status, PlayerList.ratingsReport(site, id).extract());
    }

    private Iterable<Player> getPlayers(Document doc, String needle) {
        Elements players = doc.select("tr table:has(span:containsOwn(" + needle + ")");
        return PlayerList.from(site, players.first()).extract();
    }

    public static RosterExtraction create(Site site) {
        return new RosterExtraction(site);
    }

}
