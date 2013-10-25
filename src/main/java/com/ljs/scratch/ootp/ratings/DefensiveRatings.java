package com.ljs.scratch.ootp.ratings;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public class DefensiveRatings {

    private static final ImmutableMap<Position, Integer> MINIMUMS =
        ImmutableMap.<Position, Integer>builder()
            .put(Position.SHORTSTOP, 5) // C
            .put(Position.SECOND_BASE, 4) // C
            .put(Position.THIRD_BASE, 4) // C
            .put(Position.CENTER_FIELD, 3)  // D
            .put(Position.LEFT_FIELD, 2) // D
            .put(Position.RIGHT_FIELD, 2) //D
            .build();

    private final Map<Position, Double> positionRating = Maps.newHashMap();

    private Integer infieldArm;

    private Integer outfieldArm = 0;

    public void setPositionRating(Position p, Double rating) {
        positionRating.put(p, rating);
    }

    public Integer getInfieldArm() {
        return infieldArm == null ? 0 : infieldArm;
    }

    public void setInfieldArm(Integer infieldArm) {
        this.infieldArm = infieldArm;
    }

    public Integer getOutfieldArm() {
        return outfieldArm;
    }

    public void setOutfieldArm(Integer outfieldArm) {
        this.outfieldArm = outfieldArm;
    }


    public Double getPositionScore(Position p) {
        Double rating = positionRating.containsKey(p) ? positionRating.get(p) : 0;

        return MINIMUMS.containsKey(p) && rating < MINIMUMS.get(p) ? 0.0 : rating;
    }

    @Deprecated
    public DefensiveRatings applyMinimums() {
        return this;
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
        return getFormattedValue(value.intValue());
    }

    private Character getFormattedValue(Integer value) {
        if (value == null || value <= 0) { return '-'; }
        if (value >= 10) { return 'T'; }

        return value.toString().charAt(0);
    }

    public String getPrimaryPosition() {
        DefensiveRatings rs = applyMinimums();

        Position specialty = byRawScore(rs).max(Position.CATCHER, Position.SHORTSTOP, Position.CENTER_FIELD);

        if (rs.getPositionScore(specialty) > 0) {
            return specialty.getAbbreviation();
        }

        Position tierTwo = byRawScore(rs).max(Position.SECOND_BASE, Position.THIRD_BASE);

        if (rs.getPositionScore(tierTwo) > 0) {
            return tierTwo.getAbbreviation();
        }

        Position tierThree = byRawScore(rs).max(Position.RIGHT_FIELD, Position.LEFT_FIELD);

        if (rs.getPositionScore(tierThree) > 0) {
            return tierThree.getAbbreviation();
        }

        return rs.getPositionScore(Position.FIRST_BASE) > 0
            ? Position.FIRST_BASE.getAbbreviation()
            : "DH";

    }

    private Ordering<Position> byRawScore(final DefensiveRatings rs) {
        return Ordering
            .natural()
            .onResultOf(new Function<Position, Double>() {
                @Override
                public Double apply(Position p) {
                    Double score = rs.positionRating.containsKey(p)
                        ? rs.positionRating.get(p)
                        : 0;

                    return MINIMUMS.containsKey(p) && score < MINIMUMS.get(p)
                        ? 0
                        : score;
                }
            });
    }

}
