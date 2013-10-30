package com.ljs.scratch.ootp.selection.lineup;

import com.google.common.collect.ImmutableMap;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.ratings.DefensiveRatings;
import com.ljs.scratch.ootp.ratings.Position;
import static com.ljs.scratch.ootp.ratings.Position.CATCHER;
import static com.ljs.scratch.ootp.ratings.Position.CENTER_FIELD;
import static com.ljs.scratch.ootp.ratings.Position.LEFT_FIELD;
import static com.ljs.scratch.ootp.ratings.Position.RIGHT_FIELD;
import static com.ljs.scratch.ootp.ratings.Position.SECOND_BASE;
import static com.ljs.scratch.ootp.ratings.Position.SHORTSTOP;
import static com.ljs.scratch.ootp.ratings.Position.THIRD_BASE;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public final class Defense {

    private final ImmutableMap<Player, Position> defense;

    private Defense(Map<Player, Position> defense) {
        this.defense = ImmutableMap.copyOf(defense);
    }

    public boolean contains(Player p) {
        return defense.containsKey(p);
    }

    public Position getPosition(Player p) {
        return defense.get(p);
    }

    public Player getPlayer(Position pos) {
        for (Map.Entry<Player, Position> entry : defense.entrySet()) {
            if (entry.getValue().equals(pos)) {
                return entry.getKey();
            }
        }

        throw new IllegalStateException();
    }

    public static Defense create(Map<Player, Position> defense) {
        return new Defense(defense);
    }

    public Double score() {
        double total = 0.0;

        for (Map.Entry<Player, Position> entry : defense.entrySet()) {
            Player ply = entry.getKey();
            Position pos = entry.getValue();

            DefensiveRatings r = ply.getDefensiveRatings();

            total += getPositionFactor(pos) * r.getPositionScore(pos);
        }

        return total;
    }

    public static Double getPositionFactor(Position p) {
        switch (p) {
            case CATCHER:
            case SHORTSTOP:
                return 5.0;

            case SECOND_BASE:
            case THIRD_BASE:
                return 4.0;

            case CENTER_FIELD:
                return 3.0;

            case LEFT_FIELD:
            case RIGHT_FIELD:
                return 2.0;

            default:
                return 1.0;
        }
    }

}
