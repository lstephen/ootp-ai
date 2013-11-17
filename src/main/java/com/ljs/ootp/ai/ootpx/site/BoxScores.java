package com.ljs.ootp.ai.ootpx.site;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.elo.GameResult;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Site;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class BoxScores {

    private final Site site;

    private final LocalDate date;

    private BoxScores(Site site, LocalDate date) {
        this.site = site;
        this.date = date;
    }

    public Iterable<GameResult> getResults() {
        Document doc = Pages.boxScores(site, date).load();

        Set<GameResult> results = Sets.newHashSet();

        Elements els = doc.select("table[width=405]");

        for (Element e : els) {
            Elements gameEls = e.select("tr");

            Element visitorTr = gameEls.get(1);
            Id<Team> visitorId = Id.valueOf(
                StringUtils.substringBetween(
                    visitorTr.select("a").first().attr("href"),
                    "team_",
                    ".html"));

            Integer visitorScore = Integer.parseInt(
                visitorTr.select("td.icgb").text());

            Element homeTr = gameEls.get(2);
            Id<Team> homeId = Id.valueOf(
                StringUtils.substringBetween(
                    homeTr.select("a").first().attr("href"),
                    "team_",
                    ".html"));

            Integer homeScore = Integer.parseInt(
                homeTr.select("td.icgb").text());

            results.add(GameResult
                .builder()
                .visitor(visitorId, visitorScore)
                .home(homeId, homeScore)
                .build());
        }

        return results;
    }

    public static BoxScores create(Site site, LocalDate date) {
        return new BoxScores(site, date);
    }

    public static Iterable<BoxScores> create(Site site, LocalDate from, LocalDate to) {
        LocalDate current = from;

        List<BoxScores> boxScores = Lists.newArrayList();

        while (current.isBefore(to)) {
            boxScores.add(create(site, current));

            current = current.plusDays(1);
        }

        return boxScores;
    }
}
