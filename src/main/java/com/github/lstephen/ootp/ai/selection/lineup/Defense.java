package com.github.lstephen.ootp.ai.selection.lineup;

import static com.github.lstephen.ootp.ai.player.ratings.Position.CATCHER;
import static com.github.lstephen.ootp.ai.player.ratings.Position.CENTER_FIELD;
import static com.github.lstephen.ootp.ai.player.ratings.Position.LEFT_FIELD;
import static com.github.lstephen.ootp.ai.player.ratings.Position.RIGHT_FIELD;
import static com.github.lstephen.ootp.ai.player.ratings.Position.SECOND_BASE;
import static com.github.lstephen.ootp.ai.player.ratings.Position.SHORTSTOP;
import static com.github.lstephen.ootp.ai.player.ratings.Position.THIRD_BASE;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.ratings.Position;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** @author lstephen */
public final class Defense {

  private final ImmutableMap<Player, Position> defense;

  private Defense(Map<Player, Position> defense) {
    this.defense = ImmutableMap.copyOf(defense);
  }

  public Boolean isValid() {
    return defense.values().size() == 8
        && defense
            .values()
            .containsAll(
                ImmutableSet.of(
                    Position.CATCHER,
                    Position.FIRST_BASE,
                    Position.SECOND_BASE,
                    Position.THIRD_BASE,
                    Position.SHORTSTOP,
                    Position.LEFT_FIELD,
                    Position.CENTER_FIELD,
                    Position.RIGHT_FIELD));
  }

  public Defense swap(Player lhs, Player rhs) {
    Map<Player, Position> d = Maps.newHashMap(defense);

    if (!contains(lhs) && !contains(rhs)) {
      return this;
    } else if (!contains(lhs)) {
      d.put(lhs, getPosition(rhs));
      d.remove(rhs);
    } else if (!contains(rhs)) {
      d.put(rhs, getPosition(lhs));
      d.remove(lhs);
    } else {
      d.put(lhs, getPosition(rhs));
      d.put(rhs, getPosition(lhs));
    }

    return new Defense(d);
  }

  public boolean contains(Player p) {
    return defense.containsKey(p);
  }

  public ImmutableSet<Player> players() {
    return defense.keySet();
  }

  public Position getPosition(Player p) {
    Preconditions.checkState(contains(p), "Does not contain player: %s\n%s", p, this);
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

      total += new PlayerDefenseScore(ply, pos).toDouble();
    }

    return total;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();

    for (Position p : Ordering.natural().sortedCopy(defense.values())) {
      str.append(p.getAbbreviation()).append("-").append(getPlayer(p).getShortName()).append("/");
    }

    return str.toString();
  }

  public static Integer getPositionFactor(Position p) {
    switch (p) {
      case CATCHER:
      case SHORTSTOP:
        return 4;

      case SECOND_BASE:
      case THIRD_BASE:
      case CENTER_FIELD:
        return 3;

      case LEFT_FIELD:
      case RIGHT_FIELD:
        return 2;

      default:
        return 1;
    }
  }

  public static Ordering<Defense> byScore() {
    return Ordering.natural().onResultOf(Defense::score);
  }

  public static Ordering<Defense> byAge() {
    return Ordering.natural()
        .onResultOf(
            (d) ->
                d.defense
                    .entrySet()
                    .stream()
                    .map((e) -> e.getKey().getAge() * getPositionFactor(e.getValue()))
                    .reduce(0, Integer::sum));
  }

  public static Ordering<Defense> byRawRange() {
    return Ordering.natural()
        .onResultOf(
            (d) ->
                d.defense
                    .entrySet()
                    .stream()
                    .map(
                        (e) ->
                            getPositionFactor(e.getValue())
                                * e.getKey().getDefensiveRatings().getPositionRating(e.getValue()))
                    .reduce(0.0, Double::sum));
  }

  public static Supplier<Defense> randomGenerator(final Iterable<Player> players) {
    return new Supplier<Defense>() {
      @Override
      public Defense get() {
        List<Player> ps = Lists.newArrayList(players);
        Collections.shuffle(ps);

        Map<Player, Position> defense = Maps.newHashMap();

        List<Position> pos =
            Lists.newArrayList(
                Position.CATCHER,
                Position.FIRST_BASE,
                Position.SECOND_BASE,
                Position.THIRD_BASE,
                Position.SHORTSTOP,
                Position.LEFT_FIELD,
                Position.CENTER_FIELD,
                Position.RIGHT_FIELD);

        while (!pos.isEmpty() && !ps.isEmpty()) {
          defense.put(ps.remove(0), pos.remove(0));
        }

        return Defense.create(defense);
      }
    };
  }
}
