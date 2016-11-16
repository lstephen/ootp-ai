package com.github.lstephen.ootp.ai.site;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.roster.Team;

/** @author lstephen */
public interface Standings {

  @Deprecated
  Integer getWins(Id<Team> team);

  @Deprecated
  Integer getLosses(Id<Team> team);

  Record getRecord(Id<Team> team);
}
