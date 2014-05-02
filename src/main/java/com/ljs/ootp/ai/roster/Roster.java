package com.ljs.ootp.ai.roster;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.player.PlayerSource;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author lstephen
 */
public final class Roster implements Printable {

    private static final Integer DEFAULT_TARGET_MAXIMUM = 110;

    private static final Integer DEFAULT_TARGET_MINIMUM = 90;

    public static enum Status { ML, AAA, AA, A, SA, R, DL, UNK }

    private final PlayerSource source;

    private final Set<Player> available;

    private final Multimap<Status, Player> assignments =
        ArrayListMultimap.create();

    private Integer targetMaximum = DEFAULT_TARGET_MAXIMUM;

    private Integer targetMinimum = DEFAULT_TARGET_MINIMUM;

    private Roster(PlayerSource source, Iterable<Player> available) {
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(available);

        this.source = source;
        this.available = Sets.newHashSet(available);
    }

    public ImmutableSet<Player> getPlayers(Status status) {
        return ImmutableSet.copyOf(assignments.get(status));
    }

    public Boolean contains(Player p) {
        return assignments.containsValue(p);
    }

    public Integer size() {
        return assignments.size();
    }

    public void remove(Player p) {
        assignments.remove(getStatus(p), p);
    }

    public void release(Player p) {
        remove(p);
        available.remove(p);
    }

    public Collection<Player> getAllPlayers() {
        return assignments.values();
    }

    public ImmutableSet<Player> getUnassigned() {
        return ImmutableSet.copyOf(
                Sets.difference(
                    ImmutableSet.copyOf(available),
                    ImmutableSet.copyOf(assignments.values())));
    }

    public Status getStatus(Player p) {
        for (Map.Entry<Status, Player> entries : assignments.entries()) {
            if (entries.getValue().equals(p)) {
                return entries.getKey();
            }
        }
        return null;
    }

    public void assign(Status status, PlayerId... ids) {
        for (PlayerId id : ids) {
            assign(status, source.get(id));
        }
    }

    public void assign(Status status, Player... ps) {
        assign(status, Arrays.asList(ps));
    }

    public void assign(Status status, Iterable<Player> ps) {
        for (Player p : ps) {
            assign(status, p);
        }
    }

    public void assign(Status status, Player p) {
        if (!assignments.containsValue(p)) {
            assignments.put(status, p);
        }
    }

    public RosterChanges getChangesFrom(Roster src) {
        RosterChanges changes = new RosterChanges();

        Set<Player> playersChanged = Sets.newHashSet();

        playersChanged.addAll(
            Sets.difference(
                ImmutableSet.copyOf(getAllPlayers()),
                ImmutableSet.copyOf(src.getAllPlayers())));

        for (Status s : Status.values()) {
            playersChanged.addAll(
                Sets.difference(src.getPlayers(s), getPlayers(s)));
        }

        for (Player p : playersChanged) {
            changes.addChange(p, src.getStatus(p), getStatus(p));
        }

        return changes;
    }

    public void setTargetMinimum(Integer min) {
        this.targetMinimum = min;
    }
    public void setTargetMaximum(Integer max) {
        this.targetMaximum = max;
    }

    @Override
    public void print(PrintWriter w) {
        Iterable<Status> levels = Ordering
            .explicit(Arrays.asList(Status.values()))
            .sortedCopy(
                ImmutableSet.copyOf(
                    Iterables.concat(
                        assignments.keySet(), ImmutableSet.of(Status.DL))));

        w.println();
        for (Status s : levels) {
            w.print(String.format("(%d) %-10s ", assignments.get(s).size(), s));
        }
        w.println();

        int maxSize = Ordering
            .natural()
            .max(
                Iterables.transform(
                    assignments.keySet(),
                    new Function<Status, Integer>() {
                        public Integer apply(Status s) {
                            return assignments.get(s).size();
                        }
                    }));

        for (int i = 0; i < maxSize; i++) {
            for (Status s : levels) {
                List<Player> ps = Player
                    .byShortName()
                    .sortedCopy(assignments.get(s));

                w.print(String.format("%-15s ", i < ps.size() ? ps.get(i).getShortName() : ""));
            }
            w.println();
        }

        w.println("Total:" + assignments.size() + " (target " + targetMinimum + "-" + targetMaximum + ")");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static Roster create(
        PlayerSource source, Iterable<Player> available) {
        return new Roster(source, available);
    }

    public static Roster create(Team team) {
        return create(team, team);
    }


}
