package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.roster.Team;

/**
 *
 * @author lstephen
 */
public interface Standings {

    @Deprecated
    Integer getWins(Id<Team> team);

    @Deprecated
    Integer getLosses(Id<Team> team);

    Record getRecord(Id<Team> team);

}
