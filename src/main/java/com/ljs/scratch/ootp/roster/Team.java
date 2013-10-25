package com.ljs.scratch.ootp.roster;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.config.Changes;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.player.PlayerSource;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class Team implements Iterable<Player>, PlayerSource {

    private final Set<Player> players;

    private final Set<Player> injured = Sets.newHashSet();

    private Team(Iterable<Player> players) {
        this.players = Sets.newHashSet(players);
    }

    public boolean containsPlayer(Player p) {
        return players.contains(p);
    }

    public boolean containsPlayer(PlayerId id) {
        for (Player p : players) {
            if (p.hasId(id)) {
                return true;
            }
        }
        return false;
    }

    public Player get(PlayerId id) {
        for (Player p : players) {
            if (p.hasId(id)) {
                return p;
            }
        }
        throw new IllegalStateException();
    }

    public void addInjury(Iterable<Player> ps) {
        for (Player p : ps) {
            if (p != null) {
                injured.add(p);
            }
        }
    }

    public Iterable<Player> getInjuries() {
        return ImmutableSet.copyOf(injured);
    }

    public void processManualChanges(Changes changes) {

        for (Player p : changes.get(Changes.ChangeType.ACQUISITION)) {
            players.add(p);
        }

        for (Player p : changes.get(Changes.ChangeType.RELEASE)) {
            remove(p);
        }
    }

    public void remove(Player p) {
        players.remove(p);
        injured.remove(p);
    }


    @Override
    public Iterator<Player> iterator() {
        return players.iterator();
    }

    public static Team create(Iterable<Player> players) {
        return new Team(players);
    }

}
