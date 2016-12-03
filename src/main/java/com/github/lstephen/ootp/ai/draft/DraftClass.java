package com.github.lstephen.ootp.ai.draft;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.lstephen.ootp.ai.config.Config;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.site.SiteDefinition;
import com.github.lstephen.scratch.util.Jackson;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author lstephen */
public final class DraftClass {

  private final Set<Player> players = Sets.newHashSet();

  private DraftClass() {}

  @JsonCreator
  private DraftClass(@JsonProperty("players") Set<Player> players) {
    Iterables.addAll(this.players, players);
  }

  public void add(Player p) {
    this.players.add(p);
  }

  public void add(Collection<Player> ps) {
    ps.stream().forEach(this::add);
  }

  public Collection<Player> getPlayers() {
    return playersStream().collect(Collectors.toSet());
  }

  private Stream<Player> playersStream() {
    return players.stream().filter(p -> p != null);
  }

  public void save(Site site, File f) {
    try {
      Jackson.getMapper(site).writeValue(f, this);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public static DraftClass create(Collection<Player> ps) {
    DraftClass dc = new DraftClass();
    dc.add(ps);
    return dc;
  }

  public static DraftClass load(File f, SiteDefinition site) {
    if (f.exists()) {
      try {
        DraftClass dc = Jackson.getMapper(site.getSite()).readValue(f, DraftClass.class);

        dc.playersStream().forEach(p -> p.setRatingsDefinition(site));

        return dc;
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    } else {
      return create(ImmutableSet.<Player>of());
    }
  }

  public static DraftClass load(Site site, int year) {
    return load(getDraftClassFile(site, year), site.getDefinition());
  }

  public static File getDraftClassFile(Site site, int year) {
    try {
      String historyDirectory =
          Config.createDefault().getValue("history.dir").or("c:/ootp/history");
      return new File(historyDirectory + "/" + site.getName() + year + ".draft.json");
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
