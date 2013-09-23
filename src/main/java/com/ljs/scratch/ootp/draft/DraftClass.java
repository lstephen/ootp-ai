package com.ljs.scratch.ootp.draft;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.PlayerId;
import com.ljs.scratch.util.Jackson;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class DraftClass {

    private final Set<Player> players = Sets.newHashSet();

    private DraftClass() { }

    @JsonCreator
    private DraftClass(@JsonProperty("players") Set<Player> players) {
        Iterables.addAll(this.players, players);
    }

    public void addIfNotPresent(Player p) {
        if (!this.players.contains(p)) {
            this.players.add(p);
        }
    }

    public void addIfNotPresent(Iterable<Player> ps) {
        for (Player p : ps) {
            addIfNotPresent(p);
        }
    }

    public Iterable<Player> getPlayers() {
        return ImmutableSet.copyOf(players);
    }

    public void save(File f) {
        try {
            Jackson.getMapper().writeValue(f, this);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static DraftClass create(Iterable<Player> ps) {
        DraftClass dc = new DraftClass();
        dc.addIfNotPresent(ps);
        return dc;
    }

    @JsonCreator
    public static DraftClass fromMap(Map<PlayerId, Player> ps) {
        return create(ps.values());
    }

    public static DraftClass load(File f) {
        if (f.exists()) {
            try {
                return Jackson.getMapper().readValue(f, DraftClass.class);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        return create(ImmutableSet.<Player>of());
    }

}
