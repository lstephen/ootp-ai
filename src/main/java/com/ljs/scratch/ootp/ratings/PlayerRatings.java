package com.ljs.scratch.ootp.ratings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.SplitPercentages;

/**
 *
 * @author lstephen
 */
public final class PlayerRatings {

    private final Splits<BattingRatings> batting;

    private final DefensiveRatings defensive;

    private final Splits<PitchingRatings> pitching;

    private BattingRatings battingPotential;

    private PitchingRatings pitchingPotential;

    @JsonIgnore
    private Version siteType;

    @JsonIgnore
    private SiteDefinition site;

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

    public void setSite(SiteDefinition site) {
        this.site = site;
        this.siteType = site.getType();
    }

    public DefensiveRatings getDefensive() { return defensive; }

    public Splits<BattingRatings> getBatting() { return batting; }

    public Splits<PitchingRatings> getPitching() { return pitching; }

    public boolean hasPitching() { return pitching != null; }

    public Splits<BattingRatings> getBattingPotential(int age) {

        BattingRatings ovr = getOverallBatting(getBatting());

        BattingRatings capped = RatingsBuilder
            .batting()
            .contact(capPotential(age, ovr.getContact(), battingPotential.getContact()))
            .gap(capPotential(age, ovr.getGap(), battingPotential.getGap()))
            .power(capPotential(age, ovr.getPower(), battingPotential.getPower()))
            .eye(capPotential(age, ovr.getEye(), battingPotential.getEye()))
            .build();

        BattingRatings curVsLeft = getBatting().getVsLeft();

        BattingRatings potVsLeft = RatingsBuilder
            .batting()
            .contact(cap(age, curVsLeft.getContact(), capped.getContact(), ovr.getContact()))
            .gap(cap(age, curVsLeft.getGap(), capped.getGap(), ovr.getGap()))
            .power(cap(age, curVsLeft.getPower(), capped.getPower(), ovr.getPower()))
            .eye(cap(age, curVsLeft.getEye(), capped.getEye(), ovr.getEye()))
            .build();

        BattingRatings curVsRight = getBatting().getVsRight();

        BattingRatings potVsRight = RatingsBuilder
            .batting()
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
        double factor = 1;

        if (site.getName().equals("TWML")) {
            factor = 1.5;
        }
        if (site.getName().equals("BTH")) {
            factor = 8;
        }

        if (siteType == Version.OOTP6 && current == 1) {
            return 1;
        }

        if (current == 0) {
            return 0;
        }

        return Math.max(current, Math.min(potential, (int) (current + factor * Math.max(27 - age, 0))));
    }

    public void setBattingPotential(BattingRatings ratings) {
        this.battingPotential = ratings;
    }

    public void setPitchingPotential(PitchingRatings ratings) {
        this.pitchingPotential = ratings;
    }

    public static BattingRatings getOverallBatting(Splits<BattingRatings> splits) {
        Integer vR = (int) (splitPercentages.getVsRhpPercentage() * 100);
        Integer vL = (int) (splitPercentages.getVsLhpPercentage() * 100);

        BattingRatings ovr = RatingsBuilder
            .batting()
            .contact((vR * splits.getVsRight().getContact() + vL * splits.getVsLeft().getContact()) / 100)
            .gap((vR * splits.getVsRight().getGap() + vL * splits.getVsLeft().getGap()) / 100)
            .power((vR * splits.getVsRight().getPower() + vL * splits.getVsLeft().getPower()) / 100)
            .eye((vR * splits.getVsRight().getEye() + vL * splits.getVsLeft().getEye()) / 100)
            .build();

        return ovr;
    }

    public static PitchingRatings getOverallPitching(Splits<PitchingRatings> splits) {
        Integer vR = (int) (splitPercentages.getVsRhbPercentage() * 100);
        Integer vL = (int) (splitPercentages.getVsLhbPercentage() * 100);

        PitchingRatings ovr = new PitchingRatings();
        ovr.setStuff((vR * splits.getVsRight().getStuff() + vL * splits.getVsLeft().getStuff()) / 100);
        ovr.setControl((vR * splits.getVsRight().getControl() + vL * splits.getVsLeft().getControl()) / 100);
        ovr.setMovement((vR * splits.getVsRight().getMovement() + vL * splits.getVsLeft().getMovement()) / 100);
        ovr.setHits((vR * splits.getVsRight().getHits() + vL * splits.getVsLeft().getHits()) / 100);
        ovr.setGap((vR * splits.getVsRight().getGap() + vL * splits.getVsLeft().getGap()) / 100);

        return ovr;
    }

    public static PlayerRatings create(
        Splits<BattingRatings> batting,
        DefensiveRatings defensive,
        Splits<PitchingRatings> pitching,
        SiteDefinition site) {

        PlayerRatings pr = new PlayerRatings(batting, defensive, pitching);
        pr.setSite(site);
        return pr;
    }

}
