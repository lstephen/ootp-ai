package com.ljs.ootp.ai.site;

import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.roster.Team;

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
