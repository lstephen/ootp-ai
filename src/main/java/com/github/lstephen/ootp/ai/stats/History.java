package com.github.lstephen.ootp.ai.stats;

import com.github.lstephen.ootp.ai.config.Config;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.scratch.util.Jackson;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author lstephen */
public final class History {

  private static final Logger LOG = Logger.getLogger(History.class.getName());

  private final Config config;

  private History(Config config) {
    this.config = config;
  }

  public Iterable<TeamStats<BattingStats>> loadBatting(Site site, int season, int years) {

    List<TeamStats<BattingStats>> result = Lists.newArrayList();

    for (int y = season - years; y < season; y++) {
      TeamStats<BattingStats> s = loadBatting(site, y);

      if (s != null) {
        result.add(s);
      }
    }

    return result;
  }

  public TeamStats<BattingStats> loadBatting(Site site, int season) {
    int y = season;

    if (season < 0) {
      y = site.getDate().getYear() + season;
    }

    LOG.log(Level.INFO, "Loading batting for season {0}...", y);

    File in = getBattingFile(site, y);

    if (in.exists()) {
      try {
        return Jackson.getMapper(site).readValue(in, TeamStats.Batting.class);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    } else {
      return null;
    }
  }

  public Iterable<TeamStats<PitchingStats>> loadPitching(Site site, int season, int years) {
    List<TeamStats<PitchingStats>> result = Lists.newArrayList();

    for (int y = season - years; y < season; y++) {
      TeamStats<PitchingStats> s = loadPitching(site, y);

      if (s != null) {
        result.add(s);
      }
    }

    return result;
  }

  public TeamStats<PitchingStats> loadPitching(Site site, int season) {
    int y = season;

    if (season < 0) {
      y = site.getDate().getYear() + season;
    }

    LOG.log(Level.INFO, "Loading pitching for season {0}...", y);

    File in = getPitchingFile(site, y);

    if (in.exists()) {
      try {
        return Jackson.getMapper(site).readValue(in, TeamStats.class);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    } else {
      return null;
    }
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
    return new File(getHistoryDir(), site.getName() + season + ".batting.json");
  }

  private File getPitchingFile(Site site, int season) {
    return new File(getHistoryDir(), site.getName() + season + ".pitching.json");
  }

  private String getHistoryDir() {
    return config.getValue("history.dir").or("c:/ootp/history");
  }

  public static History create() {
    try {
      return new History(Config.createDefault());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public static History create(Config config) {
    return new History(config);
  }
}
