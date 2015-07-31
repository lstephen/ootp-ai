package com.github.lstephen.ootp.ai.site;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.PlayerId;
import com.github.lstephen.ootp.ai.rating.Scale;
import com.github.lstephen.ootp.ai.roster.Roster;
import com.github.lstephen.ootp.ai.roster.Team;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.PitcherOverall;
import com.github.lstephen.ootp.ai.stats.PitchingStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;
import com.github.lstephen.ootp.extract.html.Page;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

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

    LeagueStructure getLeagueStructure();

    void clearCache();

    Page getPage(String url, Object... args);

    PitcherOverall getPitcherSelectionMethod();

    Player getPlayer(PlayerId id);

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

    TeamStats<BattingStats> getTeamBatting();

    TeamStats<PitchingStats> getTeamPitching();

    Optional<Integer> getTeamTopProspectPosition(PlayerId id);

    Version getType();

    Iterable<Player> getWaiverWire();

    boolean isFutureFreeAgent(Player p);

    Predicate<Player> isFutureFreeAgent();

    boolean isInjured(Player p);

    Scale<?> getAbilityRatingScale();
    Scale<?> getPotentialRatingScale();
    Scale<?> getBuntScale();
    Scale<?> getRunningScale();

    Printable getPowerRankingsReport(RecordPredictor recordPredictor);

}
