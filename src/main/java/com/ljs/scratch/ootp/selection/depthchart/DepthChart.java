package com.ljs.scratch.ootp.selection.depthchart;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.ratings.Position;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public class DepthChart {

    private final Map<Position, Player> starters = Maps.newHashMap();

    private final Map<Position, Player> defenders = Maps.newHashMap();

    private final Map<Position, Player> backups = Maps.newHashMap();

    private final Map<Position, Integer> backupPercentage = Maps.newHashMap();

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

    public void setBackup(Position position, Player player, Number pct) {
        backups.put(position, player);
        backupPercentage.put(position, pct.intValue());
    }

    private Player getBackup(Position position) {
        return backups.get(position);
    }

    private Integer getBackupPercentage(Position position) {
        return backupPercentage.get(position);
    }

    public void print(PrintWriter w) {
        for (Position p : Ordering
            .explicit(Arrays.asList(Position.values()))
            .sortedCopy(starters.keySet())) {

            Optional<Player> dr = getDefensiveReplacement(p);

            w.println(
                String.format(
                    "%2s: %-15s DR: %-15s BU: %-15s (%2d%%)",
                    p.getAbbreviation(),
                    StringUtils.abbreviate(getStarter(p).getShortName(), 15),
                    dr.isPresent() ? StringUtils.abbreviate(dr.get().getShortName(), 15) : "",
                    StringUtils.abbreviate(getBackup(p).getShortName(), 15),
                    getBackupPercentage(p)
                ));
            w.flush();
        }

        w.flush();
    }

}
