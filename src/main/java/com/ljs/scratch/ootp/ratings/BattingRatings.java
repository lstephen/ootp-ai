package com.ljs.scratch.ootp.ratings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 *
 * @author lstephen
 */
public final class BattingRatings {

    private final Integer contact;

    private final Integer gap;

    private final Integer power;

    private final Integer eye;

    private BattingRatings(Builder builder) {
        Preconditions.checkNotNull(builder.contact);
        Preconditions.checkNotNull(builder.gap);
        Preconditions.checkNotNull(builder.power);
        Preconditions.checkNotNull(builder.eye);

        this.contact = builder.contact;
        this.gap = builder.gap;
        this.power = builder.power;
        this.eye = builder.eye;
    }

    public Integer getContact() {
        return contact;
    }

    public Integer getGap() {
        return gap;
    }

    public Integer getPower() {
        return power;
    }

    public Integer getEye() {
        return eye;
    }

    public BattingRatings build() {
        return this;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
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

        BattingRatings rhs = BattingRatings.class.cast(obj);

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

    public static Builder builder() {
        return Builder.create();
    }

    @JsonCreator
    private static BattingRatings build(Builder builder) {
        return new BattingRatings(builder);
    }

    public static final class Builder {

        private Integer contact;

        private Integer gap;

        private Integer power;

        private Integer eye;

        private Builder() { }

        public Builder contact(Integer contact) {
            this.contact = contact;
            return this;
        }

        public Builder gap(Integer gap) {
            this.gap = gap;
            return this;
        }

        public Builder power(Integer power) {
            this.power = power;
            return this;
        }

        public Builder eye(Integer eye) {
            this.eye = eye;
            return this;
        }

        public BattingRatings build() {
            return BattingRatings.build(this);
        }

        private static Builder create() {
            return new Builder();
        }

    }

}
