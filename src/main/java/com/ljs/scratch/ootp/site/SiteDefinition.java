package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.ratings.RatingsDefinition;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.stats.PitcherOverall;

/**
 *
 * @author lstephen
 */
public interface SiteDefinition extends RatingsDefinition {

    String getName();
    Id<Team> getTeam();
    String getSiteRoot();
    Version getType();
    String getLeague();
    Integer getNumberOfTeams();
    PitcherOverall getPitcherSelectionMethod();

}
