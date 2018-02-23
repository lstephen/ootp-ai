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
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/** @author lstephen */
public final class PlayerRatings {

  private static final Integer PEAK_AGE = 27;

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

  public Splits<BattingRatings<Integer>> getBattingPotential(int age) {

    BattingRatings<Integer> ovr = getOverallBatting(getBatting());

    BattingRatings<Integer> capped =
        BattingRatings.builder(new OneToOneHundred())
            .contact(capBattingPotential(age, ovr.getContact(), battingPotential.getContact()))
            .gap(capBattingPotential(age, ovr.getGap(), battingPotential.getGap()))
            .triplesOptionalRating(
                capBattingPotential(age, ovr.getTriples(), battingPotential.getTriples()))
            .power(capBattingPotential(age, ovr.getPower(), battingPotential.getPower()))
            .eye(capBattingPotential(age, ovr.getEye(), battingPotential.getEye()))
            .k(capBattingPotential(age, ovr.getK(), battingPotential.getK()))
            .build();

    BattingRatings<?> curVsLeft = getBatting().getVsLeft();

    BattingRatings<Integer> potVsLeft =
        BattingRatings.builder(new OneToOneHundred())
            .contact(capBatting(age, curVsLeft.getContact(), capped.getContact(), ovr.getContact()))
            .gap(capBatting(age, curVsLeft.getGap(), capped.getGap(), ovr.getGap()))
            .triples(capBatting(age, curVsLeft.getTriples(), capped.getTriples(), ovr.getTriples()))
            .power(capBatting(age, curVsLeft.getPower(), capped.getPower(), ovr.getPower()))
            .eye(capBatting(age, curVsLeft.getEye(), capped.getEye(), ovr.getEye()))
            .k(capBatting(age, curVsLeft.getK(), capped.getK(), ovr.getK()))
            .runningSpeed(curVsLeft.getRunningSpeed())
            .build();

    BattingRatings<?> curVsRight = getBatting().getVsRight();

    BattingRatings<Integer> potVsRight =
        BattingRatings.builder(new OneToOneHundred())
            .contact(
                capBatting(age, curVsRight.getContact(), capped.getContact(), ovr.getContact()))
            .gap(capBatting(age, curVsRight.getGap(), capped.getGap(), ovr.getGap()))
            .triples(
                capBatting(age, curVsRight.getTriples(), capped.getTriples(), ovr.getTriples()))
            .power(capBatting(age, curVsRight.getPower(), capped.getPower(), ovr.getPower()))
            .eye(capBatting(age, curVsRight.getEye(), capped.getEye(), ovr.getEye()))
            .k(capBatting(age, curVsRight.getK(), capped.getK(), ovr.getK()))
            .runningSpeed(curVsRight.getRunningSpeed())
            .build();

    return Splits.create(potVsLeft, potVsRight);
  }

  public Splits<PitchingRatings<Integer>> getPitchingPotential(int age) {
    PitchingRatings<?> ovr = getOverallPitching(getPitching());

    PitchingRatings<Integer> capped =
        PitchingRatings.builder(new OneToOneHundred())
            .stuff(capPitchingPotential(age, ovr.getStuff(), pitchingPotential.getStuff()))
            .control(capPitchingPotential(age, ovr.getControl(), pitchingPotential.getControl()))
            .movement(capPitchingPotential(age, ovr.getMovement(), pitchingPotential.getMovement()))
            .hits(capPitchingPotential(age, ovr.getHits(), pitchingPotential.getHits()))
            .gap(capPitchingPotential(age, ovr.getGap(), pitchingPotential.getGap()))
            .build();

    PitchingRatings<?> curVsLeft = getPitching().getVsLeft();

    PitchingRatings<Integer> potVsLeft =
        PitchingRatings.builder(new OneToOneHundred())
            .stuff(capPitching(age, curVsLeft.getStuff(), capped.getStuff(), ovr.getStuff()))
            .control(
                capPitching(age, curVsLeft.getControl(), capped.getControl(), ovr.getControl()))
            .movement(
                capPitching(age, curVsLeft.getMovement(), capped.getMovement(), ovr.getMovement()))
            .hits(capPitching(age, curVsLeft.getHits(), capped.getHits(), ovr.getHits()))
            .gap(capPitching(age, curVsLeft.getGap(), capped.getGap(), ovr.getGap()))
            .groundBallPct(curVsLeft.getGroundBallPct())
            .endurance(ovr.getEndurance())
            .runs(Optional.absent())
            .build();

    PitchingRatings<?> curVsRight = getPitching().getVsRight();

    PitchingRatings<Integer> potVsRight =
        PitchingRatings.builder(new OneToOneHundred())
            .stuff(capPitching(age, curVsRight.getStuff(), capped.getStuff(), ovr.getStuff()))
            .control(
                capPitching(age, curVsRight.getControl(), capped.getControl(), ovr.getControl()))
            .movement(
                capPitching(age, curVsRight.getMovement(), capped.getMovement(), ovr.getMovement()))
            .hits(capPitching(age, curVsRight.getHits(), capped.getHits(), ovr.getHits()))
            .gap(capPitching(age, curVsRight.getGap(), capped.getGap(), ovr.getGap()))
            .groundBallPct(curVsRight.getGroundBallPct())
            .endurance(ovr.getEndurance())
            .runs(Optional.absent())
            .build();

    return Splits.create(potVsLeft, potVsRight);
  }

  private Rating<Integer, OneToOneHundred> capBatting(
      int age, Optional<Integer> current, Optional<Integer> capped, Optional<Integer> overall) {

    if (!Stream.of(current, capped, overall).allMatch(Optional::isPresent)) {
      return null;
    }

    return capBatting(age, current.get(), capped.get(), overall.get());
  }

  private Rating<Integer, OneToOneHundred> capBatting(
      int age, int current, int capped, int overall) {
    return capBattingPotential(age, current, capped + (current - overall));
  }

  private Optional<Rating<Integer, OneToOneHundred>> capBattingPotential(
      int age, Optional<Integer> current, Optional<Integer> potential) {
    if (!current.isPresent() || !potential.isPresent()) {
      return Optional.absent();
    }

    return Optional.of(capBattingPotential(age, current.get(), potential.get()));
  }

  private Rating<Integer, OneToOneHundred> capBattingPotential(
      int age, int current, int potential) {

    if (definition.isFreezeOneRatings() && current < 10) {
      return OneToOneHundred.valueOf(current);
    }

    if (current == 0) {
      return OneToOneHundred.valueOf(current);
    }

    //Double factor = definition.getYearlyRatingsIncrease();
    Integer factor = 8;

    Integer value =
        Math.max(current, Math.min(potential, current + factor * Math.max(PEAK_AGE - age, 0)));

    return OneToOneHundred.valueOf(value);
  }

  private Rating<Integer, OneToOneHundred> capPitching(
      int age, int current, int capped, int overall) {
    return capPitchingPotential(age, current, capped + (current - overall));
  }

  private Rating<Integer, OneToOneHundred> capPitchingPotential(
      int age, int current, int potential) {

    if (definition.isFreezeOneRatings() && current < 10) {
      return OneToOneHundred.valueOf(current);
    }

    if (current == 0) {
      OneToOneHundred.valueOf(current);
    }

    Integer factor = 8;

    Integer value =
        Math.max(current, Math.min(potential, current + factor * Math.max(PEAK_AGE - age, 0)));

    return OneToOneHundred.valueOf(value);
  }

  public void setBattingPotential(BattingRatings ratings) {
    this.battingPotential = ratings;
  }

  public void setPitchingPotential(PitchingRatings ratings) {
    this.pitchingPotential = ratings;
  }

  private static final Stream<Function<PitchingRatings, Integer>> PITCHING_CHECKS = Stream.of(PitchingRatings::getStuff, PitchingRatings::getControl, PitchingRatings::getMovement);
  private static final Stream<Function<BattingRatings, Integer>> BATTING_CHECKS = Stream.of(BattingRatings::getContact, BattingRatings::getGap, BattingRatings::getPower, BattingRatings::getEye, rat -> (Integer) rat.getK().or(0));

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

      return PITCHING_CHECKS.anyMatch(f -> isTO(l, r, p, f));
    } else {
      BattingRatings l = getBatting().getVsLeft();
      BattingRatings r = getBatting().getVsRight();
      BattingRatings p = getRawBattingPotential();

      return BATTING_CHECKS.anyMatch(f -> isTO(l, r, p, f));
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

      return PITCHING_CHECKS.anyMatch(f -> isAfl(l, r, f));
    } else {
      BattingRatings l = getBatting().getVsLeft();
      BattingRatings r = getBatting().getVsRight();

      return BATTING_CHECKS.anyMatch(f -> isAfl(l, r, f));
    }
  }

  private static <R> boolean isWinter(R l, R r, Function<R, Integer> get) {
    return Math.abs(get.apply(l) - get.apply(r)) >= 10 && Math.min(get.apply(l), get.apply(r)) <= 65;
  }

  @JsonIgnore
  public boolean isWinterEligible() {
    if (hasPitching()) {
      PitchingRatings l = getPitching().getVsLeft();
      PitchingRatings r = getPitching().getVsRight();

      return PITCHING_CHECKS.anyMatch(f -> isWinter(l, r, f));
    } else {
      BattingRatings l = getBatting().getVsLeft();
      BattingRatings r = getBatting().getVsRight();

      return BATTING_CHECKS.anyMatch(f -> isWinter(l, r, f));
    }
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("batting", batting)
        .add("pitching", pitching)
        .add("defensive", defensive)
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
