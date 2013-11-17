package com.ljs.ootp.ai.ootpx.site;

import com.google.common.collect.ImmutableSet;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.roster.Roster;
import com.ljs.ootp.ai.roster.Roster.Status;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Site;
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

        roster.assign(Status.ML, PlayerList.ratingsReport(site, id).extract());

        assignMinorLeagues(roster, id);

        // Assign injured players to the DL.
        // If they're already on a roster they won't be added to the DL
        // But, this is the only place that DL'd players can be associated with
        // a team.
        roster.assign(Status.DL, PlayerList.roster(site, id).extract());

        return roster;
    }

    private void assignMinorLeagues(Roster roster, Id<Team> id) {
        Document doc = Pages.minorLeagues(site, id).load();

        roster.assign(Status.AAA, getPlayers(doc, "Triple A"));
        roster.assign(Status.AA, getPlayers(doc, "Double A"));
        roster.assign(Status.A, getPlayers(doc, "Single A"));
        roster.assign(Status.SA, getPlayers(doc, "Short Season A"));
        roster.assign(Status.R, getPlayers(doc, "Rookie League"));
    }

    private Iterable<Player> getPlayers(Document doc, String needle) {
        Elements players = doc.select("tr table:has(span:containsOwn(" + needle + ")");
        return PlayerList.from(site, players.first()).extract();
    }

    public static RosterExtraction create(Site site) {
        return new RosterExtraction(site);
    }

}
