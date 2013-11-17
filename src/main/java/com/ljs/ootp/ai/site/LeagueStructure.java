package com.ljs.ootp.ai.site;

import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.roster.Team;

/**
 *
 * @author lstephen
 */
public interface LeagueStructure {

    Iterable<League> getLeagues();

    interface League {
        String getName();
        Iterable<Division> getDivisions();
    }

    interface Division {
        String getName();
        Iterable<Id<Team>> getTeams();
    }

}
