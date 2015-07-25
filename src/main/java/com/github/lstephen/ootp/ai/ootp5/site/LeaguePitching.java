package com.github.lstephen.ootp.ai.ootp5.site;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.github.lstephen.ootp.ai.config.Config;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.PitchingStats;
import com.github.lstephen.ootp.extract.html.Page;
import com.github.lstephen.scratch.util.Jackson;
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
public class LeaguePitching {

    private static final int IP_IDX = 8;
    private static final int HITS_IDX = 9;
    private static final int HOMERUN_IDX = 12;
    private static final int WALKS_IDX = 13;
    private static final int STRIKEOUT_IDX = 14;

    @JsonIgnore
    private Page page;

    @JsonIgnore
    private Site site;

    @JsonIgnore
    private String league;

    private Map<Integer, PitchingStats> historical = Maps.newHashMap();

    private LeaguePitching() { /* JAXB */ }

    public LeaguePitching(Site site, String league) {
        page = site.getPage("leaguep.html");
        this.site = site;
        this.league = league;
    }

    private File getHistoricalFile() {
        try {
            String historyDirectory = Config.createDefault().getValue("history.dir").or("c:/ootp/history/");
            return new File(historyDirectory + "/" + site.getName() + "league.pitching.json");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void loadHistorical() {

        File f = getHistoricalFile();

        if (f.exists()) {
            try {
                historical = Jackson.getMapper(site).readValue(f, LeaguePitching.class).historical;
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

    public PitchingStats extractTotal() {
        Elements total = extractTotalLine();

        BattingStats leagueBatting = site.getLeagueBatting();

        PitchingStats pitching = new PitchingStats();

        int inningsPitched = getDouble(total, IP_IDX).intValue();
        int hits = getInteger(total, HITS_IDX);

        pitching.setHits(hits);
        pitching.setAtBats(inningsPitched * 3 + hits);

        if (leagueBatting.getHits() > 0) {
            pitching.setDoubles(hits * leagueBatting.getDoubles() / leagueBatting.getHits());
            pitching.setTriples(hits * leagueBatting.getTriples() / leagueBatting.getHits());
        }

        pitching.setHomeRuns(getInteger(total, HOMERUN_IDX));
        pitching.setWalks(getInteger(total, WALKS_IDX));
        pitching.setStrikeouts(getInteger(total, STRIKEOUT_IDX));

        int currentSeason = extractDate().getYear();

        loadHistorical();

        historical.put(currentSeason, pitching);

        for (int i = 1; i < 5; i++) {
            if (historical.containsKey(currentSeason - i)) {
                pitching = pitching.add(historical.get(currentSeason - i));
            }
        }

        try {
            Jackson.getMapper(site).writeValue(getHistoricalFile(), this);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        return pitching;
    }

    private Integer getInteger(Elements line, int idx) {
        return Integer.valueOf(line.get(idx).text());
    }

    private Double getDouble(Elements line, int idx) {
        return Double.valueOf(line.get(idx).text());
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
