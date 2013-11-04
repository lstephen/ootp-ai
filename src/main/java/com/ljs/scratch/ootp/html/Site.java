package com.ljs.scratch.ootp.html;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.ljs.scratch.ootp.data.Id;
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
public interface Site {

    Roster extractRoster();

    Team extractTeam();

    Integer getCurrentSalary(Player p);

    LocalDate getDate();

    SiteDefinition getDefinition();

    PlayerList getDraft();

    PlayerList getFreeAgents();

    LeagueBatting getLeagueBatting();

    LeaguePitching getLeaguePitching();

    MinorLeagues getMinorLeagues();

    MinorLeagues getMinorLeagues(
        Id<Team> id);

    String getName();

    int getNumberOfTeams();

    Page getPage(String url);

    PitcherOverall getPitcherSelectionMethod();

    SinglePlayer getPlayer(PlayerId id);

    Iterable<Player> getPlayers(PlayerId... ids);

    Iterable<Player> getPlayers(
        Iterable<PlayerId> ids);

    PlayerList getRuleFiveDraft();

    Salary getSalary();

    Salary getSalary(
        Id<Team> id);

    Salary getSalary(int teamId);

    String getSalary(Player p);

    SingleTeam getSingleTeam();

    SingleTeam getSingleTeam(
        Id<Team> id);

    SingleTeam getSingleTeam(int teamId);

    Standings getStandings();

    TeamBatting getTeamBatting();

    TeamPitching getTeamPitching();

    TeamRatings getTeamRatings();

    TeamRatings getTeamRatings(Integer teamId);

    TeamRatings getTeamRatings(
        Id<Team> id);

    Optional<Integer> getTeamTopProspectPosition(PlayerId id);

    TopProspects getTopProspects(Integer teamId);

    TopProspects getTopProspects(
        Id<Team> id);

    Version getType();

    PlayerList getWaiverWire();

    boolean isFutureFreeAgent(Player p);

    Predicate<Player> isFutureFreeAgent();

    boolean isInjured(Player p);

}
