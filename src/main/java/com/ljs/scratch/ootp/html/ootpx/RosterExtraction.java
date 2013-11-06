package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.collect.ImmutableSet;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Roster.Status;
import com.ljs.scratch.ootp.roster.Team;
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

        // Assign players on the main page to the DL, they'll then be assigned
        // to the ML roster (or others) from the ratings report and minor leagues
        roster.assign(Status.DL, PlayerList.from(site, Pages.team(site, id).load()).extract());
        roster.assign(Status.ML, PlayerList.ratingsReport(site, id).extract());

        assignMinorLeagues(roster, id);

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
