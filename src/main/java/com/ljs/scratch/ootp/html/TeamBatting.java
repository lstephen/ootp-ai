package com.ljs.scratch.ootp.html;

import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public interface TeamBatting {

    Integer getYear();

    TeamStats<BattingStats> extract();

}
