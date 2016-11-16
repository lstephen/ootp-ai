package com.github.lstephen.ootp.ai.site;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.roster.Team;

/** @author lstephen */
public interface RecordPredictor {

  Record getExpectedEndOfSeason(Id<Team> team);
}
