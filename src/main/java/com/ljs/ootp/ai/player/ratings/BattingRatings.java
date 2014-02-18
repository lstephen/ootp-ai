package com.ljs.ootp.ai.player.ratings;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.extract.html.rating.Rating;
import com.ljs.ootp.extract.html.rating.Scale;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public final class BattingRatings<T> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    private final Scale<T> scale;

    private final Rating<T, ? extends Scale<T>> contact;

    private final Rating<T, ?> gap;

    private final Rating<T, ?> power;

    private final Rating<T, ?> eye;

    private final Rating<T, ?> k;

    private BattingRatings(Builder<T> builder) {
        Assertions.assertThat(builder.scale).isNotNull();
        Preconditions.checkNotNull(builder.contact);
        Preconditions.checkNotNull(builder.gap);
        Preconditions.checkNotNull(builder.power);
        Preconditions.checkNotNull(builder.eye);

        this.scale = builder.scale;
        this.contact = builder.contact;
        this.gap = builder.gap;
        this.power = builder.power;
        this.eye = builder.eye;
        this.k = builder.k;
    }

    public Integer getContact() {
        return contact.normalize().get();
    }

    public Integer getGap() {
        return gap.normalize().get();
    }

    public Integer getPower() {
        return power.normalize().get();
    }

    public Integer getEye() {
        return eye.normalize().get();
    }

    public Optional<Integer> getK() {
        return k == null
            ? Optional.<Integer>absent()
            : Optional.of(k.normalize().get());
    }

    public BattingRatings<T> build() {
        return this;
    }

    @Override
    public String toString() {
        return Objects
            .toStringHelper(this)
            .add("scale", scale)
            .add("contact", contact)
            .add("gap", gap)
            .add("power", power)
            .add("eye", eye)
            .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        BattingRatings<?> rhs = BattingRatings.class.cast(obj);

        return new EqualsBuilder()
            .append(contact, rhs.contact)
            .append(gap, rhs.gap)
            .append(power, rhs.power)
            .append(eye, rhs.eye)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(contact)
            .append(gap)
            .append(power)
            .append(eye)
            .toHashCode();
    }

    public static <T> Builder<T> builder(Scale<T> scale) {
        return Builder.create(scale);
    }

    @JsonCreator
    private static <T> BattingRatings<T> build(Builder<T> builder) {
        return new BattingRatings<T>(builder);
    }

    public static final class Builder<T> {

        private final Scale<T> scale;

        private Rating<T, ? extends Scale<T>> contact;

        private Rating<T, ? extends Scale<T>> gap;

        private Rating<T, ? extends Scale<T>> power;

        private Rating<T, ? extends Scale<T>> eye;

        private Rating<T, ? extends Scale<T>> k;

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

        public Builder<T> contact(Rating<T, ? extends Scale<T>> contact) {
            this.contact = contact;
            return this;
        }

        @JsonProperty("contact")
        public Builder<T> contact(String s) {
            return contact(scale.parse(s));
        }

        public Builder<T> contact(T value) {
            return contact(Rating.create(value, scale));
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
            return gap(Rating.create(value, scale));
        }

        public Builder<T> power(Rating<T, ?> power) {
            this.power = power;
            return this;
        }

        @JsonProperty("power")
        public Builder<T> power(String s) {
            return power(scale.parse(s));
        }

        public Builder<T> power(T value) {
            return power(Rating.create(value, scale));
        }

        public Builder<T> eye(Rating<T, ?> eye) {
            this.eye = eye;
            return this;
        }

        @JsonProperty("eye")
        public Builder<T> eye(String s) {
            return eye(scale.parse(s));
        }

        public Builder<T> eye(T value) {
            return eye(Rating.create(value, scale));
        }

        public Builder<T> k(Rating<T, ?> k) {
            this.k = k;
            return this;
        }

        @JsonProperty("k")
        public Builder<T> k(String s) {
            return s == null || s.equals("null")
                ? this
                : k(scale.parse(s));
        }

        public Builder<T> k(T value) {
            return value == null ? this : k(Rating.create(value, scale));
        }


        public BattingRatings<T> build() {
            return BattingRatings.build(this);
        }

        private static <T> Builder<T> create(Scale<T> scale) {
            return new Builder<T>(scale);
        }

    }

}
