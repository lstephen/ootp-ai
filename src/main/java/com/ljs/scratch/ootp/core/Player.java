package com.ljs.scratch.ootp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.DefensiveRatings;
import com.ljs.scratch.ootp.ratings.PitchingRatings;
import com.ljs.scratch.ootp.ratings.PlayerRatings;
import com.ljs.scratch.ootp.ratings.Splits;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author lstephen
 */
public final class Player {

    private static final Integer MAX_SURNAME_LENGTH = 12;

    private PlayerId id;

    private String name;

    private int age;

    private String team;

    private String salary;

    private PlayerRatings ratings;

    @JsonIgnore
    private Optional<Boolean> on40Man = Optional.absent();

    @JsonIgnore
    private Optional<Boolean> ruleFiveEligible = Optional.absent();

    @JsonIgnore
    private Optional<Boolean> outOfOptions = Optional.absent();

    @JsonIgnore
    private Optional<Boolean> clearedWaivers = Optional.absent();

    private Player() { /* JAXB */ }

    private Player(PlayerId id, String name, PlayerRatings ratings) {
        this.id = id;
        this.name = name;
        this.ratings = ratings;
    }

    public PlayerId getId() { return id; }

    public boolean hasId(PlayerId id) {
        return this.id.equals(id);
    }

    public String getName() { return name; }

    public String getShortName() {
        List<String> names = Arrays.asList(StringUtils.split(name));

        String surnames =
            StringUtils.abbreviate(
                Joiner
                    .on(' ')
                    .join(names.subList(1, names.size())), MAX_SURNAME_LENGTH);

        Character initial = names.get(0).charAt(0);

        return String.format("%s, %s", surnames, initial);
    }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public Optional<Boolean> getOn40Man() { return on40Man; }
    public void setOn40Man(Boolean on40Man) {
        this.on40Man = Optional.of(on40Man);
    }

    private Optional<Boolean> getRuleFiveEligible() {
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

    public String getRosterStatus() {
        StringBuilder str = new StringBuilder();

        if (getOn40Man().isPresent()) {
            str.append(getOn40Man().get() ? "*" : " ");
        }

        if (getOutOfOptions().isPresent()) {
            str.append(getOutOfOptions().get() ? "+" : " ");
        }

        if (getRuleFiveEligible().isPresent()) {
            str.append(getRuleFiveEligible().get() ? "#" : " ");
        }

        if (getClearedWaivers().isPresent()) {
            str.append(getClearedWaivers().get() ? "~" : " ");
        }

        return str.toString();
    }

    public String getPosition() {
        if (hasPitchingRatings()) {
            return getPitchingRatings().getVsRight().getEndurance() > 5
                ? "SP"
                : "MR";
        } else {
            return getDefensiveRatings().getPrimaryPosition();
        }
    }

    public DefensiveRatings getDefensiveRatings() {
        return ratings.getDefensive();
    }

    public Splits<BattingRatings> getBattingRatings() {
        return ratings.getBatting();
    }

    public Splits<BattingRatings> getBattingPotentialRatings() {
        return ratings.getBattingPotential(age);
    }

    public boolean hasPitchingRatings() {
        return ratings.hasPitching();
    }

    public Splits<PitchingRatings> getPitchingRatings() {
        return ratings.getPitching();
    }

    public Splits<PitchingRatings> getPitchingPotentialRatings() {
        return ratings.getPitchingPotential(age);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (!obj.getClass().equals(getClass())) { return false; }

        return Player.class.cast(obj).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static Player create(
        PlayerId id, String name, PlayerRatings ratings) {

        return new Player(id, name, ratings);
    }

    public static Ordering<Player> byAge() {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Integer>() {
                @Override
                public Integer apply(Player p) {
                    return p.getAge();
                }
            });
    }

    public static Ordering<Player> byWeightedBattingRating() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Double>() {
                public Double apply(Player p) {
                    BattingRatings ratings =
                        PlayerRatings.getOverallBatting(p.getBattingRatings());

                    return 0.7 * ratings.getEye()
                        + 0.9 * ratings.getContact()
                        + 1.3 * ratings.getGap()
                        + 2.0 * ratings.getPower();
                }
            });
    }

}
