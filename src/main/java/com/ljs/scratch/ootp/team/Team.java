package com.ljs.scratch.ootp.team;

import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.config.Changes;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.PlayerId;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.PitchingRatings;
import com.ljs.scratch.ootp.ratings.Splits;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class Team implements Iterable<Player> {

    private final Set<Player> players;

    private final Set<Player> injured = Sets.newHashSet();

    public Team(Iterable<Player> players) {
        this.players = Sets.newHashSet(players);
    }

    public boolean containsPlayer(PlayerId id) {
        for (Player p : players) {
            if (p.hasId(id)) {
                return true;
            }
        }
        return false;
    }

    public Player getPlayer(PlayerId id) {
        for (Player p : players) {
            if (p.hasId(id)) {
                return p;
            }
        }
        throw new IllegalStateException();
    }

    public Splits<BattingRatings> getBattingRatings(PlayerId id) {
        return getPlayer(id).getBattingRatings();
    }

    public Splits<PitchingRatings> getPitchingRatings(PlayerId id) {
        return getPlayer(id).getPitchingRatings();
    }

    public void addInjury(Iterable<Player> ps) {
        for (Player p : ps) {
            if (p != null) {
                injured.add(p);
            }
        }
    }

    public Iterable<Player> getInjuries() {
        return injured;
    }

    public void processManualChanges(Changes changes, Site site) {

        for (Player p : changes.get(Changes.ChangeType.ACQUISITION)) {
            players.add(p);
        }

        for (Player p : changes.get(Changes.ChangeType.RELEASE)) {
            if (containsPlayer(p.getId())) {
                players.remove(p);
            }
        }
    }

    public void remove(Player p) {
        players.remove(p);
        injured.remove(p);
    }


    @Override
    public Iterator<Player> iterator() { return players.iterator(); }

}
