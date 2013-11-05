package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Salary;
import com.ljs.scratch.ootp.html.SingleTeam;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.Standings;
import com.ljs.scratch.ootp.html.TeamBatting;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.TopProspects;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.html.page.PageFactory;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
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
        if (!Strings.isNullOrEmpty(p.getSalary()) && p.getSalary().charAt(0) == '$') {
            try {
                return NumberFormat.getNumberInstance().parse(p.getSalary().substring(1)).intValue();
            } catch (ParseException e) {
                throw Throwables.propagate(e);
            }
        }
        return 0;
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
        return ImmutableSet.<Player>of();
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
                return 0;
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
    public String getSalary(Player p) {
        throw new UnsupportedOperationException();
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
        };
    }

    @Override
    public SingleTeam getSingleTeam(int teamId) {
        return getSingleTeam(Id.<Team>valueOf(teamId));
    }

    @Override
    public Standings getStandings() {
        return new Standings() {
            public Integer getWins(Id<Team> team) { return 0; }
            public Integer getLosses(Id<Team> team) { return 0; }
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
        throw new UnsupportedOperationException();
    }

    @Override
    public TopProspects getTopProspects(Integer teamId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TopProspects getTopProspects(
        Id<Team> id) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    public static OotpX create(SiteDefinition def) {
        return new OotpX(def);
    }

}
