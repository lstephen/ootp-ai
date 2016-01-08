package com.github.lstephen.ootp.ai.player.ratings;

import com.github.lstephen.ootp.ai.rating.OneToTen;
import com.github.lstephen.ootp.ai.rating.Rating;
import com.github.lstephen.ootp.ai.rating.Scale;
import com.github.lstephen.ootp.ai.site.Site;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


/**
 *
 * @author lstephen
 */
public class PitchingRatings<T> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    private final Scale<T> scale;

    private final Rating<T, ?> hits;

    private final Rating<T, ?> gap;

    private final Rating<T, ?> stuff;

    private final Rating<T, ?> control;

    private final Rating<T, ?> movement;

    private final Rating<Integer, ? super OneToTen> endurance;

    private PitchingRatings(Builder<T> builder) {
        Preconditions.checkNotNull(builder.scale);
        Preconditions.checkNotNull(builder.hits);
        Preconditions.checkNotNull(builder.gap);
        Preconditions.checkNotNull(builder.stuff);
        Preconditions.checkNotNull(builder.control);
        Preconditions.checkNotNull(builder.movement);
        Preconditions.checkNotNull(builder.endurance);

        this.scale = builder.scale;
        this.hits = builder.hits;
        this.gap = builder.gap;
        this.stuff = builder.stuff;
        this.control = builder.control;
        this.movement = builder.movement;
        this.endurance = builder.endurance;
    }

    public Integer getHits() { return hits.normalize().get(); }

    public Integer getGap() { return gap.normalize().get(); }

    public Integer getStuff() { return stuff.normalize().get(); }

    public Integer getControl() { return control.normalize().get(); }

    public Integer getMovement() { return movement.normalize().get(); }

    public Integer getEndurance() { return endurance.get(); }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("stuff", stuff)
            .add("control", control)
            .add("movement", movement)
            .add("hits", hits)
            .add("gap", gap)
            .toString();
    }

    public static <T> Builder<T> builder(Scale<T> scale) {
        return Builder.create(scale);
    }

    @JsonCreator
    private static <T> PitchingRatings<T> build(Builder<T> builder) {
        return new PitchingRatings<T>(builder);
    }

    public static final class Builder<T> {

        private final Scale<T> scale;

        private Rating<T, ? extends Scale<T>> hits;

        private Rating<T, ? extends Scale<T>> gap;

        private Rating<T, ? extends Scale<T>> stuff;

        private Rating<T, ? extends Scale<T>> control;

        private Rating<T, ? extends Scale<T>> movement;

        private Rating<Integer, ? super OneToTen> endurance;

        private Builder(Scale<T> scale) {
            this.scale = scale;
        }

        @JsonCreator
        private Builder(
            @JsonProperty("scale") Scale<T> scale, @JacksonInject Site site) {

            this.scale = scale == null
                ? (Scale<T>) site.getAbilityRatingScale()
                : scale;
        }

        public Builder<T> hits(Rating<T, ? extends Scale<T>> hits) {
            this.hits = hits;
            return this;
        }

        @JsonProperty("hits")
        public Builder<T> hits(String s) {
            return hits(scale.parse(s));
        }

        public Builder<T> hits(T value) {
            return hits(new Rating<>(value, scale));
        }

        public Builder<T> gap(Rating<T, ?> gap) {
            this.gap = gap;
            return this;
        }

        @JsonProperty("gap")
        public Builder<T> gap(String s) {
            return gap(scale.parse(s));
        }

        public Builder<T> gap(T value) {
            return gap(new Rating<>(value, scale));
        }

        public Builder<T> stuff(Rating<T, ?> stuff) {
            this.stuff = stuff;
            return this;
        }

        @JsonProperty("stuff")
        public Builder<T> stuff(String s) {
            return stuff(scale.parse(s));
        }

        public Builder<T> stuff(T value) {
            return stuff(new Rating<>(value, scale));
        }

        public Builder<T> control(Rating<T, ?> control) {
            this.control = control;
            return this;
        }

        @JsonProperty("control")
        public Builder<T> control(String s) {
            return control(scale.parse(s));
        }

        public Builder<T> control(T value) {
            return control(new Rating<>(value, scale));
        }

        public Builder<T> movement(Rating<T, ?> movement) {
            this.movement = movement;
            return this;
        }

        @JsonProperty("movement")
        public Builder<T> movement(String s) {
            return movement(scale.parse(s));
        }

        public Builder<T> movement(T value) {
            return movement(new Rating<>(value, scale));
        }

        public Builder<T> endurance(Rating<Integer, ? super OneToTen> endurance) {
            this.endurance = endurance;
            return this;
        }

        @JsonProperty("endurance")
        public Builder<T> endurance(String s) {
            return endurance(new OneToTen().parse(s));
        }

        public Builder<T> endurance(Integer value) {
            return endurance(new Rating<>(value, new OneToTen()));
        }


        public PitchingRatings<T> build() {
            return PitchingRatings.build(this);
        }

        private static <T> Builder<T> create(Scale<T> scale) {
            return new PitchingRatings.Builder<T>(scale);
        }

    }

}
