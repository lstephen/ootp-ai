package com.github.lstephen.ootp.ai.report;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.ratings.Position;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.roster.Changes;
import com.github.lstephen.ootp.ai.selection.Mode;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.value.JavaAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.Set;

/** @author lstephen */
public final class FreeAgents {

  private final Site site;

  private final Predictor predictor;

  private final Set<Player> skipped = Sets.newHashSet();

  private final ImmutableSet<Player> fas;

  private FreeAgents(Site site, Predictor predictor, Iterable<Player> fas) {
    this.site = site;
    this.predictor = predictor;
    this.fas = ImmutableSet.copyOf(Iterables.filter(fas, Predicates.notNull()));
  }

  public Iterable<Player> all() {
    return fas;
  }

  public void skip(Player p) {
    skipped.add(p);
  }

  public void skip(Iterable<Player> ps) {
    for (Player p : ps) {
      skip(p);
    }
  }

  public Optional<Player> getPlayerToRelease(Iterable<Player> roster) {
    for (Player r : byValue().sortedCopy(roster)) {
      return Optional.of(r);
    }

    return Optional.absent();
  }

  public Iterable<Player> getTopTargets(Mode mode) {
    Set<Player> targets = Sets.newHashSet();

    for (Position pos : Iterables.concat(Position.hitting(), Position.pitching())) {
      targets.add(
          Ordering.natural()
              .onResultOf((Player p) -> JavaAdapter.overallValue(p, pos, predictor).score())
              .max(Iterables.filter(fas, p -> !skipPlayer(p))));
    }

    return targets;
  }

  private Ordering<Player> byValue() {
    return Ordering.natural()
        .onResultOf((Player p) -> JavaAdapter.overallValue(p, predictor).score())
        .compound(Player.byTieBreak());
  }

  public Boolean skipPlayer(Player fa) {
    if (skipped.contains(fa)) {
      return true;
    }
    if (fa.getShortName().contains("fake ") || fa.getShortName().contains("Draft Pik")) {
      return true;
    }
    if (fa.getTeam() != null && fa.getTeam().contains("*CEI*")) {
      return true;
    }
    return false;
  }

  public static FreeAgents create(Site site, Changes changes, Predictor predictor) {
    FreeAgents fas = create(site, predictor, site.getFreeAgents());

    fas.skip(changes.get(Changes.ChangeType.DONT_ACQUIRE));

    return fas;
  }

  public static FreeAgents create(Site site, Predictor predictor, Iterable<Player> fas) {
    return new FreeAgents(site, predictor, fas);
  }
}
