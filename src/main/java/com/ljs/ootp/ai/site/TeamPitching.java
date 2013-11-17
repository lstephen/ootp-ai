package com.ljs.ootp.ai.site;

import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public interface TeamPitching {

    Integer getYear();

    TeamStats<PitchingStats> extract();

}
