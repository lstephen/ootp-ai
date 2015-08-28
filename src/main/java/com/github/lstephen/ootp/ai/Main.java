package com.github.lstephen.ootp.ai;

import com.github.lstephen.ootp.ai.config.Config;
import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.draft.DraftReport;
import com.github.lstephen.ootp.ai.io.Printables;
import com.github.lstephen.ootp.ai.ootp5.report.SpringTraining;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.player.ratings.BattingRatings;
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings;
import com.github.lstephen.ootp.ai.player.ratings.PlayerRatings;
import com.github.lstephen.ootp.ai.player.ratings.Position;
import com.github.lstephen.ootp.ai.regression.BattingRegression;
import com.github.lstephen.ootp.ai.regression.PitchingRegression;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.report.FreeAgents;
import com.github.lstephen.ootp.ai.report.GenericValueReport;
import com.github.lstephen.ootp.ai.report.HittingSelectionReport;
import com.github.lstephen.ootp.ai.report.LeagueBattingReport;
import com.github.lstephen.ootp.ai.report.RosterReport;
import com.github.lstephen.ootp.ai.report.SalaryRegression;
import com.github.lstephen.ootp.ai.report.SalaryReport;
import com.github.lstephen.ootp.ai.report.TeamPositionReport;
import com.github.lstephen.ootp.ai.report.TeamReport;
import com.github.lstephen.ootp.ai.report.Trade;
import com.github.lstephen.ootp.ai.roster.Changes;
import com.github.lstephen.ootp.ai.roster.Changes.ChangeType;
import com.github.lstephen.ootp.ai.roster.FourtyManRoster;
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
import com.github.lstephen.ootp.ai.site.Version;
import com.github.lstephen.ootp.ai.site.impl.SiteDefinitionFactory;
import com.github.lstephen.ootp.ai.splits.Splits;
import com.github.lstephen.ootp.ai.stats.SplitPercentages;
import com.github.lstephen.ootp.ai.stats.SplitPercentagesHolder;
import com.github.lstephen.ootp.ai.stats.SplitStats;
import com.github.lstephen.ootp.ai.value.FreeAgentAcquisition;
import com.github.lstephen.ootp.ai.value.PlayerValue;
import com.github.lstephen.ootp.ai.value.TradeValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import org.joda.time.DateTimeConstants;

/**
 *
 * @author lstephen
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private static final SiteDefinition TWML =
        SiteDefinitionFactory.ootp6(
           "TWML", "http://www.darowski.com/twml/OOTP6Reports/", Id.<Team>valueOf(19), "Splendid Splinter", 20);

    private static final SiteDefinition CBL =
        SiteDefinitionFactory.ootp5(
            "CBL", "http://www.thecblonline.com/files/", Id.<Team>valueOf(20), "National", 20);

    private static final SiteDefinition HFTC =
        SiteDefinitionFactory.ootp5(
            "HFTC", "http://www.hitforthecycle.com/hftc-ootp/", Id.<Team>valueOf(8), "American", 32);

    private static final SiteDefinition OLD_BTH_CHC =
        SiteDefinitionFactory.ootp6("BTH", "http://bthbaseball.allsimbaseball10.com/game/oldbth/lgreports/", Id.<Team>valueOf(20), "National", 30);

    private static final SiteDefinition OLD_BTH_NYY =
        SiteDefinitionFactory.ootp6("OLD_BTH_NYY", "http://bthbaseball.allsimbaseball10.com/game/oldbth/lgreports/", Id.<Team>valueOf(3), "American", 30);

    private static final SiteDefinition BTHUSTLE =
        SiteDefinitionFactory.ootp6("BTHUSTLE", "http://bthbaseball.allsimbaseball10.com/game/lgreports/", Id.<Team>valueOf(14), "National", 16);

    private static final SiteDefinition SAVOY =
        SiteDefinitionFactory.ootp5("SAVOY", "http://www.thecblonline.com/savoy/", Id.<Team>valueOf(26), "UBA", 26);

    private static final SiteDefinition LBB =
        SiteDefinitionFactory.ootp5("LBB", "http://bbs56.net/LBB/Site/Leaguesite/", Id.<Team>valueOf(3), "AL", 20);

    private static final SiteDefinition GABL =
        SiteDefinitionFactory.ootp5("GABL", "http://www.goldenageofbaseball.com/commish/Leaguesite/", Id.<Team>valueOf(22), "National", 30);

    private static final SiteDefinition TFMS =
        SiteDefinitionFactory.ootp5("TFMS", "tfms5-2004/", Id.<Team>valueOf(3), "League 2", 16);

    private static final ImmutableMap<String, SiteDefinition> SITES =
        ImmutableMap
            .<String, SiteDefinition>builder()
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

        File outputDirectory =
            new File(Config.createDefault().getValue("output.dir").or("c:/ootp"));

        try (
            FileOutputStream out =
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
        Boolean isPlayoffs = "true".equals(System.getenv("OOTPAI_PLAYOFFS"));

        if (LOOK_TO_NEXT_SEASON.contains(def)) {
            isLookToNextSeason = Boolean.TRUE;
        }

        final Site site = def.getSite();

        SiteHolder.set(site);

        Printables.print(LeagueBattingReport.create(site)).to(out);

        LOG.log(Level.INFO, "Extracting current roster and team...");

        Roster oldRoster = site.extractRoster();
        Team team = site.extractTeam();

        LOG.log(Level.INFO, "Running regressions...");

        SplitPercentages pcts = SplitPercentages.create(site);
        SplitPercentagesHolder.set(pcts);

        final BattingRegression battingRegression = BattingRegression.run(site);
        Printables.print(battingRegression.correlationReport()).to(out);

        final PitchingRegression pitchingRegression = PitchingRegression.run(site);
        Printables.print(pitchingRegression.correlationReport()).to(out);

        if (isLookToNextSeason) {
            battingRegression.ignoreStatsInPredictions();
            pitchingRegression.ignoreStatsInPredictions();
        }

        pcts.print(out);


        SplitStats.setPercentages(pcts);
        PlayerRatings.setPercentages(pcts);
        BestStartersSelection.setPercentages(pcts);
        Bench.setPercentages(pcts);

        LOG.info("Loading manual changes...");
        Changes changes = Changes.load(site);

        team.processManualChanges(changes);

        LOG.info("Setting up Predictions...");
        final Predictions ps = Predictions.predict(team).using(battingRegression, pitchingRegression, site.getPitcherSelectionMethod());
        final TradeValue tv = new TradeValue(team, ps, battingRegression, pitchingRegression);

        boolean isExpandedRosters =
            site.getDate().getMonthOfYear() == DateTimeConstants.SEPTEMBER;

        int month = site.getDate().getMonthOfYear();

        Mode mode = Mode.REGULAR_SEASON;

        if (month < DateTimeConstants.APRIL) {
            mode = Mode.PRESEASON;
        } else if (isExpandedRosters) {
            mode = Mode.EXPANDED;
        }

        LOG.info("Loading FAS...");
        Iterable<FreeAgentAcquisition> faas = Collections.emptyList();

        Set<Player> released = Sets.newHashSet();

        FreeAgents fas = FreeAgents.create(site, changes, tv.getTradeTargetValue(), tv);

        if (INAUGURAL_DRAFT.contains(def)) {
          RosterReport rr = RosterReport.create(site, team);

          Printables.print(rr).to(out);

          final GenericValueReport generic = new GenericValueReport(team, ps, battingRegression, pitchingRegression, null);

          if (battingRegression.isEmpty() && pitchingRegression.isEmpty()) {
            generic.setCustomValueFunction((Player p) -> {
              Double base = 0.0;

              if (p.isPitcher()) {
                Splits<PitchingRatings<?>> rs = p.getPitchingRatings();

                PitchingRatings<?> vL = rs.getVsLeft();
                PitchingRatings<?> vR = rs.getVsRight();

                base = (vL.getStuff() + vL.getMovement() + vL.getControl()
                     + 2.0 * (vR.getStuff() + vR.getMovement() + vR.getControl())) / 3.0;
              } else {
                Splits<BattingRatings<?>> rs = p.getBattingRatings();

                BattingRatings<?> vL = rs.getVsLeft();
                BattingRatings<?> vR = rs.getVsRight();

                base = (vL.getContact() + vL.getPower() + vL.getEye()
                     + 2.0 * (vR.getContact() + vR.getPower() + vR.getEye())) / 3.0;
              }

              Integer need = Slot.getPlayerSlots(p)
                .stream()
                .filter((s) -> s != Slot.P)
                .filter((s) -> s != Slot.H || Slot.getPrimarySlot(p) == Slot.H)
                .map((s) -> rr.getTargetRatio() - rr.getRatio(s))
                .max(Integer::compare)
                .orElse(0);


              need = Math.max(0, need);

              int intangibles = 0;

              if (p.getClutch().isPresent()) {
                switch (p.getClutch().get()) {
                  case GREAT:
                    intangibles += 1;
                    break;
                  case SUFFERS:
                    intangibles += -1;
                    break;
                  default:
                    // do nothing
                }
              }

              if (p.getConsistency().isPresent()) {
                switch (p.getConsistency().get()) {
                  case VERY_INCONSISTENT:
                    intangibles -= 1;
                    break;
                  case GOOD:
                    intangibles += 1;
                    break;
                  default:
                    // do nothing
                }
              }

              Double mrFactor = Slot.getPrimarySlot(p) == Slot.MR ? 0.865 : 1.0;

              return new Double(mrFactor * base - p.getAge() + need - PlayerValue.getAgingFactor(p) + intangibles).intValue();
            });
          }

          generic.setTitle("Selected");
          generic.setPlayers(team);
          generic.print(out);

          generic.setTitle("Free Agents");
          generic.setPlayers(site.getFreeAgents());
          generic.print(out);
          return;
        }

        LOG.info("Calculating top FA targets...");
        Iterable<Player> topFaTargets = fas.getTopTargets(mode);

        Integer minRosterSize = 90;
        Integer maxRosterSize = 110;

        if (site.getName().equals("LBB")) {
            maxRosterSize = 100;
        }
        if (site.getName().equals("PSD")) {
            maxRosterSize = 165;
            minRosterSize = 135;
        }

        if (oldRoster.size() > maxRosterSize) {
            for (int i = 0; i < 2; i++) {
                Optional<Player> release = fas.getPlayerToRelease(team);

                if (release.isPresent()) {
                    team.remove(release.get());
                    released.add(release.get());
                }
            }
        }

        if (site.getDate().getMonthOfYear() < DateTimeConstants.SEPTEMBER
            && site.getDate().getMonthOfYear() > DateTimeConstants.MARCH
            && !battingRegression.isEmpty()
            && !pitchingRegression.isEmpty()) {

            LOG.info("Determining FA acquisition...");

            if (oldRoster.size() <= maxRosterSize) {
                Integer n = 2;

                if (site.getName().equals("CBL") || site.getName().equals("TWML") || site.getName().equals("BTHUSTLE")) {
                    n = 1;
                }

                if (site.getName().equals("SAVOY")
                  || site.getName().equals("GABL")
                  || site.getName().equals("BTH")
                  || site.getName().contains("OLD_BTH")) {
                    n = 0;
                }

                faas = FreeAgentAcquisition.select(site, changes, team, fas.all(), tv, n);

                if (oldRoster.size() > minRosterSize) {
                    for (FreeAgentAcquisition faa : faas) {
                        team.remove(faa.getRelease());
                    }
                }
            }
        }

        ImmutableSet<Player> futureFas = ImmutableSet.of();

        if (isLookToNextSeason) {
            futureFas =
                ImmutableSet.copyOf(
                    Iterables.filter(team, site.isFutureFreeAgent()));

            System.out.print("Removing (FA):");
            for (Player p : Player.byShortName().immutableSortedCopy(futureFas)) {
                System.out.print(p.getShortName() + "/");
            }
            System.out.println();

            team.remove(futureFas);
        }

        RosterSelection selection = RosterSelection.ootp6(team, battingRegression, pitchingRegression, tv);

        if (site.getType() == Version.OOTP5) {
            selection = RosterSelection.ootp5(team, battingRegression, pitchingRegression, tv);
        }

        selection.setPrevious(oldRoster);

        LOG.log(Level.INFO, "Selecting new rosters...");

        Stopwatch sw = Stopwatch.createStarted();

        Mode selectionMode = mode;

        if (isPlayoffs) {
            selectionMode = Mode.PLAYOFFS;
        }

        Roster newRoster = selection.select(selectionMode, changes);

        newRoster.setTargetMinimum(minRosterSize);
        newRoster.setTargetMaximum(maxRosterSize);

        sw.stop();

        LOG.log(Level.INFO, "Roster selection time: " + sw);

        Printables.print(new HittingSelectionReport(newRoster, ps, site.getTeamBatting())).to(out);
        selection.printPitchingSelectionTable(out, newRoster, site.getTeamPitching());

        Printables.print(newRoster).to(out);

        LOG.info("Calculating roster changes...");

        Printables.print(newRoster.getChangesFrom(oldRoster)).to(out);

        for (FreeAgentAcquisition faa : faas) {
            Player player = faa.getFreeAgent();
            out.write(
                String.format(
                    "%4s -> %-4s %2s %s%n",
                    "FA",
                    "",
                    player.getPosition(),
                    player.getShortName())
                .getBytes(Charsets.ISO_8859_1));
        }

        LOG.log(Level.INFO, "Choosing rotation...");

        Rotation rotation =
            RotationSelection
                .forMode(
                    selectionMode,
                    pitchingRegression.predict(team),
                    site.getPitcherSelectionMethod())
                .selectRotation(ImmutableSet.<Player>of(), Selections.onlyPitchers(newRoster.getPlayers(Status.ML)));

        Printables.print(rotation).to(out);

        LOG.log(Level.INFO, "Choosing lineups...");

        AllLineups lineups =
          new LineupSelection(Predictions.predict(newRoster.getAllPlayers()).using(battingRegression, pitchingRegression, site.getPitcherSelectionMethod()))
                .select(Selections.onlyHitters(newRoster.getPlayers(Status.ML)));

        LOG.log(Level.INFO, "Choosing Depth Charts...");

        AllDepthCharts depthCharts = new DepthChartSelection(ps)
            .select(lineups, Selections.onlyHitters(newRoster.getPlayers(Status.ML)));

        Printables.print(depthCharts).to(out);

        Printables.print(lineups).to(out);

        LOG.log(Level.INFO, "Salary Regression...");

        SalaryRegression salaryRegression = new SalaryRegression(tv, site);

        Integer n = Iterables.size(site.getTeamIds());
        int count = 1;
        for (Id<Team> id : site.getTeamIds()) {
            LOG.log(Level.INFO, "{0}/{1}...", new Object[] { count, n });

            salaryRegression.add(site.getSalariedPlayers(id));
            count++;
        }

        Printables.print(salaryRegression).to(out);

        final GenericValueReport generic = new GenericValueReport(team, ps, battingRegression, pitchingRegression, salaryRegression);
        generic.setCustomValueFunction(tv.getTradeTargetValue());
        generic.setReverse(false);

        LOG.log(Level.INFO, "Strategy...");
        generic.setTitle("Bunt for Hit");
        generic.setPlayers(newRoster
            .getPlayers(Status.ML)
            .stream()
            .filter(p -> p.getBuntForHitRating().normalize().get() > 80)
            .collect(Collectors.toSet()));
        generic.print(out);

        generic.setTitle("Stealing");
        generic.setPlayers(newRoster
            .getPlayers(Status.ML)
            .stream()
            .filter(p -> p.getStealingRating().normalize().get() > 80)
            .collect(Collectors.toSet()));
        generic.print(out);

        if (site.getDate().getMonthOfYear() == DateTimeConstants.MARCH) {
            LOG.log(Level.INFO, "Spring training...");
            Printables
                .print(SpringTraining.create(
                    site.getType(), newRoster.getAllPlayers()))
                .to(out);
        }

        generic.printReplacementLevelReport(out);

        RosterReport rosterReport = RosterReport.create(site, newRoster);

        Printables.print(rosterReport).to(out);

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

        DraftReport.create(site, tv).print(out);

        LOG.info("Extensions report...");
        generic.setTitle("Extensions");
        generic.setPlayers(
            Iterables.concat(
                Iterables.filter(newRoster.getAllPlayers(), site.isFutureFreeAgent()),
                futureFas));
        generic.print(out);

        LOG.info("Arbitration report...");
        generic.setTitle("Arbitration");
        generic.setPlayers(Iterables.filter(newRoster.getAllPlayers(), new Predicate<Player>() {
            public boolean apply(Player p) {
                return p.getSalary().endsWith("a");
            }
        }));
        generic.print(out);

        LOG.info("Salary report...");
        SalaryReport salary = new SalaryReport(team, site);
        Printables.print(salary).to(out);

        if (def.getName().equals("BTHUSTLE")) {
            LOG.info("40 man roster reports...");

            FourtyManRoster fourtyMan = new FourtyManRoster(
                newRoster,
                Predictions
                    .predict(newRoster.getAllPlayers())
                    .using(battingRegression, pitchingRegression, ps.getPitcherOverall()),
                tv);

            Printables.print(fourtyMan).to(out);

            generic.setTitle("+40");
            generic.setPlayers(ImmutableSet
                .<Player>builder()
                .addAll(fourtyMan.getPlayersToAdd())
                .addAll(FluentIterable
                    .from(changes.get(ChangeType.FOURTY_MAN))
                    .filter(Predicates.in(newRoster.getAllPlayers())))
                .build());
            generic.print(out);

            generic.setTitle("-40");
            generic.setPlayers(fourtyMan.getPlayersToRemove());
            generic.print(out);

            generic.setTitle("Waive");
            generic.setPlayers(isExpandedRosters ? ImmutableSet.of() : fourtyMan.getPlayersToWaive());
            generic.print(out);
        }

        if (battingRegression.isEmpty() || pitchingRegression.isEmpty()) {
            return;
        }

        if (def.getName().equals("BTHUSTLE")) {
            LOG.info("Waviers report...");
            generic.setTitle("Waivers");
            generic.setPlayers(site.getWaiverWire());
            generic.print(out);

            if (site.getDate().getMonthOfYear() < DateTimeConstants.APRIL) {
                LOG.info("Rule 5...");
                generic.setTitle("Rule 5");
                generic.setPlayers(
                    Iterables.filter(
                        site.getRuleFiveDraft(),
                        new Predicate<Player>() {
                            public boolean apply(Player p) {
                                return tv.getCurrentValueVsReplacement(p) >= 0;
                            }
                        }));
                generic.print(out);
            }
        }


        LOG.info("FA report...");
        generic.setTitle("Top Targets");
        generic.setPlayers(topFaTargets);
        generic.print(out);

        generic.setTitle("Free Agents");
        generic.setPlayers(site.getFreeAgents());
        generic.setLimit(50);
        generic.print(out);

        Printables.print(new TeamPositionReport(newRoster, ps)).to(out);

        generic.setTitle("Trade Values");
        generic.setPlayers(
            Iterables.concat(
                newRoster.getAllPlayers(),
                futureFas,
                Iterables.transform(
                    faas,
                    FreeAgentAcquisition::getRelease),
                released));
        generic.setLimit(200);
        generic.setMultiplier(1.1);
        generic.print(out);

        Set<Player> all = Sets.newHashSet();

        Set<Player> minorLeaguers = Sets.newHashSet();

        LOG.log(Level.INFO, "Team reports...");
        generic.setLimit(50);
        generic.setMultiplier(0.91);

        count = 1;
        for (Id<Team> id : site.getTeamIds()) {
            SingleTeam t = site.getSingleTeam(id);
            LOG.log(Level.INFO, "{0} ({1}/{2})...", new Object[] { t.getName(), count, n });
            generic.setTitle(t.getName());

            Roster r = t.getRoster();

            Iterables.addAll(all, r.getAllPlayers());

            Iterables.addAll(minorLeaguers, r.getPlayers(Status.AAA));
            Iterables.addAll(minorLeaguers, r.getPlayers(Status.AA));
            Iterables.addAll(minorLeaguers, r.getPlayers(Status.A));
            Iterables.addAll(minorLeaguers, r.getPlayers(Status.SA));
            Iterables.addAll(minorLeaguers, r.getPlayers(Status.R));

            generic.setPlayers(r.getAllPlayers());
            generic.print(out);
            count++;
        }

        TeamReport now = TeamReport.create(
            "Now",
            ps,
            site,
            new PlayerValue(ps, battingRegression, pitchingRegression)
                .getNowValue());
        if (!site.getName().contains("OLD_BTH") && !site.getName().equals("BTH")) {
          LOG.info("Power Rankings...");
          Printables.print(site.getPowerRankingsReport(now)).to(out);
        }

        LOG.info("Team Now Report...");

        now.sortByEndOfSeason();

        Printables.print(now).to(out);

        LOG.info("Team Medium Term Report...");
        TeamReport future = TeamReport.create(
            "Medium Term",
            ps,
            site,
            new PlayerValue(ps, battingRegression, pitchingRegression)
                .getFutureValue());

        future.sortByTalentLevel();
        Printables.print(future).to(out);

        generic.setLimit(10);
        generic.clearMultiplier();
        generic.setCustomValueFunction(
            new PlayerValue(ps, battingRegression, pitchingRegression)
                .getFutureValue());

        for (final Slot s : Slot.values()) {
            if (s == Slot.P) {
                continue;
            }

            LOG.log(Level.INFO, "Slot Report: {0}...", s.name());

            generic.setTitle("Top: " + s.name());
            generic.setPlayers(Iterables.filter(all, new Predicate<Player>() {
                public boolean apply(Player p) {
                    return Slot.getPrimarySlot(p) == s;
                }
            }));

            generic.print(out);
        }

        for (final Position pos : ImmutableList.of(Position.SECOND_BASE, Position.THIRD_BASE)) {
            LOG.log(Level.INFO, "Position Report: {0}...", pos.name());

            generic.setTitle("Top: " + pos.name());
            generic.setPlayers(Iterables.filter(all, new Predicate<Player>() {
                public boolean apply(Player pl) {
                    return pl.getPosition().equals(pos.getAbbreviation());
                }
            }));

            generic.print(out);
        }

        LOG.log(Level.INFO, "League wide replacement Level...");

        generic.setCustomValueFunction(tv.getTradeTargetValue());

        /*LOG.log(Level.INFO, "Trade target report...");
        generic.setTitle("Trade Targets");
        generic.setLimit(50);
        generic.setPlayers(all);
        generic.print(out);*/

        /*LOG.log(Level.INFO, "Minor league report...");
        generic.setTitle("Minor Leagues");
        generic.setPlayers(minorLeaguers);
        generic.setLimit(50);
        generic.print(out);*/

        LOG.log(Level.INFO, "Trade Bait report...");
        generic.setCustomValueFunction(tv.getTradeBaitValue(site, salaryRegression));
        generic.setTitle("Trade Bait");
        generic.setPlayers(newRoster.getAllPlayers());
        generic.setLimit(200);
        generic.clearMultiplier();
        generic.print(out);

        generic.setCustomValueFunction(tv.getTradeTargetValue());

        Iterable<Player> topBait = Ordering
            .natural()
            .reverse()
            .onResultOf(tv.getTradeBaitValue(site, salaryRegression))
            .sortedCopy(newRoster.getAllPlayers());


        /*LOG.log(Level.INFO, "Top Trades...");

        int idx = 1;
        for (Trade trade
            : Iterables.limit(
                Trade.getTopTrades(
                    tv,
                    site,
                    salaryRegression,
                    Iterables.limit(topBait, newRoster.size() / 10),
                    all),
                20)) {

            generic.setTitle("#" + idx + "-" + trade.getValue(tv, site, salaryRegression));
            generic.setPlayers(trade);
            generic.print(out);
            idx++;
        }*/

        /*LOG.info("Below Replacement Trades...");
        ImmutableSet<Player> belowReplacementBait =
            ImmutableSet.copyOf(
                Iterables.filter(
                    Iterables.limit(topBait, newRoster.size() / 10),
                    new Predicate<Player>() {
                        @Override
                        public boolean apply(Player p) {
                            return tv.getCurrentValueVsReplacement(p) < 0
                                && tv.getFutureValueVsReplacement(p) < 0;
                        }
                    }));

        idx = 1;
        for (Trade trade
            : Iterables.limit(
                Trade.getTopTrades(
                    tv,
                    site,
                    salaryRegression,
                    belowReplacementBait,
                    all),
                20)) {

            generic.setTitle("BR #" + idx + "-" + trade.getValue(tv, site, salaryRegression));
            generic.setPlayers(trade);
            generic.print(out);
            idx++;
        }*/

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
        generic.setMultiplier(0.91);
        generic.setLimit(50);
        generic.setPlayers(nonTopTens);
        generic.print(out);

        /*idx = 1;
        for (Trade trade
            : Iterables.limit(
                Trade.getTopTrades(
                    tv,
                    site,
                    salaryRegression,
                    belowReplacementBait,
                    nonTopTens),
                20)) {

            generic.setTitle("NTT #" + idx + "-" + trade.getValue(tv, site, salaryRegression));
            generic.setPlayers(trade);
            generic.print(out);
            idx++;
        }*/


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

        generic.clearMultiplier();

        //LOG.log(Level.INFO, "Top Trades for non-prospect minor leaguers...");

        /*idx = 1;
        for (Trade trade
            : Iterables.limit(
                Trade.getTopTrades(
                    tv,
                    site,
                    salaryRegression,
                    belowReplacementBait,
                    mlNonProspects),
                20)) {

            generic.setTitle("NP-ML #" + idx + "-" + trade.getValue(tv, site, salaryRegression));
            generic.setPlayers(trade);
            generic.print(out);
            idx++;
        }*/

        LOG.log(Level.INFO, "Done.");
    }

}
