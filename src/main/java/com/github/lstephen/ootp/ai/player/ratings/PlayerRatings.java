package com.github.lstephen.ootp.ai.player.ratings;

import com.github.lstephen.ootp.ai.player.ratings.json.BattingPotentialSerializer;
import com.github.lstephen.ootp.ai.rating.OneToOneHundred;
import com.github.lstephen.ootp.ai.rating.Rating;
import com.github.lstephen.ootp.ai.splits.Splits;
import com.github.lstephen.ootp.ai.stats.SplitPercentages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.base.Objects;

/**
 *
 * @author lstephen
 */
public final class PlayerRatings {

    private static final Integer PEAK_AGE = 27;

    private final Splits<BattingRatings<?>> batting;

    private final DefensiveRatings defensive;

    private final Splits<PitchingRatings<?>> pitching;

    @JsonSerialize(using = BattingPotentialSerializer.class)
    private BattingRatings battingPotential;

    private PitchingRatings pitchingPotential;

    @JsonIgnore
    private RatingsDefinition definition;

    @JsonIgnore
    private static SplitPercentages splitPercentages;

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

    public DefensiveRatings getDefensive() { return defensive; }

    public Splits<BattingRatings<?>> getBatting() { return batting; }

    public Splits<PitchingRatings<?>> getPitching() { return pitching; }

    public boolean hasPitching() { return pitching != null; }

    public Splits<BattingRatings<Integer>> getBattingPotential(int age) {

        BattingRatings<?> ovr = getOverallBatting(getBatting());

        BattingRatings<Integer> capped = BattingRatings
            .builder(OneToOneHundred.scale())
            .contact(capBattingPotential(age, ovr.getContact(), battingPotential.getContact()))
            .gap(capBattingPotential(age, ovr.getGap(), battingPotential.getGap()))
            .power(capBattingPotential(age, ovr.getPower(), battingPotential.getPower()))
            .eye(capBattingPotential(age, ovr.getEye(), battingPotential.getEye()))
            .build();

        BattingRatings<?> curVsLeft = getBatting().getVsLeft();

        BattingRatings<Integer> potVsLeft = BattingRatings
            .builder(OneToOneHundred.scale())
            .contact(capBatting(age, curVsLeft.getContact(), capped.getContact(), ovr.getContact()))
            .gap(capBatting(age, curVsLeft.getGap(), capped.getGap(), ovr.getGap()))
            .power(capBatting(age, curVsLeft.getPower(), capped.getPower(), ovr.getPower()))
            .eye(capBatting(age, curVsLeft.getEye(), capped.getEye(), ovr.getEye()))
            .build();

        BattingRatings<?> curVsRight = getBatting().getVsRight();

        BattingRatings<Integer> potVsRight = BattingRatings
            .builder(OneToOneHundred.scale())
            .contact(capBatting(age, curVsRight.getContact(), capped.getContact(), ovr.getContact()))
            .gap(capBatting(age, curVsRight.getGap(), capped.getGap(), ovr.getGap()))
            .power(capBatting(age, curVsRight.getPower(), capped.getPower(), ovr.getPower()))
            .eye(capBatting(age, curVsRight.getEye(), capped.getEye(), ovr.getEye()))
            .build();

        return Splits.create(potVsLeft, potVsRight);
    }

    public Splits<PitchingRatings<Integer>> getPitchingPotential(int age) {
        PitchingRatings<?> ovr = getOverallPitching(getPitching());

        PitchingRatings<Integer> capped = PitchingRatings
            .builder(OneToOneHundred.scale())
            .stuff(capPitchingPotential(age, ovr.getStuff(), pitchingPotential.getStuff()))
            .control(capPitchingPotential(age, ovr.getControl(), pitchingPotential.getControl()))
            .movement(capPitchingPotential(age, ovr.getMovement(), pitchingPotential.getMovement()))
            .hits(capPitchingPotential(age, ovr.getHits(), pitchingPotential.getHits()))
            .gap(capPitchingPotential(age, ovr.getGap(), pitchingPotential.getGap()))
            .endurance(ovr.getEndurance())
            .build();

        PitchingRatings curVsLeft = getPitching().getVsLeft();

        PitchingRatings<Integer> potVsLeft = PitchingRatings
            .builder(OneToOneHundred.scale())
            .stuff(capPitching(age, curVsLeft.getStuff(), capped.getStuff(), ovr.getStuff()))
            .control(capPitching(age, curVsLeft.getControl(), capped.getControl(), ovr.getControl()))
            .movement(capPitching(age, curVsLeft.getMovement(), capped.getMovement(), ovr.getMovement()))
            .hits(capPitching(age, curVsLeft.getHits(), capped.getHits(), ovr.getHits()))
            .gap(capPitching(age, curVsLeft.getGap(), capped.getGap(), ovr.getGap()))
            .endurance(ovr.getEndurance())
            .build();

        PitchingRatings curVsRight = getPitching().getVsRight();

        PitchingRatings<Integer> potVsRight = PitchingRatings
            .builder(OneToOneHundred.scale())
            .stuff(capPitching(age, curVsRight.getStuff(), capped.getStuff(), ovr.getStuff()))
            .control(capPitching(age, curVsRight.getControl(), capped.getControl(), ovr.getControl()))
            .movement(capPitching(age, curVsRight.getMovement(), capped.getMovement(), ovr.getMovement()))
            .hits(capPitching(age, curVsRight.getHits(), capped.getHits(), ovr.getHits()))
            .gap(capPitching(age, curVsRight.getGap(), capped.getGap(), ovr.getGap()))
            .endurance(ovr.getEndurance())
            .build();

        return Splits.create(potVsLeft, potVsRight);
    }

    private Rating<Integer, OneToOneHundred> capBatting(int age, int current, int capped, int overall) {
        return capBattingPotential(age, current, capped + (current - overall));
    }

    private Rating<Integer, OneToOneHundred> capBattingPotential(int age, int current, int potential) {

        if (definition.isFreezeOneRatings() && current < 10) {
            return OneToOneHundred.valueOf(current);
        }

        if (current == 0) {
            return OneToOneHundred.valueOf(current);
        }

        //Double factor = definition.getYearlyRatingsIncrease();
        Integer factor = 8;

        Integer value = Math.max(
            current,
            Math.min(
                potential,
                current + factor * Math.max(PEAK_AGE - age, 0)));

        return OneToOneHundred.valueOf(value);
    }

    private Rating<Integer, OneToOneHundred> capPitching(int age, int current, int capped, int overall) {
        return capPitchingPotential(age, current, capped + (current - overall));
    }

    private Rating<Integer, OneToOneHundred> capPitchingPotential(int age, int current, int potential) {

        if (definition.isFreezeOneRatings() && current < 10) {
            return OneToOneHundred.valueOf(current);
        }

        if (current == 0) {
            OneToOneHundred.valueOf(current);
        }

        Integer factor = 8;

        Integer value = Math.max(
            current,
            Math.min(
                potential,
                current + factor * Math.max(PEAK_AGE - age, 0)));

        return OneToOneHundred.valueOf(value);
    }

    public void setBattingPotential(BattingRatings ratings) {
        this.battingPotential = ratings;
    }

    public void setPitchingPotential(PitchingRatings ratings) {
        this.pitchingPotential = ratings;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("batting", batting)
            .add("pitching", pitching)
            .add("defensive", defensive)
            .toString();
    }

    public static BattingRatings getOverallBatting(Splits<BattingRatings<?>> splits) {
        Integer vR = (int) Math.round(splitPercentages.getVsRhpPercentage() * 1000);
        Integer vL = 1000 - vR;

        BattingRatings ovr = BattingRatings
            .builder(OneToOneHundred.scale())
            .contact(OneToOneHundred.valueOf((vR * splits.getVsRight().getContact() + vL * splits.getVsLeft().getContact()) / 1000))
            .gap(OneToOneHundred.valueOf((vR * splits.getVsRight().getGap() + vL * splits.getVsLeft().getGap()) / 1000))
            .power(OneToOneHundred.valueOf((vR * splits.getVsRight().getPower() + vL * splits.getVsLeft().getPower()) / 1000))
            .eye(OneToOneHundred.valueOf((vR * splits.getVsRight().getEye() + vL * splits.getVsLeft().getEye()) / 1000))
            .build();

        return ovr;
    }

    public static PitchingRatings getOverallPitching(Splits<PitchingRatings<?>> splits) {
        Integer vR = (int) Math.round(splitPercentages.getVsRhbPercentage() * 1000);
        Integer vL = 1000 - vR;

        return PitchingRatings
            .builder(OneToOneHundred.scale())
            .stuff((vR * splits.getVsRight().getStuff() + vL * splits.getVsLeft().getStuff()) / 1000)
            .control((vR * splits.getVsRight().getControl() + vL * splits.getVsLeft().getControl()) / 1000)
            .movement((vR * splits.getVsRight().getMovement() + vL * splits.getVsLeft().getMovement()) / 1000)
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
