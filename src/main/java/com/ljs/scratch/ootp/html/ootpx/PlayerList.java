package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Team;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class PlayerList {

    private final Site site;

    private final Page page;

    private PlayerList(Site site, Page page) {
        this.site = site;
        this.page = page;
    }

    public Iterable<Player> extract() {
        return site.getPlayers(extractIds());
    }

    public Iterable<PlayerId> extractIds() {
        Document doc = page.load();

        Elements els = doc.select("tr td a");

        Set<PlayerId> ids = Sets.newHashSet();

        for (Element el : els) {
            String href = el.attr("href");

            if (href.contains("players/")) {
                String id = StringUtils.substringBetween(href, "players/", ".html");
                ids.add(new PlayerId(id));
            }
        }

        return ids;
    }

    public static PlayerList minorLeagues(Site site, Id<Team> id) {
        return from(site, "teams/team_%s_minor_league_system.html", id.get());
    }

    public static PlayerList ratingsReport(Site site, Id<Team> id) {
        return from(site, "teams/team_%s_ratings_report.html", id.get());
    }

    public static PlayerList from(Site site, String url, Object... args) {
        return new PlayerList(site, site.getPage(String.format(url, args)));
    }

    public static PlayerList from(Site site, Page page) {
        return new PlayerList(site, page);
    }

}
