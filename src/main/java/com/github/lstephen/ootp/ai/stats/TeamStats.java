package com.github.lstephen.ootp.ai.stats;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.PlayerId;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * TODO: Two maps should be unnecessary. Probably need a custom serializer
 * to use with just one though. Jackson needs to serialize the key to a String.
 * @author lstephen
 */
public final class TeamStats<S extends Stats<S>> {

    private final Map<PlayerId, SplitStats<S>> stats = Maps.newHashMap();

    private final Map<PlayerId, Player> players = Maps.newHashMap();

    private TeamStats(Map<Player, SplitStats<S>> stats) {
        for (Map.Entry<Player, SplitStats<S>> entry : stats.entrySet()) {
            players.put(entry.getKey().getId(), entry.getKey());
            this.stats.put(entry.getKey().getId(), entry.getValue());
        }
    }

    @JsonCreator
    private TeamStats(
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

    public static <S extends Stats<S>> TeamStats<S> create(
        Map<Player, SplitStats<S>> stats) {

        return new TeamStats<S>(stats);
    }

}
