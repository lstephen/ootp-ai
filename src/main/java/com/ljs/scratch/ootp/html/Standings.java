package com.ljs.scratch.ootp.html;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.roster.Team;
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

    public Integer extractWins(Id<Team> id) {
        Document doc = page.load();

        Elements row = doc.select("table.s0 tr:has(a[href=team" + id.get() + ".html]");

        return Integer.parseInt(row.get(0).child(1).text());
    }

    public Integer extractLosses(Id<Team> id) {
        Document doc = page.load();

        Elements row = doc.select("table.s0 tr:has(a[href=team" + id.get() + ".html]");

        return Integer.parseInt(row.get(0).child(2).text());
    }

    public static Standings create(Site site) {
        return new Standings(site);
    }

}
