package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.team.TeamId;

/**
 *
 * @author lstephen
 */
public interface SiteDefinition {

    String getName();
    TeamId getTeam();
    String getSiteRoot();
    Version getType();
    String getLeague();
    Integer getNumberOfTeams();
    PitcherOverall getPitcherSelectionMethod();

}
