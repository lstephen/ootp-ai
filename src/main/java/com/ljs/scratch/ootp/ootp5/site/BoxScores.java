package com.ljs.scratch.ootp.ootp5.site;

import com.google.common.collect.Lists;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.elo.GameResult;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.Site;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class BoxScores {

    private final Site site;

    private BoxScores(Site site) {
        this.site = site;
    }

    private Document loadPage() {
        return site.getPage("box.html").load();
    }

    public Iterable<GameResult> getResults() {

        Document doc = loadPage();

        List<GameResult> results = Lists.newArrayList();

        Elements els = doc.select("tr.g ~ tr.g2");

        GameResult.Builder r = GameResult.builder();

        for (Element e : els) {
            String[] split = StringUtils.substringsBetween(e.html(), "<b>", "</b>");

            String teamName = split[0];
            Integer score = Integer.parseInt(split[1]);

            if (teamName.contains("Allstars")) {
                continue;
            }

            if (r.isVisitorSet()) {
                r.home(getTeamId(teamName), score);
                results.add(r.build());
                r = GameResult.builder();
            } else {
                r.visitor(getTeamId(teamName), score);
            }
        }

        Collections.reverse(results);

        return results;
    }

    private Id<Team> getTeamId(String teamName) {
        for (Id<Team> id : site.getTeamIds()) {
            if (site.getSingleTeam(id).getName().startsWith(teamName)) {
                return id;
            }
        }

        throw new IllegalStateException(teamName);
    }

    public static BoxScores create(Site site) {
        return new BoxScores(site);
    }

}
