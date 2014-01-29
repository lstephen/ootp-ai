package com.ljs.ootp.ai.ootpx.site;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Site;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.api.Assertions;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class TeamExtraction {

    private final Site site;

    private TeamExtraction(Site site) {
        Assertions.assertThat(site).isNotNull();

        this.site = site;
    }

    public Team extractTeam(Id<Team> id) {
        Team team = Team.create(
            Iterables.concat(
                PlayerList.roster(site, id).extract(),
                PlayerList.minorLeagues(site, id).extract()));

        team.addInjury(site.getPlayers(getInjuries(id)));

        return team;

    }

	public Iterable<PlayerId> getInjuries(Id<Team> id) {
		Set<PlayerId> injured = Sets.newHashSet();

		for (Id<Team> t : Iterables.concat(ImmutableSet.of(id), getMinorLeagueTeams(id))) {
			Iterables.addAll(injured, getSingleTeamInjuries(t));
		}

		return injured;
	}

    private Iterable<PlayerId> getSingleTeamInjuries(Id<Team> id) {
        Document doc = Pages.team(site, id).load();



        Elements els = doc.select("tr.title:has(td:containsOwn(Injuries)) ~ tr + tr");

        Set<PlayerId> injured = Sets.newHashSet();

        for (Element el : els) {
            Boolean isInjured = false;
            if (el.child(2).text().contains("weeks") || el.child(2).text().contains("months")) {
                isInjured = true;
            }
            if (el.child(3).text().contains("day(s) left")) {
                isInjured = true;
            }

            if (isInjured) {
                String playerId = StringUtils.substringBetween(el.child(0).child(0).attr("href"), "players/", ".html");
                injured.add(new PlayerId(playerId));
            };
        }

        return injured;
    }

	private Set<Id<Team>> getMinorLeagueTeams(Id<Team> id) {
		Document doc = Pages.team(site, id).load();

		Elements els = doc.select("tr.capt1:has(td:containsOwn(Minor League System)) ~ tr");

		Elements as = els.select("a[href~=team]");

		Set<Id<Team>> ids = Sets.newHashSet();

		for (Element a : as) {
			ids.add(Id.<Team>valueOf(StringUtils.substringBetween(a.attr("href"), "team_", ".html")));
		}

		return ids;
	}

    public static TeamExtraction create(Site site) {
        return new TeamExtraction(site);
    }

}
