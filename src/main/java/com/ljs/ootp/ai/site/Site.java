package com.ljs.ootp.ai.site;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.roster.Roster;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import com.ljs.ootp.extract.html.Page;
import com.ljs.ootp.extract.html.rating.Scale;
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

    Printable getPowerRankingsReport(RecordPredictor recordPredictor);

}
