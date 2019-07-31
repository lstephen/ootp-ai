package com.github.lstephen.ootp.ai.value;

import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.selection.Selections;
import com.github.lstephen.ootp.ai.site.Site;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SkillByAge implements Printable {

  private static SkillByAge instance;

  private final PlayerValue value;

  private final ByAge hitting = new ByAge();
  private final ByAge pitching = new ByAge();

  private SkillByAge(PlayerValue value) {
    this.value = value;
  }

  private void add(Player p) {
    add(Arrays.asList(p));
  }

  private void add(Iterable<Player> ps) {
    Selections.onlyHitters(ps)
        .forEach(p -> hitting.add(p.getAge(), value.getNowValueNoDefense().apply(p)));
    Selections.onlyPitchers(ps)
        .forEach(p -> pitching.add(p.getAge(), value.getNowValueNoDefense().apply(p)));
  }

  public ByAge getHitting() {
    return hitting;
  }

  public ByAge getPitching() {
    return pitching;
  }

  public void print(PrintWriter w) {
    w.println("Hitting");
    hitting.print(w);
    w.println();
    w.println("Pitching");
    pitching.print(w);
    w.println();
  }

  public static class ByAge implements Printable {

    private final Multimap<Integer, Integer> values = LinkedHashMultimap.create();

    public void add(Integer age, Integer value) {
      values.put(age, value);
    }

    private OptionalDouble getAverage(int age) {
      return values.get(age).stream().mapToInt(Integer::intValue).average();
    }

    public OptionalDouble getThreeYearAverage(int age) {
      OptionalDouble avg =
          Stream.of(-1, 0, 1)
              .flatMap(y -> values.get(age + y).stream())
              .mapToInt(Integer::intValue)
              .average();

      OptionalDouble fiveYearAvg =
          Stream.of(-2, -1, 0, 1, 2)
              .flatMap(y -> values.get(age + y).stream())
              .mapToInt(Integer::intValue)
              .average();

      return avg.isPresent() ? avg : fiveYearAvg;
    }

    public void print(PrintWriter w) {
      agesStream().forEach(a -> w.format("%3d", a));

      w.println();

      agesStream()
          .mapToObj(this::getAverage)
          .forEach(
              avg ->
                  w.format(
                      "%3s",
                      avg.isPresent() ? String.format("%3d", Math.round(avg.getAsDouble())) : ""));

      w.println();

      agesStream()
          .mapToObj(this::getThreeYearAverage)
          .forEach(
              avg ->
                  w.format(
                      "%3s",
                      avg.isPresent() ? String.format("%3d", Math.round(avg.getAsDouble())) : ""));

      w.println();
    }

    private static IntStream agesStream() {
      return IntStream.rangeClosed(15, 45);
    }
  }

  public static void init(Site site, Predictor predictor) {
    instance = new SkillByAge(new PlayerValue(predictor));

    site.getAllPlayers()
        .stream()
        .filter(
            p ->
                ReplacementLevels$.MODULE$
                            .getForIdeal(predictor)
                            .getTrendingTowardsAverage(JavaAdapter.futureAbility(p, predictor))
                        > 0.0
                    || ReplacementLevels$.MODULE$
                            .getForIdeal(predictor)
                            .getTrendingTowardsAverage(JavaAdapter.nowAbility(p, predictor))
                        > 0.0)
        .forEach(instance::add);
  }

  public static SkillByAge getInstance() {
    return instance;
  }
}
