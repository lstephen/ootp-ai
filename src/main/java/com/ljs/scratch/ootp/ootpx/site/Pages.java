package com.ljs.scratch.ootp.ootpx.site;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.ootp.extract.html.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.Site;
import org.joda.time.LocalDate;

/**
 *
 * @author lstephen
 */
public final class Pages {

    private Pages() { }

    public static Page boxScores(Site site, LocalDate date) {
        return site.getPage("leagues/league_100_scores_%d_%02d_%02d.html", date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    public static Page minorLeagues(Site site, Id<Team> t) {
        return site.getPage("teams/team_%s_minor_league_system.html", t.get());
    }

    public static Page leagueBatting(Site site) {
        return site.getPage("leagues/league_100_batting_report.html");
    }

    public static Page player(Site site, Player p) {
        return player(site, p.getId());
    }

    public static Page player(Site site, PlayerId id) {
        return site.getPage("players/%s.html", id.unwrap());
    }

    public static Page roster(Site site, Id<Team> t) {
        return site.getPage("teams/team_%s_roster_page.html", t.get());
    }

    public static Page salary(Site site, Id<Team> t) {
        return site.getPage("teams/team_%s_player_salary_report.html", t.get());
    }

    public static Page standings(Site site) {
        return site.getPage("leagues/league_100_standings.html");
    }

    public static Page team(Site site, Id<Team> t) {
        return site.getPage("teams/team_%s.html", t.get());
    }

    public static Page topProspects(Site site, Id<Team> t) {
        return site.getPage("teams/team_%s_top_prospects.html", t.get());
    }

}
