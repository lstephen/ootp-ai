package com.github.lstephen.ootp.ai.player.ratings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

/** @author lstephen */
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

  public static final class Builder {

    private Optional<Integer> range = Optional.absent();

    private Optional<Integer> errors = Optional.absent();

    private Optional<Integer> arm = Optional.absent();

    private Optional<Integer> dp = Optional.absent();

    private Optional<Integer> ability = Optional.absent();

    private Builder() {}

    public Builder range(Integer range) {
      this.range = Optional.of(range);
      return this;
    }

    public Builder errors(Integer errors) {
      this.errors = Optional.of(errors);
      return this;
    }

    public Builder arm(Integer arm) {
      this.arm = Optional.of(arm);
      return this;
    }

    public Builder dp(Integer dp) {
      this.dp = Optional.of(dp);
      return this;
    }

    public Builder ability(Integer ability) {
      this.ability = Optional.of(ability);
      return this;
    }

    public FieldingRatings build() {
      return FieldingRatings.build(this);
    }

    private static Builder create() {
      return new Builder();
    }

    @JsonCreator
    public static Builder fromJson(ObjectNode node) {
      return create()
          .range(fromJson(node, "range"))
          .errors(fromJson(node, "errors"))
          .arm(fromJson(node, "arm"))
          .dp(fromJson(node, "dp"))
          .ability(fromJson(node, "ability"));
    }

    private static int fromJson(ObjectNode node, String field) {
      JsonNode present = node.get(field).get("present");

      if (present == null) {
        if (node.get(field) == null) {
          return 0;
        } else {
          return node.get(field).asInt();
        }
      }

      if (present.asBoolean()) {
        return node.get(field).get("reference").asInt();
      }

      return 0;
    }
  }

  public static class Weighting {

    private Double range = 0.0;

    private Double errors = 0.0;

    private Double arm = 0.0;

    private Double dp = 0.0;

    private Double ability = 0.0;

    private Weighting() {}

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
