package com.github.lstephen.ootp.ai.value;

import com.github.lstephen.ootp.ai.player.Player;

/** @author lstephen */
public interface SalaryPredictor {

  Integer predictNow(Player p);

  Integer predictNext(Player p);
}
