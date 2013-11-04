package com.ljs.scratch.ootp.html.ootpx;

import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;

/**
 *
 * @author lstephen
 */
public final class Pages {

    private Pages() { }

    public static Page player(Site site, Player p) {
        return player(site, p.getId());
    }

    public static Page player(Site site, PlayerId id) {
        return site.getPage("players/" + id.unwrap() + ".html");
    }

    public static Page leagueBatting(Site site) {
        return site.getPage("leagues/league_100_batting_report.html");
    }

}
