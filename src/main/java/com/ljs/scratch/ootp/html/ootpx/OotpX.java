package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.LeagueBatting;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.LeaguePitching;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.MinorLeagues;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.PlayerList;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.Salary;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.SinglePlayer;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.SingleTeam;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.Standings;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.TeamBatting;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.TeamPitching;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.TeamRatings;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.TopProspects;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import org.joda.time.LocalDate;

/**
 *
 * @author lstephen
 */
public class OotpX implements Site {

    private SiteDefinition def;

    private OotpX(SiteDefinition def) {
        this.def = def;
    }

    @Override
    public Roster extractRoster() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Team extractTeam() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getCurrentSalary(Player p) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate getDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SiteDefinition getDefinition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerList getDraft() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerList getFreeAgents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LeagueBatting getLeagueBatting() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LeaguePitching getLeaguePitching() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNumberOfTeams() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page getPage(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PitcherOverall getPitcherSelectionMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SinglePlayer getPlayer(PlayerId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Player> getPlayers(PlayerId... ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Player> getPlayers(
        Iterable<PlayerId> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerList getRuleFiveDraft() {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public TeamPitching getTeamPitching() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerList getWaiverWire() {
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
