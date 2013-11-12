package com.ljs.scratch.ootp.elo;

import com.google.common.collect.Maps;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.roster.Team;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public final class EloRatings {

    private final Map<Id<Team>, Long> ratings = Maps.newHashMap();

    private Double kFactor = 4.0;

    private EloRatings() { }

    public void setKFactor(Double kFactor) {
        this.kFactor = kFactor;
    }

    public Long get(Id<Team> team) {
        if (!ratings.containsKey(team)) {
            ratings.put(team, 1500L);
        }
        return ratings.get(team);
    }

    public void update(GameResult result) {
        Long visitorRating = get(result.getVisitor());
        Long homeRating = get(result.getHome());

        ratings.put(result.getVisitor(), newRating(visitorRating, homeRating, result.getVisitorScore(), result.getHomeScore(), false));
        ratings.put(result.getHome(), newRating(homeRating, visitorRating, result.getHomeScore(), result.getVisitorScore(), true));
    }

    private Long newRating(Long ratingFor, Long ratingAgainst, Integer scoreFor, Integer scoreAgainst, boolean home) {
        Integer w = scoreFor > scoreAgainst ? 1 : 0;

        Double k = kFactor * Math.pow(Math.abs(scoreFor - scoreAgainst), 0.33);

        Double dr = (double) (ratingFor - ratingAgainst) + (home ? 25 : -25);

        Double we = 1 / (Math.pow(10, (-dr / 400)) + 1);

        return (long) (ratingFor + k * (w - we));
    }


    public void setRating(Id<Team> team, Long rating) {
        ratings.put(team, rating);
    }

    public static EloRatings create() {
        return new EloRatings();
    }

}
