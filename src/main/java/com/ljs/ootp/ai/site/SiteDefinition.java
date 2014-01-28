package com.ljs.ootp.ai.site;

import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.player.ratings.RatingsDefinition;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.extract.html.rating.Scale;

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

    Site getSite();
    Scale<?> getAbilityRatingScale();
    Scale<?> getPotentialRatingsScale();

}
