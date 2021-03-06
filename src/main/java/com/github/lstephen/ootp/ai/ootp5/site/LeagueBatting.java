package com.github.lstephen.ootp.ai.ootp5.site;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.lstephen.ootp.ai.config.Config;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.extract.html.Page;
import com.github.lstephen.scratch.util.Jackson;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/** @author lstephen */
public class LeagueBatting {

  private static final int HOMERUN_IDX = 6;
  private static final int RUNS_IDX = 7;
  private static final int ATBAT_IDX = 8;
  private static final int HITS_IDX = 9;
  private static final int DOUBLES_IDX = 10;
  private static final int TRIPLES_IDX = 11;
  private static final int WALKS_IDX = 12;

  @JsonIgnore private Site site;

  @JsonIgnore private Page page;

  @JsonIgnore private String league;

  private Map<Integer, BattingStats> historical = Maps.newHashMap();

  private LeagueBatting() {}

  public LeagueBatting(Site site, String league) {
    this.page = site.getPage("leagueb.html");
    this.league = league;
    this.site = site;

    loadHistorical();
  }

  private File getHistoricalFile() {
    try {
      String historyDirectory =
          Config.createDefault().getValue("history.dir").or("c:/ootp/history/");
      File historicalFile =
          new File(historyDirectory + "/" + site.getName() + "league.batting.json");
      Files.createParentDirs(historicalFile);
      return historicalFile;
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private void loadHistorical() {

    File f = getHistoricalFile();

    if (f.exists()) {
      try {
        historical = Jackson.getMapper(site).readValue(f, LeagueBatting.class).historical;
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  public LocalDate extractDate() {
    Document doc = page.load();

    return LocalDate.parse(
        StringUtils.substringAfterLast(doc.select("td.title").text(), ",").trim(),
        DateTimeFormat.forPattern("MM/dd/YYYY"));
  }

  public Optional<BattingStats> extractYearsAgo(int yearsAgo) {
    int seasonToLoad = extractDate().getYear() - yearsAgo;

    if (historical.containsKey(seasonToLoad) && historical.get(seasonToLoad).getRuns() != null) {
      return Optional.of(historical.get(seasonToLoad));
    }

    return Optional.empty();
  }

  public BattingStats extractTotal() {
    Elements total = extractTotalLine();

    BattingStats batting = new BattingStats();

    batting.setRuns(getInteger(total, RUNS_IDX));
    batting.setHomeRuns(getInteger(total, HOMERUN_IDX));
    batting.setAtBats(getInteger(total, ATBAT_IDX));
    batting.setHits(getInteger(total, HITS_IDX));
    batting.setDoubles(getInteger(total, DOUBLES_IDX));
    batting.setTriples(getInteger(total, TRIPLES_IDX));
    batting.setWalks(getInteger(total, WALKS_IDX));

    int currentSeason = extractDate().getYear();

    historical.put(currentSeason, batting);

    batting =
        IntStream.range(1, 6)
            .mapToObj(this::extractYearsAgo)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(batting, BattingStats::add);

    try {
      Jackson.getMapper(site).writeValue(getHistoricalFile(), this);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    return batting;
  }

  private Integer getInteger(Elements line, int idx) {
    return Integer.valueOf(line.get(idx).text());
  }

  private Elements extractTotalLine() {
    Document doc = page.load();

    Elements leagueStats = doc.select("tr:has(td:contains(" + league + ")) ~ tr");

    return leagueStats.select(
        "tr.g:has(td > b:contains(Total)) td, " + "tr.g2:has(td > b:contains(Total)) td");
  }
}
