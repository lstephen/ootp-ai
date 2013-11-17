package com.ljs.ootp.ai.site;

import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public interface TeamBatting {

    Integer getYear();

    TeamStats<BattingStats> extract();

}
