package com.github.lstephen.ootp.ai.draft;

import com.github.lstephen.ootp.ai.config.Config;
import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.io.Printables;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.value.JavaAdapter;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author lstephen */
public final class DraftReport implements Printable {

  private final Site site;

  private Predictor predictor;

  private DraftClass current;

  private final Set<DraftClass> historical = Sets.newHashSet();

  private ImmutableList<Player> currentPlayersSorted;

  private List<ImmutableList<Player>> historicalPlayersSorted;

  private DraftReport(Site site, Predictor predictor) {
    this.site = site;
    this.predictor = predictor;
  }

  private void loadDraftClasses() {
    current = DraftClass.create(site.getDraft());

    site.getDraft().forEach(current::addIfNotPresent);

    current.save(site, getDraftClassFile(site.getDate().getYear()));

    for (int i = 1; i < 5; i++) {
      File dcFile = getDraftClassFile(site.getDate().getYear() - i);

      if (dcFile.exists()) {
        historical.add(DraftClass.load(dcFile, site.getDefinition()));
      }
    }

    currentPlayersSorted = byOverall(predictor).immutableSortedCopy(current.getPlayers());

    historicalPlayersSorted =
        historical
            .stream()
            .map(
                dc ->
                    byOverall(new Predictor(dc.getPlayers(), predictor))
                        .immutableSortedCopy(dc.getPlayers()))
            .collect(Collectors.toList());

    List<Player> allPlayers =
        Stream.concat(
                historicalPlayersSorted.stream().flatMap(Collection::stream),
                currentPlayersSorted.stream())
            .collect(Collectors.toList());

    predictor = new Predictor(allPlayers, predictor);
  }

  private File getDraftClassFile(int year) {
    try {
      String historyDirectory =
          Config.createDefault().getValue("history.dir").or("c:/ootp/history");
      return new File(historyDirectory + "/" + site.getName() + year + ".draft.json");
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private RoundValue getRoundValue(int round) {
    Integer not = Iterables.size(site.getTeamIds());

    return getValueOfPicks((round - 1) * not, not);
  }

  private RoundValue getValueOfPicks(int n) {
    return getValueOfPicks(0, n);
  }

  private RoundValue getValueOfPicks(int start, int n) {
    RoundValue rv = RoundValue.create(predictor);

    Iterable<Player> players = FluentIterable.from(currentPlayersSorted).skip(start).limit(n);

    rv.add(players);

    historicalPlayersSorted
        .stream()
        .forEach(
            playersSorted -> {
              Iterable<Player> ps = FluentIterable.from(playersSorted).skip(start).limit(n);

              rv.addHistorical(ps);
            });

    return rv;
  }

  private Ordering<Player> byOverall(Predictor pred) {
    return Ordering.natural()
        .reverse()
        .onResultOf((Player p) -> JavaAdapter.futureValue(p, pred).score());
  }

  public void print(OutputStream out) {
    Printables.print(this).to(out);
  }

  @Override
  public void print(PrintWriter w) {
    w.println();

    Integer not = Iterables.size(site.getTeamIds());

    int n = not / 3;

    RoundValue rv = getValueOfPicks(n);
    if (rv == null || rv.isEmpty()) {
      return;
    }

    int idx = 0;

    rv.print(w, "1E");

    idx += n;

    rv = getValueOfPicks(idx, n);
    rv.print(w, "1M");

    idx += n;
    n = not - idx;

    rv = getValueOfPicks(idx, n);
    rv.print(w, "1L");

    idx = not;
    n = not / 2;

    rv = getValueOfPicks(idx, n);
    rv.print(w, "2E");

    idx += n;
    n = not * 2 - idx;

    rv = getValueOfPicks(idx, n);
    rv.print(w, "2L");

    w.println();

    int round = 1;
    rv = getRoundValue(round);

    while (!rv.isEmpty()) {
      rv.print(w, String.format("%2d", round));

      round++;
      rv = getRoundValue(round);
    }
  }

  public static DraftReport create(Site site, Predictor predictor) {
    DraftReport report = new DraftReport(site, predictor);
    report.loadDraftClasses();
    return report;
  }
}
