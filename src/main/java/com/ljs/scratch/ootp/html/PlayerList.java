package com.ljs.scratch.ootp.html;

import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.PlayerId;
import com.ljs.scratch.ootp.html.page.Page;
import java.util.Set;
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

    private PlayerList(Site site, String url) {
        this(site, site.getPage(url));
    }

    public Iterable<Player> extract() {
        return site.getPlayers(extractIds());
    }

    public Iterable<PlayerId> extractIds() {
        Document doc = page.load();

        Elements els = doc.select("tr td a");

        Set<PlayerId> ids = Sets.newHashSet();

        for (Element el : els) {
            ids.add(new PlayerId(el.attr("href").replaceAll(".html", "")));
        }

        return ids;
    }

    public static PlayerList freeAgents(Site site) {
        return new PlayerList(site, "agents.html");
    }

    public static PlayerList futureFreeAgents(Site site) {
        return new PlayerList(site, "pagents.html");
    }

    public static PlayerList waiverWire(Site site) {
        return new PlayerList(site, "waiver.html");
    }

    public static PlayerList draft(Site site) {
        return new PlayerList(site, "rookies.html");
    }

    public static PlayerList ruleFiveDraft(Site site) {
        return new PlayerList(site, "rule5.html");
    }

    public static PlayerList from(Site site, Page page) {
        return new PlayerList(site, page);
    }

}
