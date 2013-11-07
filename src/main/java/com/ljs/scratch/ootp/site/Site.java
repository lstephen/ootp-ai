package com.ljs.scratch.ootp.site;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.rating.Scale;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
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

    Iterable<Player> getDraft();

    Iterable<Player> getFreeAgents();

    BattingStats getLeagueBatting();

    PitchingStats getLeaguePitching();

    String getName();

    Iterable<Id<Team>> getTeamIds();

    Page getPage(String url, Object... args);

    PitcherOverall getPitcherSelectionMethod();

    Player getPlayer(PlayerId id);

    Iterable<Player> getPlayers(PlayerId... ids);

    Iterable<Player> getPlayers(
        Iterable<PlayerId> ids);

    Iterable<Player> getRuleFiveDraft();

    Iterable<Player> getSalariedPlayers(Id<Team> id);

    Salary getSalary();

    Salary getSalary(Id<Team> id);

    Salary getSalary(int teamId);

    SingleTeam getSingleTeam();

    SingleTeam getSingleTeam(
        Id<Team> id);

    SingleTeam getSingleTeam(int teamId);

    Standings getStandings();

    TeamBatting getTeamBatting();

    TeamPitching getTeamPitching();

    Optional<Integer> getTeamTopProspectPosition(PlayerId id);

    Version getType();

    Iterable<Player> getWaiverWire();

    boolean isFutureFreeAgent(Player p);

    Predicate<Player> isFutureFreeAgent();

    boolean isInjured(Player p);

    Scale<?> getAbilityRatingScale();
    Scale<?> getPotentialRatingScale();

}
