package com.ljs.scratch.ootp;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.config.Changes;
import com.ljs.scratch.ootp.config.Directories;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.draft.DraftReport;
import com.ljs.scratch.ootp.io.Printables;
import com.ljs.scratch.ootp.ootp5.report.PowerRankingsReport;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.ratings.PlayerRatings;
import com.ljs.scratch.ootp.regression.BattingRegression;
import com.ljs.scratch.ootp.regression.PitchingRegression;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.regression.SplitPercentages;
import com.ljs.scratch.ootp.report.RosterReport;
import com.ljs.scratch.ootp.report.SalaryRegression;
import com.ljs.scratch.ootp.report.SalaryReport;
import com.ljs.scratch.ootp.report.TeamReport;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Roster.Status;
import com.ljs.scratch.ootp.roster.RosterSelection;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.selection.Mode;
import com.ljs.scratch.ootp.selection.Selections;
import com.ljs.scratch.ootp.selection.Slot;
import com.ljs.scratch.ootp.selection.depthchart.AllDepthCharts;
import com.ljs.scratch.ootp.selection.depthchart.DepthChartSelection;
import com.ljs.scratch.ootp.selection.lineup.AllLineups;
import com.ljs.scratch.ootp.selection.lineup.LineupSelection;
import com.ljs.scratch.ootp.selection.rotation.Rotation;
import com.ljs.scratch.ootp.selection.rotation.RotationSelection;
import com.ljs.scratch.ootp.site.SingleTeam;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.SiteDefinitionFactory;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.SplitStats;
import com.ljs.scratch.ootp.value.FourtyManRoster;
import com.ljs.scratch.ootp.value.FreeAgentAcquisition;
import com.ljs.scratch.ootp.value.FreeAgents;
import com.ljs.scratch.ootp.value.GenericValueReport;
import com.ljs.scratch.ootp.value.PlayerValue;
import com.ljs.scratch.ootp.value.Trade;
import com.ljs.scratch.ootp.value.TradeValue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeConstants;

/**
 *
 * @author lstephen
 */
public class Ootp {

    private static final Logger LOG = Logger.getLogger(Ootp.class.getName());

    // PAM, MWF
    private static final SiteDefinition TWML =
        SiteDefinitionFactory.ootp6(
           "TWML", "http://www.darowski.com/twml/OOTP6Reports/", Id.<Team>valueOf(22), "Splendid Splinter", 24);
        //    "TWML", "http://www.darowski.com/twml/2033/", new TeamId("22"), "Splendid Splinter", 24);

    // WCH, TF
    private static final SiteDefinition CBL =
        SiteDefinitionFactory.ootp5(
            "CBL", "http://www.thecblonline.com/files/", Id.<Team>valueOf(24), "National", 24);

    // DET, TF
    private static final SiteDefinition HFTC =
        SiteDefinitionFactory.ootp5(
            "HFTC", "http://www.hitforthecycle.com/hftc-ootp/", Id.<Team>valueOf(8), "American", 32);

    //private static final SiteDefinition TWIB =
    //    SiteDefinition.ootp6("TWIB", "http://twib.us/reports/", new TeamId("16"), "National", 16);

    //private static final SiteDefinition NTBL =
    //    SiteDefinition.ootp6("NTBL", "http://ntbl.twib.us/reports/", new TeamId("2"), "American", 16);

    // CHC, TTSn
    private static final SiteDefinition BTH =
        SiteDefinitionFactory.ootp6("BTH", "http://bthbaseball.allsimbaseball10.com/game/lgreports/", Id.<Team>valueOf(20), "National", 30);

    // WTT, TTSt
    private static final SiteDefinition SAVOY =
        SiteDefinitionFactory.ootp5("SAVOY", "http://www.thecblonline.com/savoy/", Id.<Team>valueOf(26), "UBA", 26);

    // CIN, TTSn
    private static final SiteDefinition LBB =
        //SiteDefinitionFactory.ootp5("LBB", "http://longballerbaseball.com/game/lgreports/Leaguesite/", new TeamId("21"), "NL", 30);
        SiteDefinitionFactory.ootp5("LBB", "http://longballerbaseball.x10.mx/lgreport/Leaguesite/", Id.<Team>valueOf(21), "NL", 30);

    //private static final SiteDefinition GABL =
    //    SiteDefinition.ootp5("GABL", "http://www.goldenageofbaseball.com/commish/Leaguesite/", new TeamId("10"), "American", 30);

    private static final SiteDefinition TFMS =
        SiteDefinitionFactory.ootp5("TFMS", "tfms5-2004/", Id.<Team>valueOf(3), "League 2", 16);

    private static final SiteDefinition PSD =
        SiteDefinitionFactory.ootpx("PSD", "http://www.redraftleague.com/game/lgreports2/news/html/", Id.<Team>valueOf(8), "American", 20);

    //private static final SiteDefinition OSBL =
    //    SiteDefinitionFactory.ootpx("OBSL", "http://www.theobsl.com/2013seasonootp/", Id.<Team>valueOf(30), "National", 30);

    public static void main(String[] args) throws IOException {
        new Ootp().run();
    }

    public void run() throws IOException {
        for (SiteDefinition def : Arrays.asList
            //( TWML
            ( CBL
            //( HFTC
            //, LBB
            //( BTH
            //, SAVOY
            //, PSD
            //( TFMS
            )) {
            try (
                FileOutputStream out =
                    new FileOutputStream(new File(Directories.OUT + def.getName() + ".txt"), false)) {
                run(def, out);
            }
        }
    }

    private void run(SiteDefinition def, OutputStream out) throws IOException {
        LOG.log(Level.INFO, "Running for {0}...", def.getName());

        final Site site = def.getSite();

        LOG.log(Level.INFO, "Extracting current roster and team...");

        Roster oldRoster = site.extractRoster();
        Team team = site.extractTeam();

        LOG.log(Level.INFO, "Running regressions...");

        final BattingRegression battingRegression = BattingRegression.run(site);
        Printables.print(battingRegression.correlationReport()).to(out);

        final PitchingRegression pitchingRegression = PitchingRegression.run(site);
        Printables.print(pitchingRegression.correlationReport()).to(out);

        SplitPercentages pcts = SplitPercentages.create(site);
        pcts.print(out);

        SplitStats.setPercentages(pcts);
        PlayerRatings.setPercentages(pcts);

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

        Optional<FreeAgentAcquisition> fa = Optional.absent();
        Optional<FreeAgentAcquisition> nfa = Optional.absent();

        Set<Player> released = Sets.newHashSet();

        FreeAgents fas = FreeAgents.create(site, changes, tv.getTradeTargetValue(), tv);

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
                fa = fas.getTopAcquisition(team);

                if (fa.isPresent()) {
                    fas.skip(fa.get().getFreeAgent());

                    if (oldRoster.size() > minRosterSize) {
                        team.remove(fa.get().getRelease());
                    }
                }

                nfa = fas.getNeedAcquisition(team);

                if (nfa.isPresent()) {
                    if (oldRoster.size() > minRosterSize) {
                        team.remove(nfa.get().getRelease());
                    }

                    fas.skip(nfa.get().getFreeAgent());
                }
            }
        }

        RosterSelection selection = RosterSelection.ootp6(team, battingRegression, pitchingRegression, tv.getTradeTargetValue());

        if (site.getType() == Version.OOTP5) {
            selection = RosterSelection.ootp5(team, battingRegression, pitchingRegression, tv.getTradeTargetValue());
        }

        if (site.getType() == Version.OOTPX) {
            selection = RosterSelection.ootpx(team, battingRegression, pitchingRegression, tv.getTradeTargetValue());
        }

        selection.setPrevious(oldRoster);

        LOG.log(Level.INFO, "Selecting new rosters...");

        //if (def.getName().equals("TWML")) {
        //    mode = Mode.PLAYOFFS;
        //}

        Roster newRoster = selection.select(mode, changes);

        newRoster.setTargetMinimum(minRosterSize);
        newRoster.setTargetMaximum(maxRosterSize);

        selection.printBattingSelectionTable(out, changes);
        selection.printPitchingSelectionTable(out, changes);

        Printables.print(newRoster).to(out);

        LOG.info("Calculating roster changes...");

        Printables.print(newRoster.getChangesFrom(oldRoster)).to(out);

        if (fa.isPresent() && newRoster.size() < maxRosterSize) {
            Player player = fa.get().getFreeAgent();
            out.write(
                String.format(
                    "%4s -> %-4s %2s %s%n",
                    "FA",
                    "",
                    player.getPosition(),
                    player.getShortName())
                .getBytes(Charsets.UTF_8));
        }

        if (nfa.isPresent() && newRoster.size() < maxRosterSize) {
            Player player = nfa.get().getFreeAgent();
            out.write(
                String.format(
                    "%4s -> %-4s %2s %s%n",
                    "FA",
                    "",
                    player.getPosition(),
                    player.getShortName())
                .getBytes(Charsets.UTF_8));
        }

        LOG.log(Level.INFO, "Choosing lineups...");

        AllLineups lineups =
            new LineupSelection(battingRegression.predict(newRoster.getAllPlayers()))
                .select(Selections.onlyHitters(newRoster.getPlayers(Status.ML)));

        Printables.print(lineups).to(out);

        LOG.log(Level.INFO, "Choosing Depth Charts...");

        AllDepthCharts depthCharts = DepthChartSelection
            .create(battingRegression.predict(newRoster.getAllPlayers()))
            .select(lineups, Selections.onlyHitters(newRoster.getPlayers(Status.ML)));

        depthCharts.print(out);

        //LOG.log(Level.INFO, "New lineups test...");
        //new LineupScoreSelection(pcts, ps, out).select(changes.get(ChangeType.FORCE_ML), Selections.onlyHitters(newRoster.getAllPlayers()));

        LOG.log(Level.INFO, "Choosing rotation...");

        Rotation rotation =
            RotationSelection
                .forMode(
                    mode,
                    pitchingRegression.predict(team),
                    site.getPitcherSelectionMethod())
                .select(newRoster.getPlayers(Status.ML));

        rotation.print(out);

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

        generic.printReplacementLevelReport(out);

        RosterReport rosterReport = RosterReport.create(site, newRoster);

        Printables.print(rosterReport).to(out);

        generic.setCustomValueFunction(tv.getTradeTargetValue());

        generic.setReverse(false);

        LOG.info("Draft...");
        ImmutableSet<Player> drafted = ImmutableSet.copyOf(changes.get(Changes.ChangeType.PICKED));
        Iterable<Player> remaining = Sets.difference(ImmutableSet.copyOf(site.getDraft()), drafted);


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
        generic.setPlayers(Iterables.filter(newRoster.getAllPlayers(), site.isFutureFreeAgent()));
        generic.print(out);

        LOG.info("Salary report...");
        SalaryReport salary = new SalaryReport(team, site);
        Printables.print(salary).to(out);

        if (def.getName().equals("BTH")) {
            LOG.info("40 man roster reports...");

            FourtyManRoster fourtyMan = new FourtyManRoster(
                newRoster,
                Predictions
                    .predict(newRoster.getAllPlayers())
                    .using(battingRegression, pitchingRegression, ps.getPitcherOverall()),
                tv.getTradeTargetValue());

            fourtyMan.printReport(out);

            generic.setTitle("+40");
            generic.setPlayers(fourtyMan.getPlayersToAdd());
            generic.print(out);

            generic.setTitle("-40");
            generic.setPlayers(fourtyMan.getPlayersToRemove());
            generic.setReverse(true);
            generic.print(out);

            generic.setReverse(false);
        }

        if (battingRegression.isEmpty() || pitchingRegression.isEmpty()) {
            return;
        }

        if (def.getName().equals("BTH")) {
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


        generic.setTitle("Trade Values");
        generic.setPlayers(
            Iterables.concat(
                newRoster.getAllPlayers(),
                Iterables.transform(
                    Optional.presentInstances(ImmutableSet.of(fa, nfa)),
                    FreeAgentAcquisition.Meta.getRelease()),
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

        if (site.getType() != Version.OOTPX) {
            LOG.info("Power Rankings...");
            Printables.print(PowerRankingsReport.create(site)).to(out);
        }

        LOG.info("Team Now Report...");
        TeamReport now = TeamReport.create(
            "Now",
            site,
            new PlayerValue(ps, battingRegression, pitchingRegression)
                .getNowValue());

        Printables.print(now).to(out);

        LOG.info("Team Medium Term Report...");
        TeamReport future = TeamReport.create(
            "Medium Term",
            site,
            new PlayerValue(ps, battingRegression, pitchingRegression)
                .getFutureValue());

        Printables.print(future).to(out);

        generic.setLimit(10);

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


        LOG.log(Level.INFO, "Top Trades...");

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
        }

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

        LOG.log(Level.INFO, "Top Trades for non-prospect minor leaguers...");

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
