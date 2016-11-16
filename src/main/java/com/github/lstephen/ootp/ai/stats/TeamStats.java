package com.github.lstephen.ootp.ai.stats;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.PlayerId;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;

/** @author lstephen */
public class TeamStats<S extends Stats<S>> {

  private final Map<PlayerId, SplitStats<S>> stats = Maps.newHashMap();

  private final Map<PlayerId, Player> players = Maps.newHashMap();

  protected TeamStats(Map<Player, SplitStats<S>> stats) {
    for (Map.Entry<Player, SplitStats<S>> entry : stats.entrySet()) {
      players.put(entry.getKey().getId(), entry.getKey());
      this.stats.put(entry.getKey().getId(), entry.getValue());
    }
  }

  @JsonCreator
  protected TeamStats(
      @JsonProperty("stats") Map<PlayerId, SplitStats<S>> stats,
      @JsonProperty("players") Map<PlayerId, Player> players) {

    this.stats.putAll(stats);
    this.players.putAll(players);
  }

  public Iterable<Player> getPlayers() {
    return players.values();
  }

  public boolean contains(Player p) {
    return stats.containsKey(p.getId());
  }

  public Iterable<SplitStats<S>> getSplits() {
    return stats.values();
  }

  public SplitStats<S> getSplits(Player p) {
    Preconditions.checkArgument(
        stats.containsKey(p.getId()), "Expected to find stats for player: %s", p);

    return stats.get(p.getId());
  }

  public S getOverall(Player p) {
    return getSplits(p).getOverall();
  }

  public static <S extends Stats<S>> TeamStats<S> create(Map<Player, SplitStats<S>> stats) {

    return new TeamStats<S>(stats);
  }

  public static class Batting extends TeamStats<BattingStats> {
    private final Map<PlayerId, RunningStats> running = Maps.newHashMap();

    private Batting(TeamStats<BattingStats> batting, Map<Player, RunningStats> running) {
      super(batting.stats, batting.players);

      for (Map.Entry<Player, RunningStats> entry : running.entrySet()) {
        this.running.put(entry.getKey().getId(), entry.getValue());
      }
    }

    @JsonCreator
    private Batting(
        @JsonProperty("stats") Map<PlayerId, SplitStats<BattingStats>> stats,
        @JsonProperty("players") Map<PlayerId, Player> players,
        @JsonProperty("running") Map<PlayerId, RunningStats> running) {

      super(stats, players);

      if (running != null) {
        this.running.putAll(running);
      }
    }

    public static Batting create(
        TeamStats<BattingStats> batting, Map<Player, RunningStats> running) {
      return new Batting(batting, running);
    }
  }
}
