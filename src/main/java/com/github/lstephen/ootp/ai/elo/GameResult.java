package com.github.lstephen.ootp.ai.elo;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.roster.Team;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/** @author lstephen */
public final class GameResult {

  private final Id<Team> visitor;

  private final Integer visitorScore;

  private final Id<Team> home;

  private final Integer homeScore;

  private GameResult(Builder builder) {
    Preconditions.checkNotNull(builder.visitor);
    Preconditions.checkNotNull(builder.visitorScore);
    Preconditions.checkNotNull(builder.home);
    Preconditions.checkNotNull(builder.homeScore);

    this.visitor = builder.visitor;
    this.visitorScore = builder.visitorScore;
    this.home = builder.home;
    this.homeScore = builder.homeScore;
  }

  public Id<Team> getVisitor() {
    return visitor;
  }

  public Integer getVisitorScore() {
    return visitorScore;
  }

  public Id<Team> getHome() {
    return home;
  }

  public Integer getHomeScore() {
    return homeScore;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("visitor", visitor)
        .add("visitorScore", visitorScore)
        .add("home", home)
        .add("homeScore", homeScore)
        .toString();
  }

  public static Builder builder() {
    return Builder.create();
  }

  private static GameResult build(Builder builder) {
    return new GameResult(builder);
  }

  public static final class Builder {

    private Id<Team> visitor;

    private Integer visitorScore;

    private Id<Team> home;

    private Integer homeScore;

    private Builder() {}

    public Builder visitor(Id<Team> team, Integer score) {
      this.visitor = team;
      this.visitorScore = score;
      return this;
    }

    public boolean isVisitorSet() {
      return visitor != null;
    }

    public Builder home(Id<Team> team, Integer score) {
      this.home = team;
      this.homeScore = score;
      return this;
    }

    public GameResult build() {
      return GameResult.build(this);
    }

    public static Builder create() {
      return new Builder();
    }
  }
}
