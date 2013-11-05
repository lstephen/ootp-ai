package com.ljs.scratch.ootp.selection.depthchart;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.ratings.Position;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public class DepthChart {

    private final Map<Position, Player> starters = Maps.newHashMap();

    private final Map<Position, Player> defenders = Maps.newHashMap();

    private final List<Backup> backups = Lists.newArrayList();

    public void setStarter(Position position, Player player) {
        starters.put(position, player);
    }

    public Player getStarter(Position position) {
        return starters.get(position);
    }

    public void setDefensiveReplacement(Position position, Player player) {
        defenders.put(position, player);
    }

    public Optional<Player> getDefensiveReplacement(Position position) {
        return Optional.fromNullable(defenders.get(position));
    }

    public void addBackup(Position position, Player player, Number pct) {
        backups.add(Backup.create(position, player, pct.intValue()));
    }

    private Iterable<Backup> getBackups(Position position) {
        List<Backup> result = Lists.newArrayList();

        for (Backup bu : backups) {
            if (bu.position == position) {
                result.add(bu);
            }
        }

        return result;
    }

    public void print(PrintWriter w) {
        for (Position p : Ordering
            .explicit(Arrays.asList(Position.values()))
            .sortedCopy(starters.keySet())) {

            Optional<Player> dr = getDefensiveReplacement(p);

            w.println(
                String.format(
                    "%2s: %-15s DR: %-15s BU: %s",
                    p.getAbbreviation(),
                    StringUtils.abbreviate(getStarter(p).getShortName(), 15),
                    dr.isPresent() ? StringUtils.abbreviate(dr.get().getShortName(), 15) : "",
                    Joiner.on(' ').join(Iterables.transform(getBackups(p), Backup.format()))));
            w.flush();
        }

        w.flush();
    }

    private static class Backup {

        private final Position position;

        private final Player player;

        private final BackupPlayingTime percentage;

        private Backup(Position position, Player player, Integer percentage) {
            this.position = position;
            this.player = player;
            this.percentage = BackupPlayingTime.roundFrom(percentage);
        }

        public static Function<Backup, String> format() {
            return new Function<Backup, String>() {
                @Override
                public String apply(Backup bu) {
                    Assertions.assertThat(bu).isNotNull();

                    return String.format(
                        "(%s) %-15s",
                        bu.percentage.format(),
                        StringUtils.abbreviate(bu.player.getShortName(), 15));
                }
            };
        }

        public static Backup create(
            Position position, Player player, Integer percentage) {

            return new Backup(position, player, percentage);
        }

    }

}
