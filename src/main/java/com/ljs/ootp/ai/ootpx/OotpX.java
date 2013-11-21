package com.ljs.ootp.ai.ootpx;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.config.Config;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.ootpx.report.PowerRankingsReport;
import com.ljs.ootp.ai.ootpx.site.LeagueBattingExtraction;
import com.ljs.ootp.ai.ootpx.site.LeaguePitchingExtraction;
import com.ljs.ootp.ai.ootpx.site.LeagueStructureImpl;
import com.ljs.ootp.ai.ootpx.site.Pages;
import com.ljs.ootp.ai.ootpx.site.PlayerExtraction;
import com.ljs.ootp.ai.ootpx.site.PlayerList;
import com.ljs.ootp.ai.ootpx.site.RosterExtraction;
import com.ljs.ootp.ai.ootpx.site.TeamBattingImpl;
import com.ljs.ootp.ai.ootpx.site.TeamExtraction;
import com.ljs.ootp.ai.ootpx.site.TeamPitchingImpl;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.rating.Scale;
import com.ljs.ootp.ai.roster.Roster;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.LeagueStructure;
import com.ljs.ootp.ai.site.Record;
import com.ljs.ootp.ai.site.RecordPredictor;
import com.ljs.ootp.ai.site.Salary;
import com.ljs.ootp.ai.site.SingleTeam;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.site.SiteDefinition;
import com.ljs.ootp.ai.site.Standings;
import com.ljs.ootp.ai.site.Version;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.SplitStats;
import com.ljs.ootp.ai.stats.TeamStats;
import com.ljs.ootp.extract.html.Page;
import com.ljs.ootp.extract.html.PageFactory;
import com.ljs.ootp.extract.html.loader.DiskCachingLoader;
import com.ljs.ootp.extract.html.loader.PageLoader;
import com.ljs.ootp.extract.html.loader.PageLoaderBuilder;
import com.ljs.scratch.util.ElementsUtil;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.fest.util.Strings;
import org.joda.time.DateTimeConstants;
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

            if (p.getSalary().endsWith("r")) {
                return Math.max(base.intValue(), getCurrentSalary(p));
            } else if (p.getSalary().endsWith("a")) {
                return Math.max(base.intValue(), (int) (getCurrentSalary(p) * 1.05));
            } else {
                return base.intValue();
            }
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
    public LeagueStructure getLeagueStructure() {
        return LeagueStructureImpl.create(this);
    }

    @Override
    public Page getPage(String url, Object... args) {
        try {
            PageLoader loader = PageLoaderBuilder
                .create()
                .diskCache(Config.createDefault().getValue("cache.dir").or(DiskCachingLoader.DEFAULT_CACHE_DIR))
                .inMemoryCache()
                .build();



            return PageFactory
                .create(loader)
                .getPage(definition.getSiteRoot(), String.format(url, args));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
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
        if (getDate().getMonthOfYear() < DateTimeConstants.APRIL) {
            return new Standings() {
                public Integer getWins(Id<Team> team) {
                    return 0;
                }

                public Integer getLosses(Id<Team> team) {
                    return 0;
                }

                public Record getRecord(Id<Team> team) {
                    return Record.create(0, 0);
                }
            };
        }

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

            public Record getRecord(Id<Team> team) {
                return Record.create(getWins(team), getLosses(team));
            }
        };
    }

    @Override
    public TeamStats<BattingStats> getTeamBatting() {
        if (getDate().getMonthOfYear() < DateTimeConstants.APRIL) {
            return TeamStats.create(ImmutableMap.<Player, SplitStats<BattingStats>>of());
        } else {
            return TeamBattingImpl.create(this, definition.getTeam()).extract();
        }
    }

    @Override
    public TeamStats<PitchingStats> getTeamPitching() {
        if (getDate().getMonthOfYear() < DateTimeConstants.APRIL) {
            return TeamStats.create(ImmutableMap.<Player, SplitStats<PitchingStats>>of());
        } else {
            return TeamPitchingImpl.create(this, definition.getTeam()).extract();
        }
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

    @Override
    public Printable getPowerRankingsReport(RecordPredictor recordPredictor) {
        return PowerRankingsReport.create(this, recordPredictor);
    }

    public static OotpX create(SiteDefinition def) {
        return new OotpX(def);
    }

}
