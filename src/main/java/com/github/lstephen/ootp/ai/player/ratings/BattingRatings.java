package com.github.lstephen.ootp.ai.player.ratings;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.lstephen.ootp.ai.rating.OneToOneHundred;
import com.github.lstephen.ootp.ai.rating.Rating;
import com.github.lstephen.ootp.ai.rating.Scale;
import com.github.lstephen.ootp.ai.site.Site;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/** @author lstephen */
public final class BattingRatings<T> {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
  private final Scale<T> scale;

  private final Rating<T, ? extends Scale<T>> contact;

  private final Rating<T, ?> gap;

  private final Rating<T, ?> triples;

  private final Rating<T, ?> power;

  private final Rating<T, ?> eye;

  private final Rating<T, ?> k;

  private final Rating<?, ?> runningSpeed;

  private final Rating<?, ?> stealingAbility;

  private BattingRatings(Builder<T> builder) {
    Preconditions.checkNotNull(builder.scale);
    Preconditions.checkNotNull(builder.contact);
    Preconditions.checkNotNull(builder.gap);
    Preconditions.checkNotNull(builder.power);
    Preconditions.checkNotNull(builder.eye);

    this.scale = builder.scale;
    this.contact = builder.contact;
    this.gap = builder.gap;
    this.triples = builder.triples;
    this.power = builder.power;
    this.eye = builder.eye;
    this.k = builder.k;
    this.runningSpeed = builder.runningSpeed;
    this.stealingAbility = builder.stealingAbility;
  }

  public Integer getContact() {
    return contact.normalize().get();
  }

  public Integer getGap() {
    return gap.normalize().get();
  }

  public Optional<Integer> getTriples() {
    return triples == null ? Optional.<Integer>absent() : Optional.of(triples.normalize().get());
  }

  public Integer getPower() {
    return power.normalize().get();
  }

  public Integer getEye() {
    return eye.normalize().get();
  }

  public Optional<Integer> getK() {
    return k == null ? Optional.<Integer>absent() : Optional.of(k.normalize().get());
  }

  public Optional<Integer> getRunningSpeed() {
    return runningSpeed == null
        ? Optional.<Integer>absent()
        : Optional.of(runningSpeed.normalize().get());
  }

  public Optional<Integer> getStealingAbility() {
    return stealingAbility == null
        ? Optional.<Integer>absent()
        : Optional.of(stealingAbility.normalize().get());
  }

  public BattingRatings<T> build() {
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
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
    return new HashCodeBuilder().append(contact).append(gap).append(power).append(eye).toHashCode();
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

    private Rating<T, ? extends Scale<T>> triples;

    private Rating<T, ? extends Scale<T>> power;

    private Rating<T, ? extends Scale<T>> eye;

    private Rating<T, ? extends Scale<T>> k;

    private Rating<Integer, ? super OneToOneHundred> runningSpeed;

    private Rating<Integer, ? super OneToOneHundred> stealingAbility;

    private Builder(Scale<T> scale) {
      this.scale = scale;
    }

    @JsonCreator
    private Builder(@JsonProperty("scale") Scale<T> scale, @JacksonInject Site site) {

      this.scale = scale == null ? (Scale<T>) site.getAbilityRatingScale() : scale;
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
      return contact(new Rating<>(value, scale));
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

    public Builder<T> triples(Rating<T, ?> triples) {
      this.triples = triples;
      return this;
    }

    @JsonProperty("triples")
    public Builder<T> triples(String s) {
      return s == null || s.equals("null") ? this : triples(scale.parse(s));
    }

    public Builder<T> triples(T value) {
      return triples(new Rating<>(value, scale));
    }

    public Builder<T> triples(Optional<T> value) {
      return value.transform(this::triples).or(this);
    }

    public Builder<T> triplesOptionalRating(Optional<? extends Rating<T, ?>> value) {
      return value.transform(this::triples).or(this);
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
      return power(new Rating<>(value, scale));
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
      return eye(new Rating<>(value, scale));
    }

    public Builder<T> k(Rating<T, ?> k) {
      this.k = k;
      return this;
    }

    @JsonProperty("k")
    public Builder<T> k(String s) {
      return s == null || s.equals("null") ? this : k(scale.parse(s));
    }

    public Builder<T> k(T value) {
      return value == null ? this : k(new Rating<>(value, scale));
    }

    public Builder<T> runningSpeed(Rating<Integer, ? super OneToOneHundred> rs) {
      this.runningSpeed = rs;
      return this;
    }

    @JsonProperty("runningSpeed")
    public Builder<T> runningSpeed(String s) {
      return s == null || s.equals("null") ? this : runningSpeed(new OneToOneHundred().parse(s));
    }

    public Builder<T> runningSpeed(Integer value) {
      return value == null ? this : runningSpeed(new Rating<>(value, new OneToOneHundred()));
    }

    public Builder<T> runningSpeed(Optional<Integer> value) {
      return value.transform(this::runningSpeed).or(this);
    }


    public Builder<T> stealingAbility(Rating<Integer, ? super OneToOneHundred> sa) {
      this.stealingAbility = sa;
      return this;
    }

    @JsonProperty("stealingAbility")
    public Builder<T> stealingAbility(String s) {
      return s == null || s.equals("null") ? this : stealingAbility(new OneToOneHundred().parse(s));
    }

    public Builder<T> stealingAbility(Integer value) {
      return value == null ? this : stealingAbility(new Rating<>(value, new OneToOneHundred()));
    }

    public BattingRatings<T> build() {
      return BattingRatings.build(this);
    }

    private static <T> Builder<T> create(Scale<T> scale) {
      return new Builder<T>(scale);
    }
  }
}
