package com.github.lstephen.ootp.ai.selection.lineup;

import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

public final class AllLineups implements Iterable<Lineup>, Printable {

  private static final Integer LINEUP_SIZE = 9;
  private static final String LINEUP_ENTRY_FORMAT = "%2s %1s %-15s";

  private final All<Lineup> all;

  private AllLineups(All<Lineup> all) {
    Preconditions.checkNotNull(all);

    this.all = all;
  }

  public Lineup getVsRhp() {
    return all.getVsRhp();
  }

  public Lineup getVsRhpPlusDh() {
    return all.getVsRhpPlusDh();
  }

  public Lineup getVsLhp() {
    return all.getVsLhp();
  }

  public Lineup getVsLhpPlusDh() {
    return all.getVsLhpPlusDh();
  }

  @Override
  public Iterator<Lineup> iterator() {
    return all.iterator();
  }

  public ImmutableSet<Player> getAllPlayers() {
    Set<Player> players = Sets.newHashSet();

    for (Lineup l : this) {
      for (Lineup.Entry e : l) {
        if (e.getPlayer() != null) {
          players.add(e.getPlayer());
        }
      }
    }

    return ImmutableSet.copyOf(players);
  }

  /**
   * Get {@link Player}s that are in all lineups.
   *
   * @return
   */
  public Iterable<Player> getCommonPlayers() {

    Set<Player> result = Sets.newHashSet(getAllPlayers());

    for (Lineup l : this) {
      result = Sets.intersection(result, l.playerSet());
    }

    return result;
  }

  @Override
  public void print(PrintWriter w) {
    w.println();
    w.println(
        String.format("   %-21s %-21s %-21s %-21s", "vsRHP", "vsRHP+DH", "vsLHP", "vsLHP+DH"));

    for (int i = 0; i < LINEUP_SIZE; i++) {
      w.println(
          String.format(
              "%d. %-21s %-21s %-21s %-21s",
              Integer.valueOf(i + 1),
              all.getVsRhp().getEntry(i).format(LINEUP_ENTRY_FORMAT),
              all.getVsRhpPlusDh().getEntry(i).format(LINEUP_ENTRY_FORMAT),
              all.getVsLhp().getEntry(i).format(LINEUP_ENTRY_FORMAT),
              all.getVsLhpPlusDh().getEntry(i).format(LINEUP_ENTRY_FORMAT)));
    }
  }

  public double getHomeRunsPerPlateAppearance(Predictor predictor) {
    return (all.getVsRhp().getHomeRunsPerPlateAppearance(predictor, Lineup.VsHand.VS_RHP)
            + all.getVsRhpPlusDh().getHomeRunsPerPlateAppearance(predictor, Lineup.VsHand.VS_RHP)
            + all.getVsLhp().getHomeRunsPerPlateAppearance(predictor, Lineup.VsHand.VS_LHP)
            + all.getVsLhpPlusDh().getHomeRunsPerPlateAppearance(predictor, Lineup.VsHand.VS_LHP))
        / 4.0;
  }

  public static AllLineups create(All<Lineup> all) {
    return new AllLineups(all);
  }
}
