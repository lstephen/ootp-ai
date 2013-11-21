package com.ljs.ootp.ai.player.ratings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 *
 * @author lstephen
 */
public class FieldingRatings {

    private final Optional<Integer> range;

    private final Optional<Integer> errors;

    private final Optional<Integer> arm;

    private final Optional<Integer> dp;

    private final Optional<Integer> ability;

    private FieldingRatings(Builder builder) {
        this.range = builder.range;
        this.errors = builder.errors;
        this.arm = builder.arm;
        this.dp = builder.dp;
        this.ability = builder.ability;
    }

    public Double score(Weighting w) {
        Double total =
            w.range * range.or(0)
            + w.errors * errors.or(0)
            + w.arm * arm.or(0)
            + w.dp * dp.or(0)
            + w.ability * ability.or(0);

        return total / w.sum() / 10;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("range", range)
            .add("errors", errors)
            .add("arm", arm)
            .add("dp", dp)
            .add("ability", ability)
            .toString();
    }

    public static Builder builder() {
        return Builder.create();
    }

    @JsonCreator
    private static FieldingRatings build(Builder builder) {
        return new FieldingRatings(builder);
    }

    public static Weighting weighting() {
        return Weighting.create();
    }

    public final static class Builder {

        private Optional<Integer> range = Optional.absent();

        private Optional<Integer> errors = Optional.absent();

        private Optional<Integer> arm = Optional.absent();

        private Optional<Integer> dp = Optional.absent();

        private Optional<Integer> ability = Optional.absent();

        private Builder() { }

        public Builder range(Integer range) {
            this.range = Optional.of(range);
            return this;
        }

        @JsonProperty("range")
        private void range(ObjectNode range) {
            range(range.get("reference").asInt());
        }

        public Builder errors(Integer errors) {
            this.errors = Optional.of(errors);
            return this;
        }

        @JsonProperty("errors")
        private void errors(ObjectNode errors) {
            errors(errors.get("reference").asInt());
        }

        public Builder arm(Integer arm) {
            this.arm = Optional.of(arm);
            return this;
        }

        @JsonProperty("arm")
        private void arm(ObjectNode arm) {
            arm(arm.get("reference").asInt());
        }

        public Builder dp(Integer dp) {
            this.dp = Optional.of(dp);
            return this;
        }

        @JsonProperty("dp")
        private void dp(ObjectNode dp) {
            dp(dp.get("reference").asInt());
        }

        public Builder ability(Integer ability) {
            this.ability = Optional.of(ability);
            return this;
        }

        @JsonProperty("ability")
        private void ability(ObjectNode ability) {
            ability(ability.get("reference").asInt());
        }

        public FieldingRatings build() {
            return FieldingRatings.build(this);
        }

        private static Builder create() {
            return new Builder();
        }

    }

    public static class Weighting {

        private Double range = 0.0;

        private Double errors = 0.0;

        private Double arm = 0.0;

        private Double dp = 0.0;

        private Double ability = 0.0;

        private Weighting() { }

        public Weighting range(Double range) {
            this.range = range;
            return this;
        }

        public Weighting errors(Double errors) {
            this.errors = errors;
            return this;
        }

        public Weighting arm(Double arm) {
            this.arm = arm;
            return this;
        }

        public Weighting dp(Double dp) {
            this.dp = dp;
            return this;
        }

        public Weighting ability(Double ability) {
            this.ability = ability;
            return this;
        }

        public Double sum() {
            return range + errors + arm + dp + ability;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                .add("range", range)
                .add("errors", errors)
                .add("arm", arm)
                .add("dp", dp)
                .add("ability", ability)
                .toString();
        }

        private static Weighting create() {
            return new Weighting();
        }

    }

}
