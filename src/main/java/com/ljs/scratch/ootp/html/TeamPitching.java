package com.ljs.scratch.ootp.html;

import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.TeamStats;

/**
 *
 * @author lstephen
 */
public interface TeamPitching {

    Integer getYear();

    TeamStats<PitchingStats> extract();

}
