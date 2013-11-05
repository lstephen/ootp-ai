package com.ljs.scratch.ootp.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.DefensiveRatings;
import com.ljs.scratch.ootp.ratings.PitchingRatings;
import com.ljs.scratch.ootp.ratings.PlayerRatings;
import com.ljs.scratch.ootp.ratings.Splits;
import com.ljs.scratch.ootp.selection.Slot;
import com.ljs.scratch.ootp.site.SiteDefinition;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public final class Player {

    private static final Integer MAX_SURNAME_LENGTH = 12;

    private final PlayerId id;

    private final String name;

    private int age;

    private String team;

    private String salary;

    private final PlayerRatings ratings;

    @JsonIgnore
    private Optional<String> listedPosition = Optional.absent();

    @JsonIgnore
    private Optional<Boolean> on40Man = Optional.absent();

    @JsonIgnore
    private Optional<Boolean> ruleFiveEligible = Optional.absent();

    @JsonIgnore
    private Optional<Boolean> outOfOptions = Optional.absent();

    @JsonIgnore
    private Optional<Boolean> clearedWaivers = Optional.absent();

    @JsonIgnore
    private Optional<Integer> yearsOfProService = Optional.absent();

    @JsonIgnore
    private Optional<Integer> teamTopProspectPosition = Optional.absent();

    @JsonIgnore
    private ImmutableList<Slot> slots;

    private Player(PlayerId id, String name, PlayerRatings ratings) {
        this.id = id;
        this.name = name;
        this.ratings = ratings;
    }

    public PlayerId getId() { return id; }

    public boolean hasId(PlayerId id) {
        return this.id.equals(id);
    }

    public void setSite(SiteDefinition site) {
        ratings.setDefinition(site);
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

    public String getFirstName() {
        return Arrays.asList(StringUtils.split(name)).get(0);
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

    public ImmutableList<Slot> getSlots() {
        if (slots == null) {
            slots = Slot.getPlayerSlots(this);
        }
        return slots;
    }

    public String getRosterStatus() {
        StringBuilder str = new StringBuilder();

        Optional<Integer> pos = getTeamTopProspectPosition();
        if (pos.isPresent()) {
            str.append(pos.get() >= 10 ? "T" : pos.get());
        } else {
            str.append(" ");
        }

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

    public Optional<String> getListedPosition() {
        return listedPosition;
    }

    public void setListedPosition(String listedPosition) {
        this.listedPosition = Optional.of(listedPosition);
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

    @JsonCreator
    public static Player create(
        @JsonProperty("id") PlayerId id,
        @JsonProperty("name") String name,
        @JsonProperty("ratings") PlayerRatings ratings) {

        return new Player(id, name, ratings);
    }

    public static Ordering<Player> byAge() {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Integer>() {
                @Override
                public Integer apply(Player p) {
                    Assertions.assertThat(p).isNotNull();

                    return p.getAge();
                }
            });
    }

    public static Ordering<Player> byShortName() {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, String>() {
                @Override
                public String apply(Player p) {
                    Assertions.assertThat(p).isNotNull();

                    return p.getShortName();
                }
            });
    }

    public static Ordering<Player> byWeightedBattingRating() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Double>() {
                @Override
                public Double apply(Player p) {
                    Assertions.assertThat(p).isNotNull();

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
