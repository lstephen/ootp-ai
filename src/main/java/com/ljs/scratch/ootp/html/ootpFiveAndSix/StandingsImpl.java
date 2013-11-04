package com.ljs.scratch.ootp.html.ootpFiveAndSix;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.Standings;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.roster.Team;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class StandingsImpl implements Standings {

    private final Page page;

    private StandingsImpl(Site site) {
        this.page = site.getPage("standr.html");
    }

    @Override
    public Integer getWins(Id<Team> id) {
        Document doc = page.load();

        Elements row = doc.select("table.s0 tr:has(a[href=team" + id.get() + ".html]");

        return Integer.parseInt(row.get(0).child(1).text());
    }

    @Override
    public Integer getLosses(Id<Team> id) {
        Document doc = page.load();

        Elements row = doc.select("table.s0 tr:has(a[href=team" + id.get() + ".html]");

        return Integer.parseInt(row.get(0).child(2).text());
    }

    public static StandingsImpl create(Site site) {
        return new StandingsImpl(site);
    }

}
