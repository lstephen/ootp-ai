package com.github.lstephen.ootp.ai.site;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.player.ratings.RatingsDefinition;
import com.github.lstephen.ootp.ai.rating.Scale;
import com.github.lstephen.ootp.ai.roster.Team;

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

    Site getSite();
    Scale<?> getAbilityRatingScale();
    Scale<?> getPotentialRatingsScale();
    Scale<?> getBuntScale();
    Scale<?> getRunningScale();

}
