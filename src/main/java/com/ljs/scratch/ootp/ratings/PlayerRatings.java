package com.ljs.scratch.ootp.ratings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ljs.scratch.ootp.regression.SplitPercentages;

/**
 *
 * @author lstephen
 */
public final class PlayerRatings {

    private static final Integer PEAK_AGE = 27;

    private final Splits<BattingRatings> batting;

    private final DefensiveRatings defensive;

    private final Splits<PitchingRatings> pitching;

    private BattingRatings battingPotential;

    private PitchingRatings pitchingPotential;

    @JsonIgnore
    private RatingsDefinition definition;

    @JsonIgnore
    private static SplitPercentages splitPercentages;

    @JsonCreator
    private PlayerRatings(
        @JsonProperty("batting") Splits<BattingRatings> batting,
        @JsonProperty("defensive") DefensiveRatings defensive,
        @JsonProperty("pitching") Splits<PitchingRatings> pitching) {

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

    public Splits<BattingRatings> getBatting() { return batting; }

    public Splits<PitchingRatings> getPitching() { return pitching; }

    public boolean hasPitching() { return pitching != null; }

    public Splits<BattingRatings> getBattingPotential(int age) {

        BattingRatings ovr = getOverallBatting(getBatting());

        BattingRatings capped = BattingRatings
            .builder()
            .contact(capPotential(age, ovr.getContact(), battingPotential.getContact()))
            .gap(capPotential(age, ovr.getGap(), battingPotential.getGap()))
            .power(capPotential(age, ovr.getPower(), battingPotential.getPower()))
            .eye(capPotential(age, ovr.getEye(), battingPotential.getEye()))
            .build();

        BattingRatings curVsLeft = getBatting().getVsLeft();

        BattingRatings potVsLeft = BattingRatings
            .builder()
            .contact(cap(age, curVsLeft.getContact(), capped.getContact(), ovr.getContact()))
            .gap(cap(age, curVsLeft.getGap(), capped.getGap(), ovr.getGap()))
            .power(cap(age, curVsLeft.getPower(), capped.getPower(), ovr.getPower()))
            .eye(cap(age, curVsLeft.getEye(), capped.getEye(), ovr.getEye()))
            .build();

        BattingRatings curVsRight = getBatting().getVsRight();

        BattingRatings potVsRight = BattingRatings
            .builder()
            .contact(cap(age, curVsRight.getContact(), capped.getContact(), ovr.getContact()))
            .gap(cap(age, curVsRight.getGap(), capped.getGap(), ovr.getGap()))
            .power(cap(age, curVsRight.getPower(), capped.getPower(), ovr.getPower()))
            .eye(cap(age, curVsRight.getEye(), capped.getEye(), ovr.getEye()))
            .build();

        return Splits.create(potVsLeft, potVsRight);
    }

    public Splits<PitchingRatings> getPitchingPotential(int age) {
        PitchingRatings ovr = getOverallPitching(getPitching());

        PitchingRatings capped = new PitchingRatings();
        capped.setStuff(capPotential(age, ovr.getStuff(), pitchingPotential.getStuff()));
        capped.setControl(capPotential(age, ovr.getControl(), pitchingPotential.getControl()));
        capped.setMovement(capPotential(age, ovr.getMovement(), pitchingPotential.getMovement()));
        capped.setHits(capPotential(age, ovr.getHits(), pitchingPotential.getHits()));
        capped.setGap(capPotential(age, ovr.getGap(), pitchingPotential.getGap()));

        PitchingRatings potVsLeft = new PitchingRatings();
        PitchingRatings curVsLeft = getPitching().getVsLeft();

        potVsLeft.setStuff(cap(age, curVsLeft.getStuff(), capped.getStuff(), ovr.getStuff()));
        potVsLeft.setControl(cap(age, curVsLeft.getControl(), capped.getControl(), ovr.getControl()));
        potVsLeft.setMovement(cap(age, curVsLeft.getMovement(), capped.getMovement(), ovr.getMovement()));
        potVsLeft.setHits(cap(age, curVsLeft.getHits(), capped.getHits(), ovr.getHits()));
        potVsLeft.setGap(cap(age, curVsLeft.getGap(), capped.getGap(), ovr.getGap()));

        PitchingRatings potVsRight = new PitchingRatings();
        PitchingRatings curVsRight = getPitching().getVsRight();

        potVsRight.setStuff(cap(age, curVsRight.getStuff(), capped.getStuff(), ovr.getStuff()));
        potVsRight.setControl(cap(age, curVsRight.getControl(), capped.getControl(), ovr.getControl()));
        potVsRight.setMovement(cap(age, curVsRight.getMovement(), capped.getMovement(), ovr.getMovement()));
        potVsRight.setHits(cap(age, curVsRight.getHits(), capped.getHits(), ovr.getHits()));
        potVsRight.setGap(cap(age, curVsRight.getGap(), capped.getGap(), ovr.getGap()));

        return Splits.create(potVsLeft, potVsRight);
    }

    private int cap(int age, int current, int capped, int overall) {
        return capPotential(age, current, capped + (current - overall));
    }

    private int capPotential(int age, int current, int potential) {

        if (definition.isFreezeOneRatings() && current == 1) {
            return 1;
        }

        if (current == 0) {
            return 0;
        }

        Double factor = definition.getYearlyRatingsIncrease();

        return Math.max(
            current,
            Math.min(
                potential,
                (int) (current + factor * Math.max(PEAK_AGE - age, 0))));
    }

    public void setBattingPotential(BattingRatings ratings) {
        this.battingPotential = ratings;
    }

    public void setPitchingPotential(PitchingRatings ratings) {
        this.pitchingPotential = ratings;
    }

    public static BattingRatings getOverallBatting(Splits<BattingRatings> splits) {
        Integer vR = (int) Math.round(splitPercentages.getVsRhpPercentage() * 1000);
        Integer vL = 1000 - vR;

        BattingRatings ovr = BattingRatings
            .builder()
            .contact((vR * splits.getVsRight().getContact() + vL * splits.getVsLeft().getContact()) / 1000)
            .gap((vR * splits.getVsRight().getGap() + vL * splits.getVsLeft().getGap()) / 1000)
            .power((vR * splits.getVsRight().getPower() + vL * splits.getVsLeft().getPower()) / 1000)
            .eye((vR * splits.getVsRight().getEye() + vL * splits.getVsLeft().getEye()) / 1000)
            .build();

        return ovr;
    }

    public static PitchingRatings getOverallPitching(Splits<PitchingRatings> splits) {
        Integer vR = (int) Math.round(splitPercentages.getVsRhbPercentage() * 1000);
        Integer vL = 1000 - vR;

        PitchingRatings ovr = new PitchingRatings();
        ovr.setStuff((vR * splits.getVsRight().getStuff() + vL * splits.getVsLeft().getStuff()) / 1000);
        ovr.setControl((vR * splits.getVsRight().getControl() + vL * splits.getVsLeft().getControl()) / 1000);
        ovr.setMovement((vR * splits.getVsRight().getMovement() + vL * splits.getVsLeft().getMovement()) / 1000);
        ovr.setHits((vR * splits.getVsRight().getHits() + vL * splits.getVsLeft().getHits()) / 1000);
        ovr.setGap((vR * splits.getVsRight().getGap() + vL * splits.getVsLeft().getGap()) / 1000);

        return ovr;
    }

    public static PlayerRatings create(
        Splits<BattingRatings> batting,
        DefensiveRatings defensive,
        Splits<PitchingRatings> pitching,
        RatingsDefinition definition) {

        PlayerRatings pr = new PlayerRatings(batting, defensive, pitching);
        pr.setDefinition(definition);
        return pr;
    }

}
