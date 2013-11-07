package com.ljs.scratch.ootp.regression;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.TeamStats;
import com.ljs.scratch.util.Jackson;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lstephen
 */
public class History {

    private static final Logger LOG =
        Logger.getLogger(History.class.getName());

    public Iterable<TeamStats<BattingStats>> loadBatting(Site site, int season, int years) {

        List<TeamStats<BattingStats>> result = Lists.newArrayList();

        for (int y = season - years; y < season; y++) {
            LOG.log(Level.INFO, "Loading batting for season {0}...", y);

            File in = getBattingFile(site, y);

            if (in.exists()) {
                try {
                    result.add(Jackson.getMapper(site).readValue(in, TeamStats.class));
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        }

        return result;
    }

    public Iterable<TeamStats<PitchingStats>> loadPitching(Site site, int season, int years) {
        List<TeamStats<PitchingStats>> result = Lists.newArrayList();

        for (int y = season - years; y < season; y++) {
            LOG.log(Level.INFO, "Loading pitching for season {0}...", y);

            File in = getPitchingFile(site, y);

            if (in.exists()) {
                try {
                    result.add(Jackson.getMapper(site).readValue(in, TeamStats.class));
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        }

        return result;
    }

    public void saveBatting(TeamStats<BattingStats> stats, Site site, int season) {
        save(getBattingFile(site, season), stats, site, season);
    }

    public void savePitching(TeamStats<PitchingStats> stats, Site site, int season) {
        save(getPitchingFile(site, season), stats, site, season);

    }

    private void save(File f, TeamStats<?> stats, Site site, int season) {
        try {
            Files.createParentDirs(f);

            Jackson.getMapper(site).writeValue(f, stats);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    

    private File getBattingFile(Site site, int season) {
        return new File("c:/ootp/history", site.getName() + season + ".batting.json");
    }

    private File getPitchingFile(Site site, int season) {
        return new File("c:/ootp/history", site.getName() + season + ".pitching.json");
    }

}
