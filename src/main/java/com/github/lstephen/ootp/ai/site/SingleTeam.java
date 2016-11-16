package com.github.lstephen.ootp.ai.site;

import com.github.lstephen.ootp.ai.player.PlayerId;
import com.github.lstephen.ootp.ai.roster.Roster;

/** @author lstephen */
public interface SingleTeam {

  String getName();

  Roster getRoster();

  Iterable<PlayerId> getInjuries();
}
