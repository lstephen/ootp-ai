package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.TeamBatting;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.MinorLeagues;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.Salary;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.SingleTeam;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.Standings;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.TeamRatings;
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
import java.util.Arrays;
import java.util.List;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public class OotpX implements Site {

    private SiteDefinition definition;

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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Player> getFreeAgents() {
        return PlayerList.from(this, "leagues/league_100_free_agents_report_0.html").extract();
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
    public MinorLeagues getMinorLeagues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MinorLeagues getMinorLeagues(
        Id<Team> id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public int getNumberOfTeams() {
        return definition.getNumberOfTeams();
    }

    @Override
    public Page getPage(String url) {
        return PageFactory.create(definition.getSiteRoot(), url);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Salary getSalary(
        Id<Team> id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Salary getSalary(int teamId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSalary(Player p) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleTeam getSingleTeam() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleTeam getSingleTeam(
        Id<Team> id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleTeam getSingleTeam(int teamId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Standings getStandings() {
        throw new UnsupportedOperationException();
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
    public TeamRatings getTeamRatings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TeamRatings getTeamRatings(Integer teamId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TeamRatings getTeamRatings(
        Id<Team> id) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate<Player> isFutureFreeAgent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInjured(Player p) {
        throw new UnsupportedOperationException();
    }

    public static OotpX create(SiteDefinition def) {
        return new OotpX(def);
    }

}
