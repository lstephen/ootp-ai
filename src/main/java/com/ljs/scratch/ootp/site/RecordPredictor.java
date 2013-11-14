package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.roster.Team;

/**
 *
 * @author lstephen
 */
public interface RecordPredictor {

    Record getExpectedEndOfSeason(Id<Team> team);

}
