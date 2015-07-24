package com.github.lstephen.ootp.ai.ootp5.site;

import com.github.lstephen.ootp.extract.html.Page;
import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.roster.Team;
import com.github.lstephen.ootp.ai.site.Record;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.site.Standings;
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
