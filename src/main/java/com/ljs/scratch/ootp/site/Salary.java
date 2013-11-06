package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.player.Player;

/**
 *
 * @author lstephen
 */
public interface Salary {

    Integer getCurrentSalary(Player p);

    Integer getNextSalary(Player p);

}
