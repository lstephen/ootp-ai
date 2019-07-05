package com.github.lstephen.ootp.ai;

import com.github.lstephen.ootp.ai.config.Config;
import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.draft.DraftReport;
import com.github.lstephen.ootp.ai.io.Printables;
import com.github.lstephen.ootp.ai.ootp5.report.SpringTraining;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.ratings.PlayerRatings;
import com.github.lstephen.ootp.ai.regression.BattingPrediction;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.regression.Predictor$;
import com.github.lstephen.ootp.ai.report.DevelopmentReport;
import com.github.lstephen.ootp.ai.report.FreeAgents;
import com.github.lstephen.ootp.ai.report.GenericValueReport;
import com.github.lstephen.ootp.ai.report.HistorialDevelopmentReport;
import com.github.lstephen.ootp.ai.report.HittingSelectionReport;
import com.github.lstephen.ootp.ai.report.LeagueBattingReport;
import com.github.lstephen.ootp.ai.report.PitchingStrategyReport;
import com.github.lstephen.ootp.ai.report.PotentialVsActualReport;
import com.github.lstephen.ootp.ai.report.SalaryReport;
import com.github.lstephen.ootp.ai.report.TeamPositionReport;
import com.github.lstephen.ootp.ai.report.TeamReport;
import com.github.lstephen.ootp.ai.roster.Changes;
import com.github.lstephen.ootp.ai.roster.Changes.ChangeType;
import com.github.lstephen.ootp.ai.roster.FourtyManRoster;
import com.github.lstephen.ootp.ai.roster.Moves;
import com.github.lstephen.ootp.ai.roster.Roster;
import com.github.lstephen.ootp.ai.roster.Roster.Status;
import com.github.lstephen.ootp.ai.roster.RosterSelection;
import com.github.lstephen.ootp.ai.roster.Team;
import com.github.lstephen.ootp.ai.selection.BestStartersSelection;
import com.github.lstephen.ootp.ai.selection.Mode;
import com.github.lstephen.ootp.ai.selection.Selections;
import com.github.lstephen.ootp.ai.selection.bench.Bench;
import com.github.lstephen.ootp.ai.selection.depthchart.AllDepthCharts;
import com.github.lstephen.ootp.ai.selection.depthchart.DepthChartSelection;
import com.github.lstephen.ootp.ai.selection.lineup.AllLineups;
import com.github.lstephen.ootp.ai.selection.lineup.LineupSelection;
import com.github.lstephen.ootp.ai.selection.rotation.Rotation;
import com.github.lstephen.ootp.ai.selection.rotation.RotationSelection;
import com.github.lstephen.ootp.ai.site.SingleTeam;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.site.SiteDefinition;
import com.github.lstephen.ootp.ai.site.SiteHolder;
import com.github.lstephen.ootp.ai.site.impl.SiteDefinitionFactory;
import com.github.lstephen.ootp.ai.stats.BaseRunsCoefficients;
import com.github.lstephen.ootp.ai.stats.SplitPercentages;
import com.github.lstephen.ootp.ai.stats.SplitPercentagesHolder;
import com.github.lstephen.ootp.ai.stats.SplitStats;
import com.github.lstephen.ootp.ai.value.JavaAdapter;
import com.github.lstephen.ootp.ai.value.PlayerValue;
import com.github.lstephen.ootp.ai.value.ReplacementLevels$;
import com.github.lstephen.ootp.extract.html.Page;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.joda.time.DateTimeConstants;

/** @author lstephen */
public class Main {

  private static final Logger LOG = Logger.getLogger(Main.class.getName());

  private static final SiteDefinition TWML =
      SiteDefinitionFactory.ootp6(
          "TWML",
          "http://www.darowski.com/twml/OOTP6Reports/",
          Id.<Team>valueOf(14),
          "Splendid Splinter",
          16);

  private static final SiteDefinition CBL =
      SiteDefinitionFactory.ootp5(
          "CBL", "http://www.thecblonline.com/files/", Id.<Team>valueOf(20), "National", 20);

  private static final SiteDefinition HFTC =
      SiteDefinitionFactory.ootp5(
          "HFTC", "http://www.hitforthecycle.com/hftc-ootp/", Id.<Team>valueOf(8), "American", 32);

  private static final SiteDefinition OLD_BTH_CHC =
      SiteDefinitionFactory.ootp6(
          "BTH",
          "http://bthbaseball.allsimbaseball10.com/game/oldbth/lgreports/",
          Id.<Team>valueOf(20),
          "National",
          30);

  private static final SiteDefinition OLD_BTH_NYY =
      SiteDefinitionFactory.ootp6(
          "OLD_BTH_NYY",
          "http://bthbaseball.allsimbaseball10.com/game/oldbth/lgreports/",
          Id.<Team>valueOf(3),
          "American",
          30);

  private static final SiteDefinition BTHUSTLE =
      SiteDefinitionFactory.ootp6(
          "BTHUSTLE",
          "http://bthbaseball.allsimbaseball10.com/game/lgreports/",
          Id.<Team>valueOf(14),
          "National",
          16);

  private static final SiteDefinition SAVOY =
      SiteDefinitionFactory.ootp5(
          "SAVOY", "http://www.thecblonline.com/savoy/", Id.<Team>valueOf(26), "UBA", 26);

  private static final SiteDefinition LBB =
      SiteDefinitionFactory.ootp5(
          "LBB",
          "http://longballer2.x10host.com/LBB.lg/leaguesite/",
          Id.<Team>valueOf(3),
          "AL",
          20);

  private static final SiteDefinition GABL =
      SiteDefinitionFactory.ootp5(
          "GABL",
          "http://www.goldenageofbaseball.com/commish/Leaguesite/",
          Id.<Team>valueOf(22),
          "National",
          30);

  private static final SiteDefinition TFMS =
      SiteDefinitionFactory.ootp5("TFMS", "tfms5-2004/", Id.<Team>valueOf(3), "League 2", 16);

  private static final ImmutableMap<String, SiteDefinition> SITES =
      ImmutableMap.<String, SiteDefinition>builder()
          .put("TWML", TWML)
          .put("CBL", CBL)
          .put("HFTC", HFTC)
          .put("OLD_BTH_CHC", OLD_BTH_CHC)
          .put("OLD_BTH_NYY", OLD_BTH_NYY)
          .put("BTHUSTLE", BTHUSTLE)
          .put("LBB", LBB)
          .put("SAVOY", SAVOY)
          .put("GABL", GABL)
          .build();

  private static ImmutableSet<SiteDefinition> LOOK_TO_NEXT_SEASON = ImmutableSet.of();

  private static ImmutableSet<SiteDefinition> INAUGURAL_DRAFT = ImmutableSet.of();

  public static void main(String[] args) throws IOException {
    new Main().run();
  }

  public void run() throws IOException {
    String site = System.getenv("OOTPAI_SITE");

    if (site == null) {
      throw new IllegalStateException("OOTPAI_SITE is required");
    }

    run(SITES.get(site));
  }

  private void run(SiteDefinition def) throws IOException {
    Preconditions.checkNotNull(def, "SiteDefinition not found");

    File outputDirectory = new File(Config.createDefault().getValue("output.dir").or("c:/ootp"));

    try (FileOutputStream out =
        new FileOutputStream(new File(outputDirectory, def.getName() + ".txt"), false)) {
      run(def, out);
    }
  }

  private void run(SiteDefinition def, OutputStream out) throws IOException {
    LOG.log(Level.INFO, "Running for {0}...", def.getName());

    String clearCache = System.getenv("OOTPAI_CLEAR_CACHE");

    if ("true".equals(clearCache)) {
      LOG.log(Level.INFO, "Clearing cache...");
      def.getSite().clearCache();
    }

    Boolean isLookToNextSeason = Boolean.FALSE;

    if (LOOK_TO_NEXT_SEASON.contains(def)) {
      isLookToNextSeason = Boolean.TRUE;
    }

    final Site site = def.getSite();

    SiteHolder.set(site);

    LOG.log(Level.INFO, "Warming team cache...");
    site.getTeamIds()
        .parallelStream()
        .flatMap(
            id ->
                Stream.of("team%s.html", "team%spr.html", "team%ssa.html")
                    .map(s -> String.format(s, id.get())))
        .map(site::getPage)
        .forEach(Page::load);

    LOG.log(Level.INFO, "Warming player cache...");
    site.getAllPlayers();

    BaseRunsCoefficients.calculate(site);
    Printables.print(BaseRunsCoefficients.report()).to(out);
    Printables.print(LeagueBattingReport.create(site)).to(out);

    LOG.log(Level.INFO, "Extracting current roster and team...");

    Roster oldRoster = site.extractRoster();
    Context$.MODULE$.oldRoster_$eq(oldRoster);
    Team team = site.extractTeam();

    LOG.log(Level.INFO, "Calculating SplitPercentages...");

    SplitPercentages pcts = SplitPercentages.create(site);
    SplitPercentagesHolder.set(pcts);
    SplitStats.setPercentages(pcts);
    PlayerRatings.setPercentages(pcts);
    BestStartersSelection.setPercentages(pcts);
    Bench.setPercentages(pcts);
    pcts.print(out);

    LOG.info("Training regressions and running predictions...");
    final Predictor predictor = Predictor$.MODULE$.train(site);
    Printables.print(predictor.correlationReport()).to(out);

    Context$.MODULE$.currentPredictor_$eq(predictor);

    LOG.info("Loading manual changes...");
    Changes changes = Changes.load(site);

    team.processManualChanges(changes);

    boolean isPreseason = site.getDate().getMonthOfYear() < DateTimeConstants.APRIL;
    boolean isExpandedRosters = site.getDate().getMonthOfYear() == DateTimeConstants.SEPTEMBER;
    boolean isPlayoffs =
        site.getDate().getMonthOfYear() >= DateTimeConstants.OCTOBER
            || "true".equals(System.getenv("OOTPAI_PLAYOFFS"));

    int month = site.getDate().getMonthOfYear();

    Mode mode = Mode.REGULAR_SEASON;

    if (isPreseason) {
      mode = Mode.PRESEASON;
    } else if (isExpandedRosters) {
      mode = Mode.EXPANDED;
    } else if (isPlayoffs) {
      mode = Mode.PLAYOFFS;
    }

    LOG.info("Loading FAS...");
    FreeAgents fas = FreeAgents.create(site, changes, predictor);

    RosterSelection selection = new RosterSelection(team, predictor);

    selection.setPrevious(oldRoster);

    LOG.log(Level.INFO, "Selecting ideal roster...");

    Context$.MODULE$.idealRoster_$eq(selection.select(Mode.IDEAL));

    LOG.info("Calculating top FA targets...");
    Iterable<Player> topFaTargets = fas.getTopTargets(mode);

    Integer minRosterSize = 70;
    Integer maxRosterSize = 75;

    ImmutableSet<Player> futureFas = ImmutableSet.of();

    if (isLookToNextSeason) {
      futureFas = ImmutableSet.copyOf(Iterables.filter(team, site.isFutureFreeAgent()));

      System.out.print("Removing (FA):");
      for (Player p : Player.byShortName().immutableSortedCopy(futureFas)) {
        System.out.print(p.getShortName() + "/");
      }
      System.out.println();

      team.remove(futureFas);
    }

    LOG.log(Level.INFO, "Selecting new rosters...");

    Stopwatch sw = Stopwatch.createStarted();

    Mode selectionMode = mode;

    if (isPlayoffs) {
      selectionMode = Mode.PLAYOFFS;
    }

    Roster newRoster = selection.select(selectionMode, changes);

    newRoster.setTargetMinimum(minRosterSize);
    newRoster.setTargetMaximum(maxRosterSize);

    Context$.MODULE$.newRoster_$eq(newRoster);

    sw.stop();

    LOG.log(Level.INFO, "Roster selection time: " + sw);

    Printables.print(new HittingSelectionReport(newRoster, predictor, site.getTeamBatting()))
        .to(out);
    selection.printPitchingSelectionTable(out, newRoster, site.getTeamPitching());

    Printables.print(newRoster).to(out);

    LOG.info("Calculating roster changes...");

    Printables.print(newRoster.getChangesFrom(oldRoster)).to(out);

    LOG.log(Level.INFO, "Choosing rotation...");

    Rotation rotation =
        RotationSelection.forMode(selectionMode, predictor)
            .useAllAvailable()
            .selectRotation(
                ImmutableSet.<Player>of(),
                Selections.onlyPitchers(newRoster.getPlayers(Status.ML)));

    Printables.print(rotation).to(out);
    Printables.print(new PitchingStrategyReport(rotation, predictor)).to(out);

    LOG.log(Level.INFO, "Choosing lineups...");

    BattingPrediction pitcherHitting = rotation.getPitcherHitting(predictor);

    LOG.log(
        Level.INFO,
        "Pitcher Hitting: vsL:"
            + pitcherHitting.vsLeft().getSlashLine()
            + " vsR:"
            + pitcherHitting.vsRight().getSlashLine());

    AllLineups lineups =
        new LineupSelection(predictor)
            .select(Selections.onlyHitters(newRoster.getPlayers(Status.ML)));

    LOG.log(Level.INFO, "Choosing Depth Charts...");

    AllDepthCharts depthCharts =
        new DepthChartSelection(predictor).select(lineups, newRoster.getPlayers(Status.ML));

    Printables.print(depthCharts).to(out);

    Printables.print(lineups).to(out);

    LOG.info("Salary report...");
    SalaryReport salary = new SalaryReport(team, site.getSalary(), site.getFinancials(), predictor);

    final GenericValueReport generic = new GenericValueReport(team, predictor, salary);
    generic.setReverse(false);

    LOG.log(Level.INFO, "Strategy...");
    generic.setTitle("Bunt for Hit");
    generic.setPlayers(
        newRoster
            .getPlayers(Status.ML)
            .stream()
            .filter(p -> p.getBuntForHitRating().normalize().get() > 80)
            .collect(Collectors.toSet()));
    generic.print(out);

    double stealingBreakEven = 0.590 + 3.33 * lineups.getHomeRunsPerPlateAppearance(predictor);
    generic.setTitle("Stealing @ " + String.format("%.1f", stealingBreakEven * 100.0));
    generic.setPlayers(
        newRoster
            .getPlayers(Status.ML)
            .stream()
            .filter(p -> p.getStealingRating().normalize().get() > stealingBreakEven * 100.0)
            .collect(Collectors.toSet()));
    generic.print(out);

    if (def.getName().equals("BTHUSTLE")) {
      LOG.info("40 man roster reports...");

      FourtyManRoster fourtyMan = new FourtyManRoster(team, newRoster, predictor);
      fourtyMan.setChanges(changes);

      Printables.print(fourtyMan).to(out);

      generic.setTitle("+40");
      generic.setPlayers(
          ImmutableSet.<Player>builder()
              .addAll(fourtyMan.getPlayersToAdd())
              .addAll(
                  FluentIterable.from(changes.get(ChangeType.FOURTY_MAN))
                      .filter(Predicates.in(newRoster.getAllPlayers())))
              .build());
      generic.print(out);

      generic.setTitle("-40");
      generic.setPlayers(fourtyMan.getPlayersToRemove());
      generic.print(out);

      generic.setTitle("Waive");
      generic.setPlayers(isExpandedRosters ? ImmutableSet.of() : fourtyMan.getPlayersToWaive());
      generic.print(out);

      LOG.info("Waviers report...");
      generic.setTitle("Waivers");
      generic.setPlayers(site.getWaiverWire());
      generic.print(out);

      if (site.getDate().getMonthOfYear() < DateTimeConstants.APRIL) {
        LOG.info("AFL...");
        generic.setTitle("AFL");
        generic.setPlayers(Iterables.filter(newRoster.getAllPlayers(), Player::isAflEligible));
        generic.print(out);

        LOG.info("Winter ball...");
        generic.setTitle("Winter ball");
        generic.setPlayers(Iterables.filter(newRoster.getAllPlayers(), Player::isWinterEligible));
        generic.print(out);

        LOG.info("Rule 5...");
        generic.setTitle("Rule 5");
        generic.setPlayers(
            Iterables.filter(
                site.getRuleFiveDraft(),
                new Predicate<Player>() {
                  public boolean apply(Player p) {
                    return JavaAdapter.nowValue(p, predictor).vsReplacement().get().toLong() >= 0;
                  }
                }));
        generic.print(out);
      }

      LOG.info("T=O...");
      generic.setTitle("T=O");
      generic.setPlayers(
          Iterables.filter(
              newRoster.getAllPlayers(),
              p -> {
                if (!p.isTOEligible()) {
                  return false;
                }

                Long current = JavaAdapter.nowValue(p, predictor).vsReplacement().get().toLong();
                Long future =
                    JavaAdapter.futureValue(p, predictor).vsReplacement().isEmpty()
                        ? 0
                        : JavaAdapter.futureValue(p, predictor).vsReplacement().get().toLong();

                if (p.getAge() <= 25 && future > 0) {
                  return true;
                }

                return false;
              }));
      generic.print(out);

      LOG.info("SP to MR...");
      generic.setTitle("SP to MR");
      generic.setPlayers(Iterables.filter(newRoster.getAllPlayers(), Player::isSpToMrEligible));
      generic.print(out);
    }

    Moves moves = new Moves(newRoster, changes, predictor);

    generic.setTitle("Release");
    generic.setPlayers(moves.getRelease());
    generic.print(out);

    generic.setTitle("Sign");
    generic.setPlayers(moves.getSign());
    generic.print(out);

    if (site.getDate().getMonthOfYear() == DateTimeConstants.MARCH) {
      LOG.log(Level.INFO, "Spring training...");
      Printables.print(SpringTraining.create(site.getType(), newRoster.getAllPlayers())).to(out);
    }

    Printables.print(ReplacementLevels$.MODULE$.getForIdeal(predictor)).to(out);

    LOG.info("Development Report...");
    Printables.print(new DevelopmentReport(site, predictor)).to(out);

    LOG.info("Historial Development Report...");
    Printables.print(new HistorialDevelopmentReport(site, predictor)).to(out);

    LOG.info("Potential vs actual Report...");
    Printables.print(new PotentialVsActualReport(site)).to(out);

    LOG.info("Draft...");
    ImmutableSet<Player> drafted = ImmutableSet.copyOf(changes.get(Changes.ChangeType.PICKED));
    Iterable<Player> remaining =
        Sets.difference(
            ImmutableSet.copyOf(FluentIterable.from(site.getDraft()).filter(Predicates.notNull())),
            drafted);

    if (!Iterables.isEmpty(remaining)) {
      generic.setTitle("Drafted");
      generic.setPlayers(drafted);
      generic.print(out);

      generic.setTitle("Remaining");
      generic.setPlayers(remaining);
      generic.print(out);
    }

    DraftReport.create(site, predictor).print(out);

    Printables.print(salary).to(out);

    LOG.info("Extensions report...");
    generic.setTitle("Extensions");
    generic.setPlayers(
        Iterables.concat(
            Iterables.filter(newRoster.getAllPlayers(), site.isFutureFreeAgent()), futureFas));
    generic.print(out);

    LOG.info("Arbitration report...");
    generic.setTitle("Arbitration");
    generic.setPlayers(
        Iterables.filter(
            newRoster.getAllPlayers(),
            new Predicate<Player>() {
              public boolean apply(Player p) {
                return p.getSalary().endsWith("a");
              }
            }));
    generic.print(out);

    LOG.info("FA report...");
    generic.setTitle("Top Targets");
    generic.setPlayers(topFaTargets);
    generic.print(out);

    generic.setTitle("Free Agents");
    generic.setPlayers(site.getFreeAgents());
    generic.setLimit(50);
    generic.print(out);

    Printables.print(new TeamPositionReport(newRoster, predictor)).to(out);

    generic.setTitle("Trade Values");
    generic.setPlayers(Iterables.concat(newRoster.getAllPlayers(), futureFas));
    generic.setLimit(200);
    generic.print(out);

    Set<Player> all = Sets.newHashSet();

    Set<Player> minorLeaguers = Sets.newHashSet();

    LOG.log(Level.INFO, "Team reports...");
    generic.setLimit(50);

    int n = Iterables.size(site.getTeamIds());

    int count = 1;
    for (Id<Team> id : site.getTeamIds()) {
      SingleTeam t = site.getSingleTeam(id);
      LOG.log(Level.INFO, "{0} ({1}/{2})...", new Object[] {t.getName(), count, n});
      generic.setTitle(t.getName());

      Roster r = t.getRoster();

      Iterables.addAll(all, r.getAllPlayers());

      Iterables.addAll(minorLeaguers, r.getPlayers(Status.AAA));
      Iterables.addAll(minorLeaguers, r.getPlayers(Status.AA));
      Iterables.addAll(minorLeaguers, r.getPlayers(Status.A));

      generic.setPlayers(r.getAllPlayers());
      generic.print(out);
      count++;
    }

    TeamReport now =
        TeamReport.create("Now", predictor, site, new PlayerValue(predictor).getNowValue());

    LOG.info("Team Now Report...");

    now.sortByEndOfSeason();

    Printables.print(now).to(out);

    generic.useDefaultValueFunction();

    LOG.log(Level.INFO, "Non Top 10 prospects...");
    ImmutableSet<Player> nonTopTens =
        ImmutableSet.copyOf(
            Iterables.filter(
                all,
                new Predicate<Player>() {
                  public boolean apply(Player p) {
                    return !p.getTeamTopProspectPosition().isPresent()
                        && p.getAge() <= 25
                        && site.getCurrentSalary(p) == 0;
                  }
                }));

    generic.setTitle("Non Top prospects");
    generic.setLimit(50);
    generic.setPlayers(nonTopTens);
    generic.print(out);

    LOG.log(Level.INFO, "Minor league non-prospects...");

    ImmutableSet<Player> mlNonProspects =
        ImmutableSet.copyOf(
            Iterables.filter(
                minorLeaguers,
                new Predicate<Player>() {
                  public boolean apply(Player p) {
                    return p.getAge() > 25;
                  }
                }));

    generic.setTitle("ML non-prospects");
    generic.setPlayers(mlNonProspects);
    generic.print(out);

    if (def.getName().equals("BTHUSTLE")) {
      LOG.info("SP to MR...");
      generic.setTitle("SP to MR");
      generic.setPlayers(
          Iterables.filter(Iterables.concat(all, site.getFreeAgents()), Player::isSpToMrEligible));
      generic.print(out);
    }

    LOG.log(Level.INFO, "Done.");
  }
}
