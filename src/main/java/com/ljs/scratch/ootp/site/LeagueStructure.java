package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.roster.Team;

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
