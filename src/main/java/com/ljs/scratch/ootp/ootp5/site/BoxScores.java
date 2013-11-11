package com.ljs.scratch.ootp.ootp5.site;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
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

    public Iterable<Result> getResults() {

        Document doc = loadPage();

        List<Result> results = Lists.newArrayList();

        Elements els = doc.select("tr.g ~ tr.g2");

        Result r = Result.create();

        for (Element e : els) {
            String[] split = StringUtils.substringsBetween(e.html(), "<b>", "</b>");

            String teamName = split[0];
            Integer score = Integer.parseInt(split[1]);

            if (r.visitorName == null) {
                r.visitorName = teamName;
                r.visitorScore = score;
            } else {
                r.homeName = teamName;
                r.homeScore = score;

                results.add(r);
                r = Result.create();
            }
        }

        Collections.reverse(results);

        return results;
    }

    public static BoxScores create(Site site) {
        return new BoxScores(site);
    }

    public static final class Result {

        private String visitorName;

        private Integer visitorScore;

        private String homeName;

        private Integer homeScore;

        private Result() { }

        public String getVisitorName() {
            return visitorName;
        }

        public Integer getVisitorScore() {
            return visitorScore;
        }

        public String getHomeName() {
            return homeName;
        }

        public Integer getHomeScore() {
            return homeScore;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                .add("visitorName", visitorName)
                .add("visitorScore", visitorScore)
                .add("homeName", homeName)
                .add("homeScore", homeScore)
                .toString();
        }

        private static Result create() {
            return new Result();
        }

    }

}
