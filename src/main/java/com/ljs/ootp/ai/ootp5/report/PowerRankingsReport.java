package com.ljs.ootp.ai.ootp5.report;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.elo.EloRatings;
import com.ljs.ootp.ai.elo.GameResult;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.ootp5.site.BoxScores;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Record;
import com.ljs.ootp.ai.site.RecordPredictor;
import com.ljs.ootp.ai.site.Site;
import java.io.PrintWriter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public final class PowerRankingsReport implements Printable {

  private final Site site;

  private final RecordPredictor recordPredictor;

  private final EloRatings ratings = EloRatings.create();

  private final Multiset<Id<Team>> wins = HashMultiset.create();

  private final Multiset<Id<Team>> losses = HashMultiset.create();

  private PowerRankingsReport(Site site, RecordPredictor recordPredictor) {
    this.site = site;
    this.recordPredictor = recordPredictor;
  }

  @Override
  public void print(PrintWriter w) {
    w.println();

    populateInitialRatings();

    BoxScores scores = BoxScores.create(site);


    Integer numberOfResults = Iterables.size(scores.getResults());

    Double resultsPerTeam = (double) numberOfResults / Iterables.size(site.getTeamIds());

    ratings.setKFactor((162.0 / resultsPerTeam) / 2);

    scores.getResults().forEach(this::updateRating);

    Iterable<Id<Team>> ids = Ordering
      .natural()
      .reverse()
      .onResultOf(ratings::get)
      .sortedCopy(site.getTeamIds());

    w.println("** Power Rankings **");

    ids.forEach(id -> {
      Record current = site.getStandings().getRecord(id);

      w.println(
        String.format(
          "%-20s | %3d-%3d %.3f | %2d-%2d | %4d ",
          StringUtils.abbreviate(site.getSingleTeam(id).getName(), 20),
          current.getWins(),
          current.getLosses(),
          current.getWinPercentage(),
          wins.count(id),
          losses.count(id),
          ratings.get(id)
          ));

    });
  }

  private void updateRating(GameResult result) {
    Id<Team> visitor = result.getVisitor();
    Id<Team> home = result.getHome();

    if (site.getStandings().getRecord(visitor).getGames() == 0 && site.getStandings().getRecord(home).getGames() == 0) {
      return;
    }

    if (result.getVisitorScore() > result.getHomeScore()) {
      wins.add(visitor);
      losses.add(home);
    } else {
      wins.add(home);
      losses.add(visitor);
    }

    ratings.update(result);
  }

  private void populateInitialRatings() {
    site.getTeamIds().forEach(team -> {
      Double we = recordPredictor.getExpectedEndOfSeason(team).getWinPercentage();

      Long elo = 1500 + Math.round(
        (400 * Math.log(-we / (we-1)))
        / Math.log(10));

      ratings.setRating(team, elo);
    });
  }

  public static PowerRankingsReport create(Site site, RecordPredictor recordPredictor) {
    return new PowerRankingsReport(site, recordPredictor);
  }

}
