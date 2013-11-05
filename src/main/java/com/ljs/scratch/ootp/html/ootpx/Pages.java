package com.ljs.scratch.ootp.html.ootpx;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Team;

/**
 *
 * @author lstephen
 */
public final class Pages {

    private Pages() { }

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


}
