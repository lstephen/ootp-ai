package com.ljs.scratch.ootp.ootpx.site;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Page;
import com.ljs.scratch.ootp.html.PageFactory;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.rating.Scale;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.Salary;
import com.ljs.scratch.ootp.site.SingleTeam;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Standings;
import com.ljs.scratch.ootp.site.TeamBatting;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.util.ElementsUtil;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.fest.util.Strings;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class OotpX implements Site {

    private SiteDefinition definition;

    private ImmutableSet<PlayerId> fas;

    private OotpX(SiteDefinition definition) {
        this.definition = definition;
    }

    @Override
    public Roster extractRoster() {
        return RosterExtraction.create(this).extract(definition.getTeam());
    }

    @Override
    public Team extractTeam() {
        return TeamExtraction.create(this).extractTeam(definition.getTeam());
    }

    @Override
    public Integer getCurrentSalary(Player p) {
        return PlayerExtraction.create(this).getCurrentSalary(p.getId());
    }

    private Integer getNextSalary(Player p) {
        Document doc = Pages.salary(this, definition.getTeam()).load();

        Elements els = doc.select("table.lposhadow tr td:has(a[href~=" + p.getId().unwrap() + "]) + td + td");

        if (els.isEmpty()) {
            return 0;
        }

        String raw = CharMatcher.WHITESPACE.trimFrom(els.text());

        if (Strings.isNullOrEmpty(raw)) {
            return 0;
        }

        try {
            Double base = NumberFormat.getNumberInstance().parse(raw.substring(1)).doubleValue();

            if (raw.contains("k")) {
                base = base * 1000;
            }

            if (raw.contains("m")) {
                base = base * 1000000;
            }

            return base.intValue();
        } catch (ParseException e) {
            throw Throwables.propagate(e);
        }

    }

    @Override
    public LocalDate getDate() {
        Document doc = Pages.leagueBatting(this).load();
        String date = doc.select("span:matchesOwn([0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9])").text();
        return LocalDate.parse(date, DateTimeFormat.forPattern("MM-dd-yyyy"));
    }

    @Override
    public SiteDefinition getDefinition() {
        return definition;
    }

    @Override
    public Iterable<Player> getDraft() {
        return PlayerList.from(this, "leagues/league_100_rookie_draft_pool_report.html").extract();
    }

    @Override
    public Iterable<Player> getFreeAgents() {
        return Iterables.concat(
            PlayerList.from(this, "leagues/league_100_free_agents_report_0.html").extract(),
            PlayerList.from(this, "leagues/league_100_free_agents_report_1.html").extract());
    }

    @Override
    public BattingStats getLeagueBatting() {
        return LeagueBattingExtraction.create(this).extract();
    }

    @Override
    public PitchingStats getLeaguePitching() {
        return LeaguePitchingExtraction.create(this).extract();
    }

    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public Iterable<Id<Team>> getTeamIds() {
        Document doc = Pages.standings(this).load();

        Set<Id<Team>> ids = Sets.newHashSet();

        for (Element el : doc.select("a[href~=teams/]")) {
            String href = el.attr("href");

            ids.add(Id.<Team>valueOf(StringUtils.substringBetween(href, "team_", ".html")));
        }

        return ids;
    }

    @Override
    public Page getPage(String url, Object... args) {
        return PageFactory.create(definition.getSiteRoot(), String.format(url, args));
    }

    @Override
    public PitcherOverall getPitcherSelectionMethod() {
        return PitcherOverall.FIP;
    }

    @Override
    public Player getPlayer(PlayerId id) {
        return PlayerExtraction.create(this).extract(id);
    }

    @Override
    public Iterable<Player> getPlayers(PlayerId... ids) {
        return getPlayers(Arrays.asList(ids));
    }

    @Override
    public Iterable<Player> getPlayers(
        Iterable<PlayerId> ids) {
        List<Player> ps = Lists.newArrayList();

        for (PlayerId id : ids) {
            ps.add(getPlayer(id));
        }

        return ps;
    }

    @Override
    public Iterable<Player> getRuleFiveDraft() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Salary getSalary() {
        return getSalary(definition.getTeam());
    }

    @Override
    public Salary getSalary(Id<Team> id) {
        return new Salary() {
            public Integer getCurrentSalary(Player p) {
                return OotpX.this.getCurrentSalary(p);
            }

            public Integer getNextSalary(Player p) {
                return OotpX.this.getNextSalary(p);
            }
        };
    }

    @Override
    public Iterable<Player> getSalariedPlayers(Id<Team> id) {
        return PlayerList
            .from(this, "teams/team_%s_player_salary_report.html", id.get())
            .extract();
    }

    @Override
    public Salary getSalary(int teamId) {
        return getSalary(Id.<Team>valueOf(teamId));
    }

    @Override
    public SingleTeam getSingleTeam() {
        return getSingleTeam(definition.getTeam());
    }

    @Override
    public SingleTeam getSingleTeam(final Id<Team> id) {
        return new SingleTeam() {
            @Override
            public String getName() {
                Document doc = Pages.standings(OotpX.this).load();
                return doc.select("a[href~=team_" + id.get() + ".html").first().text();
            }

            @Override
            public Roster getRoster() {
                return RosterExtraction.create(OotpX.this).extract(id);
            }

            @Override
            public Iterable<PlayerId> getInjuries() {
                return TeamExtraction.create(OotpX.this).getInjuries(id);
            }
        };
    }

    @Override
    public SingleTeam getSingleTeam(int teamId) {
        return getSingleTeam(Id.<Team>valueOf(teamId));
    }

    @Override
    public Standings getStandings() {
        final Document doc = Pages.standings(this).load();

        final Elements standings = doc.select("tr.title3 + tr");

        return new Standings() {
            public Integer getWins(Id<Team> team) {
                Elements els = standings.select("tbody > tr:has(a[href~=team_" + team.get() + "]) > td");
                return ElementsUtil.getInteger(els, 1);
            }

            public Integer getLosses(Id<Team> team) {
                Elements els = standings.select("tbody > tr:has(a[href~=team_" + team.get() + "]) > td");
                return ElementsUtil.getInteger(els, 2);
            }
        };
    }

    @Override
    public TeamBatting getTeamBatting() {
        return TeamBattingImpl.create(this, definition.getTeam());
    }

    @Override
    public TeamPitchingImpl getTeamPitching() {
        return TeamPitchingImpl.create(this, definition.getTeam());
    }

    @Override
    public Optional<Integer> getTeamTopProspectPosition(PlayerId id) {
        for (Id<Team> t : getTeamIds()) {
            Document doc = Pages.topProspects(this, t).load();

            Elements els = doc.select("table.lposhadow tr:has(a[href~=" + id.unwrap() + "])");

            if (els.isEmpty()) {
                continue;
            }

            Integer rank = Integer.parseInt(els.first().child(0).text());

            return Optional.of(rank);
        }
        return Optional.absent();
    }

    @Override
    public Version getType() {
        return definition.getType();
    }

    @Override
    public Iterable<Player> getWaiverWire() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFutureFreeAgent(Player p) {
        return futureFreeAgents().contains(p.getId());
    }

    @Override
    public Predicate<Player> isFutureFreeAgent() {
        return new Predicate<Player>() {
            @Override
            public boolean apply(Player input) {
                return isFutureFreeAgent(input);
            }
        };
    }

    private ImmutableSet<PlayerId> futureFreeAgents() {
        if (fas == null) {
            fas = ImmutableSet.copyOf(Iterables.concat(
                PlayerList.from(this, "leagues/league_100_upcoming_free_agents_report_0.html").extractIds(),
                PlayerList.from(this, "leagues/league_100_upcoming_free_agents_report_1.html").extractIds()));
        }
        return fas;
    }

    @Override
    public boolean isInjured(Player p) {
        for (Id<Team> t : getTeamIds()) {
            if (Iterables.contains(getSingleTeam(t).getInjuries(), p.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Scale<?> getAbilityRatingScale() {
        return definition.getAbilityRatingScale();
    }

    @Override
    public Scale<?> getPotentialRatingScale() {
        return definition.getPotentialRatingsScale();
    }

    public static OotpX create(SiteDefinition def) {
        return new OotpX(def);
    }

}
