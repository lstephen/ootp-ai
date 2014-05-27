package com.ljs.ootp.ai.player;

import com.google.common.base.Optional;

/**
 *
 * @author lstephen
 */
public final class RosterStatus {

    private Boolean upcomingFreeAgent = Boolean.FALSE;

    private Boolean injured = Boolean.FALSE;

    private Optional<Boolean> on40Man = Optional.absent();

    private Optional<Boolean> ruleFiveEligible = Optional.absent();

    private Optional<Boolean> outOfOptions = Optional.absent();

    private Optional<Boolean> clearedWaivers = Optional.absent();

    private Optional<Integer> yearsOfProService = Optional.absent();

    private Optional<Integer> teamTopProspectPosition = Optional.absent();

    private RosterStatus() { }

    public Boolean isUpcomingFreeAgent() {
        return upcomingFreeAgent;
    }

    public void setUpcomingFreeAgent(Boolean upcomingFreeAgent) {
        this.upcomingFreeAgent = upcomingFreeAgent;
    }

    public Boolean isInjured() {
        return injured;
    }

    public void setInjured(Boolean injured) {
        this.injured = injured;
    }

    public Optional<Boolean> getOn40Man() { return on40Man; }
    public void setOn40Man(Boolean on40Man) {
        this.on40Man = Optional.of(on40Man);
    }

    public Optional<Boolean> getRuleFiveEligible() {
        return ruleFiveEligible;
    }

    public void setRuleFiveEligible(Boolean ruleFiveEligible) {
        this.ruleFiveEligible = Optional.of(ruleFiveEligible);
    }

    public Optional<Boolean> getOutOfOptions() {
        return outOfOptions;
    }

    public void setOutOfOptions(Boolean outOfOptions) {
        this.outOfOptions = Optional.of(outOfOptions);
    }

    public Optional<Boolean> getClearedWaivers() {
        return clearedWaivers;
    }

    public void setClearedWaivers(Boolean clearedWaivers) {
        this.clearedWaivers = Optional.of(clearedWaivers);
    }

    public Optional<Integer> getYearsOfProService() {
        return yearsOfProService;
    }

    public void setYearsOfProService(Integer years) {
        this.yearsOfProService = Optional.of(years);
    }

    public Optional<Integer> getTeamTopProspectPosition() {
        return teamTopProspectPosition;
    }

    public void setTeamTopProspectPosition(Integer position) {
        this.teamTopProspectPosition = Optional.of(position);
    }

    public static RosterStatus create() {
        return new RosterStatus();
    }

}
