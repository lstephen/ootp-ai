package com.github.lstephen.ootp.ai.site;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.roster.Team;

/** @author lstephen */
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
