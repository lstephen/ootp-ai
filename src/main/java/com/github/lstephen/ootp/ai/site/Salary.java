package com.github.lstephen.ootp.ai.site;

import com.github.lstephen.ootp.ai.player.Player;

/**
 *
 * @author lstephen
 */
public interface Salary {

    Integer getCurrentSalary(Player p);

    Integer getNextSalary(Player p);

}
