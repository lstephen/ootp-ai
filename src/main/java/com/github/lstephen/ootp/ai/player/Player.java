package com.github.lstephen.ootp.ai.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.lstephen.ootp.ai.player.ratings.BattingRatings;
import com.github.lstephen.ootp.ai.player.ratings.DefensiveRatings;
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings;
import com.github.lstephen.ootp.ai.player.ratings.PlayerRatings;
import com.github.lstephen.ootp.ai.player.ratings.Position;
import com.github.lstephen.ootp.ai.player.ratings.RatingsDefinition;
import com.github.lstephen.ootp.ai.player.ratings.StarRating;
import com.github.lstephen.ootp.ai.rating.Rating;
import com.github.lstephen.ootp.ai.splits.Splits;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** @author lstephen */
public final class Player {

  private static final Integer MAX_SURNAME_LENGTH = 12;

  private final PlayerId id;

  private final String name;

  private int age;

  private String team;

  private String salary;

  private final PlayerRatings ratings;

  private BattingHand battingHand;

  @JsonIgnore private Optional<String> listedPosition = Optional.absent();

  @JsonIgnore private RosterStatus rosterStatus = RosterStatus.create();

  @JsonIgnore private ImmutableList<Slot> slots;

  @JsonIgnore private Optional<StarRating> stars = Optional.absent();

  @JsonIgnore private Optional<Clutch> clutch = Optional.absent();

  @JsonIgnore private Optional<Consistency> consistency = Optional.absent();

  private Player(PlayerId id, String name, PlayerRatings ratings) {
    this.id = id;
    this.name = name;
    this.ratings = ratings;
  }

  public PlayerId getId() {
    return id;
  }

  public boolean hasId(PlayerId id) {
    return this.id.equals(id);
  }

  public void setRatingsDefinition(RatingsDefinition definition) {
    ratings.setDefinition(definition);
  }

  public String getName() {
    return name;
  }

  public String getShortName() {
    List<String> names = Arrays.asList(StringUtils.split(name));

    String surnames =
        StringUtils.abbreviate(
            Joiner.on(' ').join(names.subList(1, names.size())), MAX_SURNAME_LENGTH);

    Character initial = names.get(0).charAt(0);

    return String.format("%s, %s", surnames, initial);
  }

  public String getFirstName() {
    return Arrays.asList(StringUtils.split(name)).get(0);
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getTeam() {
    return team;
  }

  public void setTeam(String team) {
    this.team = team;
  }

  public String getSalary() {
    return salary;
  }

  public void setSalary(String salary) {
    this.salary = salary;
  }

  public BattingHand getBattingHand() {
    return battingHand;
  }

  public void setBattingHand(BattingHand battingHand) {
    this.battingHand = battingHand;
  }

  public Optional<StarRating> getStars() {
    return stars;
  }

  public void setStars(StarRating stars) {
    this.stars = Optional.of(stars);
  }

  public Optional<Clutch> getClutch() {
    return clutch;
  }

  public void setClutch(Clutch clutch) {
    this.clutch = Optional.of(clutch);
  }

  public Optional<Consistency> getConsistency() {
    return consistency;
  }

  public void setConsistency(Consistency consistency) {
    this.consistency = Optional.of(consistency);
  }

  public String getIntangibles() {
    String clutch = " ";

    if (getClutch().isPresent()) {
      switch (getClutch().get()) {
        case GREAT:
          clutch = "+";
          break;
        case SUFFERS:
          clutch = "-";
          break;
        default: // nothing
      }
    }

    String consistency = " ";
    if (getConsistency().isPresent()) {
      switch (getConsistency().get()) {
        case GOOD:
          consistency = "+";
          break;
        case VERY_INCONSISTENT:
          consistency = "-";
          break;
        default: // nothing
      }
    }

    return clutch + consistency;
  }

  public Optional<Boolean> getOn40Man() {
    return rosterStatus.getOn40Man();
  }

  public void setOn40Man(Boolean on40Man) {
    rosterStatus.setOn40Man(on40Man);
  }

  public Optional<Boolean> getRuleFiveEligible() {
    return rosterStatus.getRuleFiveEligible();
  }

  public void setRuleFiveEligible(Boolean ruleFiveEligible) {
    rosterStatus.setRuleFiveEligible(ruleFiveEligible);
  }

  public Optional<Boolean> getOutOfOptions() {
    return rosterStatus.getOutOfOptions();
  }

  public void setOutOfOptions(Boolean outOfOptions) {
    rosterStatus.setOutOfOptions(outOfOptions);
  }

  public Optional<Boolean> getClearedWaivers() {
    return rosterStatus.getClearedWaivers();
  }

  public void setClearedWaivers(Boolean clearedWaivers) {
    rosterStatus.setClearedWaivers(clearedWaivers);
  }

  public Optional<Integer> getYearsOfProService() {
    return rosterStatus.getYearsOfProService();
  }

  public void setYearsOfProService(Integer years) {
    rosterStatus.setYearsOfProService(years);
  }

  public Optional<Integer> getTeamTopProspectPosition() {
    return rosterStatus.getTeamTopProspectPosition();
  }

  public void setTeamTopProspectPosition(Integer position) {
    rosterStatus.setTeamTopProspectPosition(position);
  }

  public Boolean isInjured() {
    return rosterStatus.isInjured();
  }

  public void setInjured(Boolean injured) {
    rosterStatus.setInjured(injured);
  }

  public Boolean isUpcomingFreeAgent() {
    return rosterStatus.isUpcomingFreeAgent();
  }

  public void setUpcomingFreeAgent(Boolean ufa) {
    rosterStatus.setUpcomingFreeAgent(ufa);
  }

  public ImmutableList<Slot> getSlots() {
    if (slots == null) {
      slots = Slot.getPlayerSlots(this);
    }
    return slots;
  }

  public String getRosterStatus() {
    StringBuilder str = new StringBuilder();

    str.append(isInjured() ? "I" : " ");
    str.append(isUpcomingFreeAgent() ? "F" : " ");

    Optional<Integer> pos = getTeamTopProspectPosition();
    if (pos.isPresent()) {
      if (pos.get() > 10) {
        str.append(">");
      } else if (pos.get() == 10) {
        str.append("T");
      } else {
        str.append(pos.get());
      }
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
      return getPitchingRatings().getVsRight().getEndurance() > 5 ? "SP" : "MR";
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

  @JsonIgnore
  public boolean isHitter() {
    return getBattingRatings() != null && !isPitcher();
  }

  @JsonIgnore
  public boolean isPitcher() {
    return hasPitchingRatings();
  }

  public Boolean canPlay(Position pos) {
    return pos == Position.DESIGNATED_HITTER
        || pos == Position.FIRST_BASE
        || getDefensiveRatings().getPositionScore(pos) > 1.0e-6;
  }

  public DefensiveRatings getDefensiveRatings() {
    return ratings.getDefensive();
  }

  public Splits<BattingRatings<?>> getBattingRatings() {
    return ratings.getBatting();
  }

  public Splits<BattingRatings<Integer>> getBattingPotentialRatings() {
    return ratings.getBattingPotential(age);
  }

  public Rating<?, ?> getBuntForHitRating() {
    return ratings.getBuntForHit();
  }

  public Rating<?, ?> getStealingRating() {
    return ratings.getStealing();
  }

  public boolean hasPitchingRatings() {
    return ratings.hasPitching();
  }

  public Splits<PitchingRatings<?>> getPitchingRatings() {
    return ratings.getPitching();
  }

  public Splits<PitchingRatings<Integer>> getPitchingPotentialRatings() {
    return ratings.getPitchingPotential(age);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!obj.getClass().equals(getClass())) {
      return false;
    }

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

  /**
   * An ordering designed to produce consistent results when players are otherwise valued equally.
   */
  public static Ordering<Player> byTieBreak() {
    return Player.byAge().compound(Player.byShortName());
  }

  public static Ordering<Player> byAge() {
    return Ordering.natural().onResultOf(Player::getAge);
  }

  public static Ordering<Player> byShortName() {
    return Ordering.natural().onResultOf(Player::getShortName);
  }

  public static Ordering<Player> byWeightedBattingRating() {
    return Ordering.natural()
        .reverse()
        .onResultOf(
            new Function<Player, Double>() {
              @Override
              public Double apply(Player p) {
                Preconditions.checkNotNull(p);

                BattingRatings ratings = PlayerRatings.getOverallBatting(p.getBattingRatings());

                // We don't use calculated WOBA constants, as we want to be
                // able to use this when we have no stats/regression available
                return 0.7 * ratings.getEye()
                    + 0.9 * ratings.getContact()
                    + 1.3 * ratings.getGap()
                    + 2.0 * ratings.getPower();
              }
            });
  }
}
