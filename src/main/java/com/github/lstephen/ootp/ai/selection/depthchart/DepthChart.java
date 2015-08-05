package com.github.lstephen.ootp.ai.selection.depthchart;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.ratings.Position;

import java.io.PrintWriter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import org.apache.commons.lang3.StringUtils;

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

    public Iterable<Backup> getBackups(Position position) {
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
                    "%2s| %-15s (D) %-15s |%2s| %s",
                    p.getAbbreviation(),
                    StringUtils.abbreviate(getStarter(p).getShortName(), 15),
                    dr.isPresent() ? StringUtils.abbreviate(dr.get().getShortName(), 15) : "",
                    p.getAbbreviation(),
                    Joiner.on(' ').join(Iterables.transform(getBackups(p), Backup.format()))));
            w.flush();
        }

        w.flush();
    }

    public static class Backup {

        private final Position position;

        private final Player player;

        private final Integer percentage;

        private Backup(Position position, Player player, Integer percentage) {
            this.position = position;
            this.player = player;
            this.percentage = percentage;
        }

        public Player getPlayer() {
          return player;
        }

        public Position getPosition() {
          return position;
        }

        public Integer getPercentage() {
          return percentage;
        }

        public static Function<Backup, String> format() {
            return new Function<Backup, String>() {
                @Override
                public String apply(Backup bu) {
                    Preconditions.checkNotNull(bu);

                    return String.format(
                        "(%2d) %-15s",
                        bu.percentage,
                        bu.player.getShortName());
                }
            };
        }

        public static Backup create(
            Position position, Player player, Integer percentage) {

            return new Backup(position, player, percentage);
        }

    }

}
