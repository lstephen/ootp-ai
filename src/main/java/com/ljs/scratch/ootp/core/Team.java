package com.ljs.scratch.ootp.core;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.PitchingRatings;
import com.ljs.scratch.ootp.ratings.Splits;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

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

    public void processManualChanges(File file, Site site) {
        try {
            for (String s : Files.readLines(file, Charsets.UTF_8)) {
                if (s.isEmpty()) {
                    continue;
                }

                PlayerId id = new PlayerId(StringUtils.substringAfter(s, ","));

                if (s.charAt(0) == 'a') {
                    players.add(site.getPlayer(id).extract());
                }

                if (s.charAt(0) == 'r' && containsPlayer(id)) {
                    players.remove(getPlayer(id));
                }
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public void remove(Player p) {
        players.remove(p);
        injured.remove(p);
    }


    @Override
    public Iterator<Player> iterator() { return players.iterator(); }

}
