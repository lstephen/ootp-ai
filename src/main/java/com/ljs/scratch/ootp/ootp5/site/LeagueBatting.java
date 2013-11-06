package com.ljs.scratch.ootp.ootp5.site;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.html.Page;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.util.Jackson;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class LeagueBatting {

    private static final int HOMERUN_IDX = 6;
    private static final int ATBAT_IDX = 8;
    private static final int HITS_IDX = 9;
    private static final int DOUBLES_IDX = 10;
    private static final int TRIPLES_IDX = 11;
    private static final int WALKS_IDX = 12;

    @JsonIgnore
    private Site site;

    @JsonIgnore
    private Page page;

    @JsonIgnore
    private String league;

    private Map<Integer, BattingStats> historical = Maps.newHashMap();

    private LeagueBatting() { }

    public LeagueBatting(Site site, String league) {
        page = site.getPage("leagueb.html");
        this.league = league;
        this.site = site;
    }

    private File getHistoricalFile() {
        return new File("c:/ootp/history/" + site.getName() + "league.batting.json");
    }

    private void loadHistorical() {

        File f = getHistoricalFile();

        if (f.exists()) {
            try {
                historical =
                    Jackson.getMapper().readValue(f, LeagueBatting.class).historical;
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public LocalDate extractDate() {
        Document doc = page.load();

        return LocalDate.parse(
            StringUtils.substringAfterLast(doc.select("td.title").text(), ",").trim(),
            DateTimeFormat.forPattern("MM/dd/YYYY"));
    }

    public BattingStats extractTotal() {
        Elements total = extractTotalLine();

        BattingStats batting = new BattingStats();

        batting.setHomeRuns(getInteger(total, HOMERUN_IDX));
        batting.setAtBats(getInteger(total, ATBAT_IDX));
        batting.setHits(getInteger(total, HITS_IDX));
        batting.setDoubles(getInteger(total, DOUBLES_IDX));
        batting.setTriples(getInteger(total, TRIPLES_IDX));
        batting.setWalks(getInteger(total, WALKS_IDX));

        int currentSeason = extractDate().getYear();

        loadHistorical();

        historical.put(currentSeason, batting);

        for (int i = 1; i < 5; i++) {
            if (historical.containsKey(currentSeason - i)) {
                batting = batting.add(historical.get(currentSeason - i));
            }
        }

        try {
            Jackson.getMapper().writeValue(getHistoricalFile(), this);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        return batting;
    }

    private Integer getInteger(Elements line, int idx) {
        return Integer.valueOf(line.get(idx).text());
    }

    private Elements extractTotalLine() {
        Document doc = page.load();

        Elements leagueStats =
            doc.select("tr:has(td:contains(" + league + ")) ~ tr");

        return leagueStats.select(
            "tr.g:has(td > b:contains(Total)) td, "
                + "tr.g2:has(td > b:contains(Total)) td");
    }

}
