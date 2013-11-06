package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Roster;

/**
 *
 * @author lstephen
 */
public interface SingleTeam {

    String getName();

    Roster getRoster();

    Iterable<PlayerId> getInjuries();

}
