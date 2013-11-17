package com.ljs.ootp.ai.ootp5.site;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.ljs.ootp.extract.html.Page;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Site;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class TopProspects {

    private final Page page;

    private TopProspects(Site site, Id<Team> team) {
        this.page = site.getPage("team" + team.get() + "pr.html");
    }


    public Optional<Integer> getPosition(PlayerId p) {
        Document doc = page.load();

        Elements els = doc.select("table.s0 tr:has(a[href=" + p.unwrap() + ".html]");

        if (els != null && !els.isEmpty()) {
            return Optional.of(
                Integer.parseInt(
                    CharMatcher.WHITESPACE.trimFrom(StringUtils.substringBefore(els.get(0).text(), " "))));
        }

        return Optional.absent();
    }


    public static TopProspects of(Site site, Id<Team> team) {
        return new TopProspects(site, team);
    }

    public static TopProspects of(Site site, Integer teamId) {
        return of(site, Id.<Team>valueOf(teamId.toString()));
    }

}
