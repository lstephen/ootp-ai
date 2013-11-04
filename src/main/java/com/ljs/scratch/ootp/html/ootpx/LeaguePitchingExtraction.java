package com.ljs.scratch.ootp.html.ootpx;

import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.util.ElementsUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * TODO: Historical
 * @author lstephen
 */
public final class LeaguePitchingExtraction {

    private final Site site;

    private LeaguePitchingExtraction(Site site) {
        this.site = site;
    }

    public PitchingStats extract() {
        Document doc = site.getPage("leagues/league_100_pitching_report.html").load();

        Elements line = doc.select("tr.title:has(td:containsOwn(" + site.getDefinition().getLeague() + " Totals)) + tr + tr").get(0).children();

        PitchingStats total = new PitchingStats();

        total.setHits(ElementsUtil.getInteger(line, 8));
        total.setAtBats((int) (ElementsUtil.getDouble(line, 7) * 3 + total.getHits()));
        //total.setDoubles(ElementsUtil.getInteger(line, 10));
        //total.setTriples(ElementsUtil.getInteger(line, 11));
        total.setHomeRuns(ElementsUtil.getInteger(line, 11));
        total.setWalks(ElementsUtil.getInteger(line, 12));
        total.setStrikeouts(ElementsUtil.getInteger(line, 13));

        return total;
    }

    public static LeaguePitchingExtraction create(Site site) {
        return new LeaguePitchingExtraction(site);
    }

}
