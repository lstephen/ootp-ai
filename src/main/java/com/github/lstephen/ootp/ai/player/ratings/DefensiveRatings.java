package com.github.lstephen.ootp.ai.player.ratings;

import com.github.lstephen.ootp.ai.selection.lineup.Defense;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author lstephen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefensiveRatings {

    private static final ImmutableMap<Position, Double> MINIMUMS =
        ImmutableMap.<Position, Double>builder()
            .put(Position.SHORTSTOP, 5.0) // C
            .put(Position.SECOND_BASE, 4.0) // C
            .put(Position.THIRD_BASE, 4.0) // C
            .put(Position.CENTER_FIELD, 3.0)  // D
            .put(Position.LEFT_FIELD, 2.0) // D
            .put(Position.RIGHT_FIELD, 2.0) //D
            .put(Position.CATCHER, 1.0)
            .put(Position.FIRST_BASE, 1.0)
            .build();

    private static final ImmutableMap<Position, FieldingRatings.Weighting> WEIGHTINGS =
        ImmutableMap.<Position, FieldingRatings.Weighting>builder()
        .put(Position.CATCHER, FieldingRatings.weighting().arm(1.25).ability(.75))
        .put(Position.FIRST_BASE, FieldingRatings.weighting().range(1.0).errors(1.25).arm(.75).dp(1.0))
        .put(Position.SECOND_BASE, FieldingRatings.weighting().range(1.25).errors(1.25).arm(.25).dp(1.25))
        .put(Position.THIRD_BASE, FieldingRatings.weighting().range(1.25).errors(1.0).arm(1.5).dp(.25))
        .put(Position.SHORTSTOP, FieldingRatings.weighting().range(1.0).errors(1.0).arm(1.0).dp(1.0))
        .put(Position.LEFT_FIELD, FieldingRatings.weighting().range(1.0).errors(1.0).arm(1.0))
        .put(Position.CENTER_FIELD, FieldingRatings.weighting().range(1.25).errors(1.0).arm(.75))
        .put(Position.RIGHT_FIELD, FieldingRatings.weighting().range(.75).errors(1.0).arm(1.25))
        .build();


    private final Map<Position, Double> positionRating = Maps.newHashMap();

    private FieldingRatings catcher;

    private FieldingRatings infield;

    private FieldingRatings outfield;

    public Double getPositionRating(Position p) {
        return positionRating.containsKey(p) ? positionRating.get(p) : 0.0;
    }

    public void setPositionRating(Position p, Double rating) {
        positionRating.put(p, rating);
    }

    public void setCatcher(FieldingRatings catcher) {
        this.catcher = catcher;
    }

    public void setInfield(FieldingRatings infield) {
        this.infield = infield;

    }

    public void setOutfield(FieldingRatings outfield) {
        this.outfield = outfield;
    }

    public Double getPositionScore(Position p) {
        Double rating = positionRating.containsKey(p) ? positionRating.get(p) : 0.0;

        Double skill = null;

        switch (p) {
            case CATCHER:
                if (catcher != null) {
                    skill = catcher.score(WEIGHTINGS.get(p));
                }
                break;
            case SECOND_BASE:
            case THIRD_BASE:
            case SHORTSTOP:
                if (infield != null) {
                    skill = infield.score(WEIGHTINGS.get(p));
                }
                break;
            case LEFT_FIELD:
            case CENTER_FIELD:
            case RIGHT_FIELD:
                if (outfield != null) {
                    skill = outfield.score(WEIGHTINGS.get(p));
                }
                break;
            case FIRST_BASE:
                if (infield != null && outfield != null) {
                    skill = Math.max(
                        infield.score(WEIGHTINGS.get(p)),
                        outfield.score(WEIGHTINGS.get(p)));
                }
                break;
            default:
        }

        Double score = skill == null
            ? rating
            : ((skill + 1.25 * rating) / 2.25);

        if (MINIMUMS.containsKey(p)) {
          Double min = MINIMUMS.get(p);

          Double f = Defense.getPositionFactor(p).doubleValue();
          Double m = (f + min) / min;

          return m * score - f;
        } else {
          return score;
        }
    }

    public String getPositionScores() {
        StringBuilder str = new StringBuilder();

        for (Position p : ImmutableSet.of(
            Position.CATCHER,
            Position.FIRST_BASE,
            Position.SECOND_BASE,
            Position.THIRD_BASE,
            Position.SHORTSTOP,
            Position.LEFT_FIELD,
            Position.CENTER_FIELD,
            Position.RIGHT_FIELD)) {

            str.append(getFormattedPositionScore(p));
        }

        return str.toString();
    }

    public Character getFormattedPositionScore(Position p) {
        return getFormattedValue(getPositionScore(p));
    }

    private Character getFormattedValue(Double value) {
        return getFormattedValue(Math.round(value));
    }

    private Character getFormattedValue(Long value) {
        if (value == null || value <= 0) { return '-'; }
        if (value >= 10) { return 'T'; }

        return value.toString().charAt(0);
    }

    public String getPrimaryPosition() {
      return Position.hitting()
        .stream()
        .filter(positionRating::containsKey)
        .max(Ordering.natural().onResultOf(p -> Defense.score(this, p)))
        .map(Position::getAbbreviation)
        .orElse("DH");
    }

}
