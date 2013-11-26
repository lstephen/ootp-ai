package com.ljs.ootp.ai.ootpx.site;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.ljs.ootp.ai.config.Config;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.scratch.util.ElementsUtil;
import com.ljs.scratch.util.Jackson;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.joda.time.DateTimeConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * TODO: Historical
 * @author lstephen
 */
public final class LeaguePitchingExtraction {

    @JsonIgnore
    private Site site;

    private Map<Integer, PitchingStats> historical = Maps.newHashMap();

    private LeaguePitchingExtraction() { }

    private LeaguePitchingExtraction(Site site) {
        this.site = site;
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
                historical = Jackson.getMapper(site).readValue(f, LeaguePitchingExtraction.class).historical;
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public PitchingStats extract() {
        Document doc = site.getPage("leagues/league_100_pitching_report.html").load();

        Elements line = doc.select("tr.title:has(td:containsOwn(" + site.getDefinition().getLeague() + " Totals)) + tr + tr").get(0).children();

        PitchingStats total = new PitchingStats();

        if (site.getDate().getMonthOfYear() >= DateTimeConstants.APRIL) {

            total.setHits(ElementsUtil.getInteger(line, 8));
            total.setAtBats((int) (ElementsUtil.getDouble(line, 7) * 3 + total.getHits()));

            BattingStats leagueBatting = site.getLeagueBatting();

            if (leagueBatting.getHits() > 0) {
                total.setDoubles(total.getHits() * leagueBatting.getDoubles() / leagueBatting.getHits());
                total.setTriples(total.getHits() * leagueBatting.getTriples() / leagueBatting.getHits());
            }

            total.setHomeRuns(ElementsUtil.getInteger(line, 11));
            total.setWalks(ElementsUtil.getInteger(line, 12));
            total.setStrikeouts(ElementsUtil.getInteger(line, 13));
        }

        int currentSeason = site.getDate().getYear();

        loadHistorical();

        historical.put(currentSeason, total);

        for (int i = 1; i < 5; i++) {
            if (historical.containsKey(currentSeason - i)) {
                total = total.add(historical.get(currentSeason - i));
            }
        }

        try {
            Jackson.getMapper(site).writeValue(getHistoricalFile(), this);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        return total;
    }

    public static LeaguePitchingExtraction create(Site site) {
        return new LeaguePitchingExtraction(site);
    }

}
