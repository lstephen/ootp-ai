package com.ljs.scratch.ootp.ootp5.site;

import com.ljs.ootp.extract.html.Page;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.Record;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.Standings;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class StandingsImpl implements Standings {

    private final Page page;

    private StandingsImpl(Site site) {
        this.page = Pages.standings(site);
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

    @Override
    public Record getRecord(Id<Team> id) {
        return Record.create(getWins(id), getLosses(id));
    }

    public static StandingsImpl create(Site site) {
        return new StandingsImpl(site);
    }

}
