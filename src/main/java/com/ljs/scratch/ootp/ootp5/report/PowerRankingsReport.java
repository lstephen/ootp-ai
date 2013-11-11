package com.ljs.scratch.ootp.ootp5.report;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.io.Printable;
import com.ljs.scratch.ootp.ootp5.site.BoxScores;
import com.ljs.scratch.ootp.ootp5.site.BoxScores.Result;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.Standings;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public class PowerRankingsReport implements Printable {

    private final Site site;

    private Map<Id<Team>, Long> ratings = Maps.newHashMap();

    private Multiset<Id<Team>> wins = HashMultiset.create();

    private Multiset<Id<Team>> losses = HashMultiset.create();

    private Integer numberOfResults;

    private PowerRankingsReport(Site site) {
        this.site = site;
    }

    @Override
    public void print(PrintWriter w) {
        w.println();

        populateInitialRatings();

        BoxScores scores = BoxScores.create(site);

        numberOfResults = Iterables.size(scores.getResults());

        for (Result result : scores.getResults()) {
            updateRating(result);
        }

        Iterable<Id<Team>> ids = Ordering
            .natural()
            .reverse()
            .onResultOf(
                new Function<Id<Team>, Long>() {
                    public Long apply(Id<Team> id) {
                        return ratings.get(id);
                    }
                })
            .sortedCopy(ratings.keySet());

        w.println("** Power Rankings **");

        for (Id<Team> id : ids) {
            int ws = site.getStandings().getWins(id);
            int ls = site.getStandings().getLosses(id);

            double pct = ws + ls > 0 ? (double) ws / (ws + ls) : .500;

            w.println(
                String.format(
                    "%-20s | %3d-%3d %.3f | %2d-%2d | %4d ",
                    StringUtils.abbreviate(site.getSingleTeam(id).getName(), 20),
                    ws,
                    ls,
                    pct,
                    wins.count(id),
                    losses.count(id),
                    ratings.get(id)
                ));

        }
    }

    private Id<Team> getTeamId(String teamName) {
        for (Id<Team> id : site.getTeamIds()) {
            if (site.getSingleTeam(id).getName().startsWith(teamName)) {
                return id;
            }
        }

        throw new IllegalStateException(teamName);
    }

    private void updateRating(Result result) {
        Id<Team> visitor = getTeamId(result.getVisitorName());
        Id<Team> home = getTeamId(result.getHomeName());

        Long visitorRating = ratings.get(visitor);
        Long homeRating = ratings.get(home);

        if (result.getVisitorScore() > result.getHomeScore()) {
            wins.add(visitor);
            losses.add(home);
        } else {
            wins.add(home);
            losses.add(visitor);
        }

        ratings.put(visitor, newRating(visitorRating, homeRating, result.getVisitorScore(), result.getHomeScore(), false));
        ratings.put(home, newRating(homeRating, visitorRating, result.getHomeScore(), result.getVisitorScore(), true));
    }

    private Long newRating(Long ratingFor, Long ratingAgainst, Integer scoreFor, Integer scoreAgainst, boolean home) {
        Integer w = scoreFor > scoreAgainst ? 1 : 0;

        Double kFactor = ((double) 162 / (numberOfResults / Iterables.size(site.getTeamIds())));

        Double k = kFactor * Math.pow(Math.abs(scoreFor - scoreAgainst), 0.33);

        Double dr = (double) (ratingFor - ratingAgainst) + (home ? 25 : -25);

        Double we = 1 / (Math.pow(10, (-dr / 400)) + 1);

        //System.out.println("ratings:" + ratingFor + "/" + ratingAgainst);
        //System.out.println("dr:" + dr + " hm:" + home);
        //System.out.println("we:" + we);
        //System.out.println("new:" + (ratingFor + k * (w - we)));

        return (long) (ratingFor + k * (w - we));
    }

    private void populateInitialRatings() {
        Standings standings = site.getStandings();

        for (Id<Team> team : site.getTeamIds()) {
            Integer w = standings.getWins(team);
            Integer l = standings.getLosses(team);

            Double we = w + l == 0 ? 0.5 : (double) w / (w + l);

            Long elo = 1500 + Math.round(
                (400 * Math.log((double) -we / (we-1)))
                / Math.log(10));

            ratings.put(team, elo);
        }
    }

    public static PowerRankingsReport create(Site site) {
        return new PowerRankingsReport(site);
    }

}
