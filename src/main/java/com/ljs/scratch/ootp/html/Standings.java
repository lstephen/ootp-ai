package com.ljs.scratch.ootp.html;

import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.team.TeamId;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class Standings {

    private final Page page;

    private Standings(Site site) {
        this.page = site.getPage("standr.html");
    }

    public Integer extractWins(TeamId id) {
        Document doc = page.load();

        Elements row = doc.select("table.s0 tr:has(a[href=team" + id.unwrap() + ".html]");

        return Integer.parseInt(row.get(0).child(1).text());
    }

    public Integer extractLosses(TeamId id) {
        Document doc = page.load();

        Elements row = doc.select("table.s0 tr:has(a[href=team" + id.unwrap() + ".html]");

        return Integer.parseInt(row.get(0).child(2).text());
    }

    public static Standings create(Site site) {
        return new Standings(site);
    }

}
