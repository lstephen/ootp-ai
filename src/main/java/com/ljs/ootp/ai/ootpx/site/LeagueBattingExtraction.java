package com.ljs.ootp.ai.ootpx.site;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.ljs.ootp.ai.config.Config;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.stats.BattingStats;
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
public final class LeagueBattingExtraction {

    @JsonIgnore
    private Site site;

    private Map<Integer, BattingStats> historical = Maps.newHashMap();

    private LeagueBattingExtraction() { }

    private LeagueBattingExtraction(Site site) {
        this.site = site;
    }

    private File getHistoricalFile() {
        try {
            String historyDirectory = Config.createDefault().getValue("history.dir").or("c:/ootp/history");
            return new File(historyDirectory + "/" + site.getName() + "league.batting.json");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void loadHistorical() {

        File f = getHistoricalFile();

        if (f.exists()) {
            try {
                historical =
                    Jackson.getMapper(site).readValue(f, LeagueBattingExtraction.class).historical;
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public BattingStats extract() {
        Document doc = Pages.leagueBatting(site).load();

        Elements line = doc.select("tr.title:has(td:containsOwn(" + site.getDefinition().getLeague() + " Totals)) + tr + tr").get(0).children();

        BattingStats total = new BattingStats();

        if (site.getDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
            total.setAtBats(ElementsUtil.getInteger(line, 8));
            total.setHits(ElementsUtil.getInteger(line, 9));
            total.setDoubles(ElementsUtil.getInteger(line, 10));
            total.setTriples(ElementsUtil.getInteger(line, 11));
            total.setHomeRuns(ElementsUtil.getInteger(line, 6));
            total.setWalks(ElementsUtil.getInteger(line, 12));
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

    public static LeagueBattingExtraction create(Site site) {
        return new LeagueBattingExtraction(site);
    }

}
