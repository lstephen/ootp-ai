package com.ljs.ootp.ai.ootp5;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.config.Config;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.ootp5.report.PowerRankingsReport;
import com.ljs.ootp.ai.ootp5.site.LeagueBatting;
import com.ljs.ootp.ai.ootp5.site.LeaguePitching;
import com.ljs.ootp.ai.ootp5.site.LeagueStructureImpl;
import com.ljs.ootp.ai.ootp5.site.PlayerList;
import com.ljs.ootp.ai.ootp5.site.SalaryImpl;
import com.ljs.ootp.ai.ootp5.site.SalarySource;
import com.ljs.ootp.ai.ootp5.site.SingleTeamImpl;
import com.ljs.ootp.ai.ootp5.site.StandingsImpl;
import com.ljs.ootp.ai.ootp5.site.TeamBattingImpl;
import com.ljs.ootp.ai.ootp5.site.TeamPitchingImpl;
import com.ljs.ootp.ai.ootp5.site.TeamRatings;
import com.ljs.ootp.ai.ootp5.site.TopProspects;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.player.PlayerSource;
import com.ljs.ootp.extract.html.rating.Scale;
import com.ljs.ootp.ai.roster.Roster;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.LeagueStructure;
import com.ljs.ootp.ai.site.RecordPredictor;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.site.SiteDefinition;
import com.ljs.ootp.ai.site.Standings;
import com.ljs.ootp.ai.site.Version;
import com.ljs.ootp.ai.stats.BattingStats;
import com.ljs.ootp.ai.stats.PitcherOverall;
import com.ljs.ootp.ai.stats.PitchingStats;
import com.ljs.ootp.ai.stats.TeamStats;
import com.ljs.ootp.extract.html.Page;
import com.ljs.ootp.extract.html.PageFactory;
import com.ljs.ootp.extract.html.loader.DiskCachingLoader;
import com.ljs.ootp.extract.html.loader.PageLoader;
import com.ljs.ootp.extract.html.loader.PageLoaderBuilder;
import java.io.IOException;
import java.util.Set;
import org.joda.time.LocalDate;

/**
 *
 * @author lstephen
 */
public final class SiteImpl implements Site, SalarySource {

  private final SiteDefinition definition;

  private final PlayerSource players;

  private ImmutableSet<PlayerId> futureFas;

  private ImmutableSet<PlayerId> injured;

  private SiteImpl(SiteDefinition def, PlayerSource players) {
    this.definition = def;
    this.players = players;
  }

  @Override
  public SiteDefinition getDefinition() {
    return definition;
  }

  @Override
  public String getName() {
    return definition.getName();
  }

  @Override
  public Version getType() {
    return definition.getType();
  }

  @Override
  public LocalDate getDate() {
    return getLeagueBattingPage().extractDate();
  }

  @Override
  public PitcherOverall getPitcherSelectionMethod() {
    return definition.getPitcherSelectionMethod();
  }

  @Override
  public ImmutableList<Id<Team>> getTeamIds() {
    ImmutableList.Builder<Id<Team>> builder = ImmutableList.builder();

    for (int i = 1; i <= definition.getNumberOfTeams(); i++) {
      builder.add(Id.<Team>valueOf(i));
    }

    return builder.build();
  }

  @Override
  public LeagueStructure getLeagueStructure() {
    return LeagueStructureImpl.create(this);
  }

  @Override
  public Page getPage(String url, Object... args) {
    try {
      PageLoader loader = PageLoaderBuilder
        .create()
        .diskCache(Config.createDefault().getValue("cache.dir").or(DiskCachingLoader.DEFAULT_CACHE_DIR))
        .inMemoryCache()
        .build();



      return PageFactory
        .create(loader)
        .getPage(definition.getSiteRoot(), String.format(url, args));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public Iterable<Player> getDraft() {
    return PlayerList.draft(this).extract();
  }

  @Override
  public Iterable<Player> getFreeAgents() {
    return PlayerList.freeAgents(this).extract();
  }

  @Override
  public BattingStats getLeagueBatting() {
    return getLeagueBattingPage().extractTotal();
  }

  private LeagueBatting getLeagueBattingPage() {
    return new LeagueBatting(this, definition.getLeague());
  }

  @Override
  public PitchingStats getLeaguePitching() {
    return new LeaguePitching(this, definition.getLeague()).extractTotal();
  }

  @Override
  public Iterable<Player> getRuleFiveDraft() {
    return PlayerList.ruleFiveDraft(this).extract();
  }

  @Override
  public Iterable<Player> getSalariedPlayers(Id<Team> id) {
    return getSalary(id).getSalariedPlayers();
  }

  @Override
  public SalaryImpl getSalary() {
    return getSalary(definition.getTeam());
  }

  @Override
  public SalaryImpl getSalary(Id<Team> id) {
    return new SalaryImpl(this, id);
  }

  @Override
  public SalaryImpl getSalary(int teamId) {
    return getSalary(Id.<Team>valueOf(Integer.toString(teamId)));
  }

  public String getSalary(Player p) {
    return getTeamIds()
      .stream()
      .map(team -> getSalary(team).getSalary(p))
      .filter(s -> s != null)
      .findFirst()
      .orElse("");
  }

  @Override
  public Integer getCurrentSalary(Player p) {
    return getTeamIds()
      .stream()
      .map(team -> getSalary(team).getCurrentSalary(p))
      .filter(s -> s != 0)
      .findFirst()
      .orElse(0);
  }

  @Override
  public Optional<Integer> getTeamTopProspectPosition(PlayerId id) {
    java.util.Optional<Integer> jopt = getTeamIds()
      .stream()
      .map(team -> getTopProspects(team).getPosition(id))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findFirst();

    return jopt.isPresent() ? Optional.of(jopt.get()) : Optional.absent();
  }

  @Override
  public SingleTeamImpl getSingleTeam() {
    return getSingleTeam(definition.getTeam());
  }

  @Override
  public SingleTeamImpl getSingleTeam(Id<Team> id) {
    return new SingleTeamImpl(this, id);
  }

  @Override
  public SingleTeamImpl getSingleTeam(int teamId) {
    return getSingleTeam(Id.<Team>valueOf(teamId));
  }

  @Override
  public Standings getStandings() {
    return StandingsImpl.create(this);
  }

  @Override
  public TeamStats<BattingStats> getTeamBatting() {
    return new TeamBattingImpl(this, definition.getTeam()).extract();
  }

  @Override
  public TeamStats<PitchingStats> getTeamPitching() {
    return new TeamPitchingImpl(this, definition.getTeam()).extract();
  }

  public TopProspects getTopProspects(Integer teamId) {
    return getTopProspects(Id.<Team>valueOf(teamId));
  }

  public TopProspects getTopProspects(Id<Team> id) {
    return TopProspects.of(this, id);
  }

  @Override
  public Player getPlayer(PlayerId id) {
    return players.get(id);
  }

  @Override
  public Iterable<Player> getWaiverWire() {
    return PlayerList.waiverWire(this).extract();
  }

  @Override
  public Iterable<Player> getPlayers(Iterable<PlayerId> ids) {
    return Iterables.transform(ids, new Function<PlayerId, Player>() {
      @Override
      public Player apply(PlayerId id) {
        return getPlayer(id);
      }
    });
  }

  @Override
  public boolean isFutureFreeAgent(Player p) {
    if (futureFas == null) {
      futureFas =
        ImmutableSet.copyOf(
            PlayerList.futureFreeAgents(this).extractIds());
    }

    return futureFas.contains(p.getId());
  }

  @Override
  public Predicate<Player> isFutureFreeAgent() {
    return new Predicate<Player>() {
      @Override
      public boolean apply(Player p) {
        return isFutureFreeAgent(p);
      }
    };
  }

  @Override
  public boolean isInjured(Player p) {
    if (injured == null) {
      Set<PlayerId> is = Sets.newHashSet();

      getTeamIds().forEach(id -> Iterables.addAll(is, getSingleTeam(id).getInjuries()));

      injured = ImmutableSet.copyOf(is);
    }

    return injured.contains(p.getId());
  }

  @Override
  public Team extractTeam() {
    return new TeamRatings(this, definition.getTeam()).extractTeam();
  }

  @Override
  public Roster extractRoster() {
    return getSingleTeam().getRoster();
  }

  @Override
  public Scale<?> getAbilityRatingScale() {
    return definition.getAbilityRatingScale();
  }

  @Override
  public Scale<?> getPotentialRatingScale() {
    return definition.getPotentialRatingsScale();
  }

  @Override
  public Printable getPowerRankingsReport(RecordPredictor recordPredictor) {
    return PowerRankingsReport.create(this, recordPredictor);
  }

  public static SiteImpl create(SiteDefinition definition, PlayerSource players) {
    return new SiteImpl(definition, players);
  }

}
