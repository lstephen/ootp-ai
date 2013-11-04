package com.ljs.scratch.ootp.html;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.roster.Team;

/**
 *
 * @author lstephen
 */
public interface Standings {

    Integer getWins(Id<Team> team);

    Integer getLosses(Id<Team> team);

}
