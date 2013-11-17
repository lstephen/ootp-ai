package com.ljs.ootp.ai.site;

import com.ljs.ootp.ai.player.Player;

/**
 *
 * @author lstephen
 */
public interface Salary {

    Integer getCurrentSalary(Player p);

    Integer getNextSalary(Player p);

}
