package com.github.lstephen.ootp.ai.ootp5.site;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.PlayerId;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.extract.html.Page;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** @author lstephen */
public final class PlayerList {

  private static final Pattern PLAYER_ID_REGEX = Pattern.compile("p\\d+\\.html");

  private final Site site;

  private final Page page;

  private boolean filterNulls = false;

  private PlayerList(Site site, Page page) {
    this.site = site;
    this.page = page;
  }

  private PlayerList(Site site, String url) {
    this(site, site.getPage(url));
  }

  public PlayerList filterNulls() {
    this.filterNulls = true;
    return this;
  }

  public Collection<Player> extract() {
    ArrayList<Player> toReturn = new ArrayList<>();
    Iterables.addAll(toReturn, Iterables.filter(site.getPlayers(extractIds()), c -> filterNulls ? c != null : true));
    return toReturn;
  }

  public Set<PlayerId> extractIds() {
    return page.load()
        .select("a")
        .stream()
        .map(e -> e.attr("href"))
        .distinct()
        .filter(href -> PLAYER_ID_REGEX.matcher(href).matches())
        .map(href -> href.replaceAll(".html", ""))
        .map(PlayerId::new)
        .collect(Collectors.toSet());
  }

  public static PlayerList allPlayers(Site site) {
    return new PlayerList(site, "players.html");
  }

  public static PlayerList freeAgents(Site site) {
    return new PlayerList(site, "agents.html").filterNulls();
  }

  public static PlayerList futureFreeAgents(Site site) {
    return new PlayerList(site, "pagents.html");
  }

  public static PlayerList waiverWire(Site site) {
    return new PlayerList(site, "waiver.html");
  }

  public static PlayerList draft(Site site) {
    return new PlayerList(site, "rookies.html");
  }

  public static PlayerList ruleFiveDraft(Site site) {
    return new PlayerList(site, "rule5.html");
  }

  public static PlayerList from(Site site, Page page) {
    return new PlayerList(site, page);
  }
}
