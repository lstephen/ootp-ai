package com.ljs.scratch.ootp.ootpx.site;

import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.ootp.extract.html.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.Site;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.api.Assertions;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class PlayerList {

    private final Site site;

    private final Element root;

    private PlayerList(Site site, Element root) {
        Assertions.assertThat(site).isNotNull();
        Assertions.assertThat(root).isNotNull();

        this.site = site;
        this.root = root;
    }

    public Iterable<Player> extract() {
        return site.getPlayers(extractIds());
    }

    public Iterable<PlayerId> extractIds() {
        Elements els = root.select("tr td a");

        Set<PlayerId> ids = Sets.newHashSet();

        for (Element el : els) {
            String href = el.attr("href");

            if (href.contains("players/") && href.contains(".html")) {
                String id = StringUtils.substringBetween(href, "players/", ".html");

                if (id.equals("player_0")) {
                    continue;
                }

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

    public static PlayerList roster(Site site, Id<Team> t) {
        return from(site, Pages.roster(site, t));
    }

    public static PlayerList from(Site site, String url, Object... args) {
        return from(site, site.getPage(String.format(url, args)));
    }

    public static PlayerList from(Site site, Page page) {
        return from(site, page.load());
    }

    public static PlayerList from(Site site, Element root) {
        return new PlayerList(site, root);
    }

}
