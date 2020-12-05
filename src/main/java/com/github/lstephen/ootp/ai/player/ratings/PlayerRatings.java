package com.github.lstephen.ootp.ai.player.ratings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.lstephen.ootp.ai.player.ratings.json.BattingPotentialSerializer;
import com.github.lstephen.ootp.ai.player.ratings.json.BuntForHitDeserializer;
import com.github.lstephen.ootp.ai.player.ratings.json.StealingDeserializer;
import com.github.lstephen.ootp.ai.rating.OneToOneHundred;
import com.github.lstephen.ootp.ai.rating.Rating;
import com.github.lstephen.ootp.ai.splits.Splits;
import com.github.lstephen.ootp.ai.stats.SplitPercentages;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** @author lstephen */
public final class PlayerRatings {

  private final Splits<BattingRatings<?>> batting;

  private final DefensiveRatings defensive;

  private final Splits<PitchingRatings<?>> pitching;

  @JsonSerialize(using = BattingPotentialSerializer.class)
  private BattingRatings<?> battingPotential;

  private PitchingRatings<?> pitchingPotential;

  @JsonDeserialize(using = BuntForHitDeserializer.class)
  private Rating<?, ?> buntForHit;

  @JsonDeserialize(using = StealingDeserializer.class)
  private Rating<?, ?> stealing;

  @JsonIgnore private RatingsDefinition definition;

  @JsonIgnore private static SplitPercentages splitPercentages;

  @JsonCreator
  private PlayerRatings(
      @JsonProperty("batting") Splits<BattingRatings<?>> batting,
      @JsonProperty("defensive") DefensiveRatings defensive,
      @JsonProperty("pitching") Splits<PitchingRatings<?>> pitching) {

    this.batting = batting;
    this.defensive = defensive;
    this.pitching = pitching;
  }

  public static void setPercentages(SplitPercentages splitPercentages) {
    PlayerRatings.splitPercentages = splitPercentages;
  }

  public void setDefinition(RatingsDefinition definition) {
    this.definition = definition;
  }

  public DefensiveRatings getDefensive() {
    return defensive;
  }

  public Splits<BattingRatings<?>> getBatting() {
    return batting;
  }

  public Rating<?, ?> getBuntForHit() {
    return buntForHit;
  }

  public void setBuntForHit(Rating<?, ?> buntForHit) {
    this.buntForHit = buntForHit;
  }

  public Rating<?, ?> getStealing() {
    return stealing;
  }

  public void setStealing(Rating<?, ?> stealing) {
    this.stealing = stealing;
  }

  public Splits<PitchingRatings<?>> getPitching() {
    return pitching;
  }

  public boolean hasPitching() {
    return pitching != null;
  }

  public BattingRatings<?> getRawBattingPotential() {
    return battingPotential;
  }

  public PitchingRatings<?> getRawPitchingPotential() {
    return pitchingPotential;
  }

  public Splits<BattingRatings<?>> getBattingPotential(int age) {
    return Splits.create(getRawBattingPotential(), getRawBattingPotential());
  }

  public Splits<PitchingRatings<?>> getPitchingPotential(int age) {
    return Splits.create(getRawPitchingPotential(), getRawPitchingPotential());
  }

  public void setBattingPotential(BattingRatings ratings) {
    this.battingPotential = ratings;
  }

  public void setPitchingPotential(PitchingRatings ratings) {
    this.pitchingPotential = ratings;
  }

  private static final Stream<Function<PitchingRatings, Integer>> pitchingChecks() {
    return Stream.of(
        PitchingRatings::getStuff, PitchingRatings::getControl, PitchingRatings::getMovement);
  }

  private static final Stream<Function<BattingRatings, Integer>> battingChecks() {
    return Stream.of(
        BattingRatings::getContact,
        BattingRatings::getGap,
        BattingRatings::getPower,
        BattingRatings::getEye,
        rat -> (Integer) rat.getK().or(0));
  }

  // TODO: This code duplication between the eligibility checks can probably be
  // refactored.

  private static <R> boolean isTO(R l, R r, R p, Function<R, Integer> get) {
    return get.apply(p) < 90 && (0.75 * get.apply(r) + 0.25 * get.apply(l) >= get.apply(p) + 5);
  }

  @JsonIgnore
  public boolean isTOEligible() {
    if (hasPitching()) {
      PitchingRatings l = getPitching().getVsLeft();
      PitchingRatings r = getPitching().getVsRight();
      PitchingRatings p = getRawPitchingPotential();

      return pitchingChecks().anyMatch(f -> isTO(l, r, p, f));
    } else {
      BattingRatings l = getBatting().getVsLeft();
      BattingRatings r = getBatting().getVsRight();
      BattingRatings p = getRawBattingPotential();

      return battingChecks().anyMatch(f -> isTO(l, r, p, f));
    }
  }

  private static <R> boolean isAfl(R l, R r, Function<R, Integer> get) {
    return 0.75 * get.apply(l) + 0.25 * get.apply(l) <= 65;
  }

  @JsonIgnore
  public boolean isAflEligible() {
    if (hasPitching()) {
      PitchingRatings l = getPitching().getVsLeft();
      PitchingRatings r = getPitching().getVsRight();

      return pitchingChecks().anyMatch(f -> isAfl(l, r, f));
    } else {
      BattingRatings l = getBatting().getVsLeft();
      BattingRatings r = getBatting().getVsRight();

      return battingChecks().anyMatch(f -> isAfl(l, r, f));
    }
  }

  private static <R> boolean isWinter(R l, R r, Function<R, Integer> get) {
    return Math.abs(get.apply(l) - get.apply(r)) >= 10
        && Math.min(get.apply(l), get.apply(r)) <= 65;
  }

  @JsonIgnore
  public boolean isWinterEligible() {
    if (hasPitching()) {
      PitchingRatings l = getPitching().getVsLeft();
      PitchingRatings r = getPitching().getVsRight();

      return pitchingChecks().anyMatch(f -> isWinter(l, r, f));
    } else {
      BattingRatings l = getBatting().getVsLeft();
      BattingRatings r = getBatting().getVsRight();

      return battingChecks().anyMatch(f -> isWinter(l, r, f));
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("batting", batting)
        .append("pitching", pitching)
        .append("defensive", defensive)
        .toString();
  }

  public static BattingRatings<Integer> getOverallBatting(Splits<BattingRatings<?>> splits) {
    Integer vR = (int) Math.round(splitPercentages.getVsRhpPercentage() * 1000);
    Integer vL = 1000 - vR;

    BattingRatings.Builder<Integer> builder =
        BattingRatings.builder(new OneToOneHundred())
            .contact(
                OneToOneHundred.valueOf(
                    (vR * splits.getVsRight().getContact() + vL * splits.getVsLeft().getContact())
                        / 1000))
            .gap(
                OneToOneHundred.valueOf(
                    (vR * splits.getVsRight().getGap() + vL * splits.getVsLeft().getGap()) / 1000))
            .power(
                OneToOneHundred.valueOf(
                    (vR * splits.getVsRight().getPower() + vL * splits.getVsLeft().getPower())
                        / 1000))
            .eye(
                OneToOneHundred.valueOf(
                    (vR * splits.getVsRight().getEye() + vL * splits.getVsLeft().getEye()) / 1000))
            .runningSpeed(splits.getVsRight().getRunningSpeed());

    if (splits.getVsRight().getK().isPresent() && splits.getVsLeft().getK().isPresent()) {
      builder =
          builder.k(
              OneToOneHundred.valueOf(
                  (vR * splits.getVsRight().getK().get() + vL * splits.getVsLeft().getK().get())
                      / 1000));
    }

    return builder.build();
  }

  public static PitchingRatings getOverallPitching(Splits<PitchingRatings<?>> splits) {
    Integer vR = (int) Math.round(splitPercentages.getVsRhbPercentage() * 1000);
    Integer vL = 1000 - vR;

    return PitchingRatings.builder(new OneToOneHundred())
        .stuff((vR * splits.getVsRight().getStuff() + vL * splits.getVsLeft().getStuff()) / 1000)
        .control(
            (vR * splits.getVsRight().getControl() + vL * splits.getVsLeft().getControl()) / 1000)
        .movement(
            (vR * splits.getVsRight().getMovement() + vL * splits.getVsLeft().getMovement()) / 1000)
        .hits((vR * splits.getVsRight().getHits() + vL * splits.getVsLeft().getHits()) / 1000)
        .gap((vR * splits.getVsRight().getGap() + vL * splits.getVsLeft().getGap()) / 1000)
        .endurance(splits.getVsLeft().getEndurance())
        .build();
  }

  public static PlayerRatings create(
      Splits<BattingRatings<?>> batting,
      DefensiveRatings defensive,
      Splits<PitchingRatings<?>> pitching,
      RatingsDefinition definition) {

    PlayerRatings pr = new PlayerRatings(batting, defensive, pitching);
    pr.setDefinition(definition);
    return pr;
  }
}
