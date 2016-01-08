package com.github.lstephen.ootp.ai.ootp5.site;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.roster.Team;
import com.github.lstephen.ootp.ai.site.LeagueStructure;
import com.github.lstephen.ootp.ai.site.Site;

import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class LeagueStructureImpl implements LeagueStructure {

    private final Site site;

    private LeagueStructureImpl(Site site) {
        this.site = site;
    }

    private Document standings() {
        return Pages.standings(site).load();
    }

    public Iterable<League> getLeagues() {
        Document doc = standings();

        Elements els = doc.select("td.s5:containsOwn(Standings)");

        List<League> leagues = Lists.newArrayList();

        for (Element e : els) {
            if (e.text().contains(",")) {
                continue;
            }
            final String name = CharMatcher.WHITESPACE.trimFrom(StringUtils.substringBeforeLast(e.text(), "Standings"));
            leagues.add(new League() {
                public String getName() {
                    return name;
                }

                public Iterable<Division> getDivisions() {
                    return LeagueStructureImpl.this.getDivisions(name);
                }
            });
        }

        return leagues;
    }

    private Iterable<Division> getDivisions(final String league) {
        Document doc = standings();

        Element leagueEl = doc.select("tr:has(td.s5:containsOwn(" + league + " Standings))").first();

        List<Division> divisions = Lists.newArrayList();

        Element divEl = leagueEl.nextElementSibling();

        while (divEl.select("td.s5:containsOwn(Standings)").isEmpty()) {
            final String name = CharMatcher.WHITESPACE.trimFrom(divEl.select("td.s5").text());

            if (Strings.isNullOrEmpty(name)) {
                break;
            }

            divisions.add(new Division() {

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public Iterable<Id<Team>> getTeams() {
                    return LeagueStructureImpl.this.getTeams(league, name);
                }
            });

            divEl = divEl.nextElementSibling().nextElementSibling();
        }

        return divisions;
    }


    private Iterable<Id<Team>> getTeams(String league, String division) {
        Document doc = standings();

        Elements els = doc
            .select("tr:has(td.s5:containsOwn(" + league + ")) ~ tr:has(td.s5:containsOwn(" + division + ")) + tr")
            .first()
            .select("a");


        List<Id<Team>> teams = Lists.newArrayList();

        for (Element el : els) {
            teams.add(Id.<Team>valueOf(StringUtils.substringBetween(el.attr("href"), "team", ".html")));
        }

        return teams;
    }

    public static LeagueStructureImpl create(Site site) {
        return new LeagueStructureImpl(site);
    }

}
