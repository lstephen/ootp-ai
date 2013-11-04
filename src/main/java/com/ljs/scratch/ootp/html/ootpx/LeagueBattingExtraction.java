package com.ljs.scratch.ootp.html.ootpx;

import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.util.ElementsUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * TODO: Historical
 * @author lstephen
 */
public final class LeagueBattingExtraction {

    private final Site site;

    private LeagueBattingExtraction(Site site) {
        this.site = site;
    }

    public BattingStats extract() {
        Document doc = Pages.leagueBatting(site).load();

        Elements line = doc.select("tr.title:has(td:containsOwn(" + site.getDefinition().getLeague() + " Totals)) + tr + tr").get(0).children();

        BattingStats total = new BattingStats();

        total.setAtBats(ElementsUtil.getInteger(line, 8));
        total.setHits(ElementsUtil.getInteger(line, 9));
        total.setDoubles(ElementsUtil.getInteger(line, 10));
        total.setTriples(ElementsUtil.getInteger(line, 11));
        total.setHomeRuns(ElementsUtil.getInteger(line, 6));
        total.setWalks(ElementsUtil.getInteger(line, 12));

        return total;
    }

    public static LeagueBattingExtraction create(Site site) {
        return new LeagueBattingExtraction(site);
    }

}
