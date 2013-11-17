package com.ljs.ootp.ai.site;

import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.roster.Team;

/**
 *
 * @author lstephen
 */
public interface RecordPredictor {

    Record getExpectedEndOfSeason(Id<Team> team);

}
