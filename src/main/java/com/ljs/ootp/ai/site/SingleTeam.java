package com.ljs.ootp.ai.site;

import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.roster.Roster;

/**
 *
 * @author lstephen
 */
public interface SingleTeam {

    String getName();

    Roster getRoster();

    Iterable<PlayerId> getInjuries();

}
