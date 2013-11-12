package com.ljs.scratch.ootp.report;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.annotation.ReturnTypesAreNonnullByDefault;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.io.Printable;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.selection.HitterSelectionFactory;
import com.ljs.scratch.ootp.selection.Mode;
import com.ljs.scratch.ootp.selection.PitcherSelectionFactory;
import com.ljs.scratch.ootp.selection.Selection;
import com.ljs.scratch.ootp.selection.SelectionFactory;
import com.ljs.scratch.ootp.selection.Selections;
import com.ljs.scratch.ootp.selection.Slot;
import com.ljs.scratch.ootp.selection.SlotSelection;
import com.ljs.scratch.ootp.site.LeagueStructure;
import com.ljs.scratch.ootp.site.Record;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.Standings;
import com.ljs.scratch.ootp.stats.PitchingStats;
import java.io.PrintWriter;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.fest.assertions.api.Assertions;
import org.fest.util.Strings;

/**
 *
 * @author lstephen
 */
@ParametersAreNonnullByDefault
@ReturnTypesAreNonnullByDefault
public final class TeamReport implements Printable {

    private final String title;

    private final Site site;

    private final Function<Player, Integer> value;

    private final PitchingStats leaguePitching;

    private final DescriptiveStatistics mlHitting = new DescriptiveStatistics();

    private final DescriptiveStatistics mlPitching = new DescriptiveStatistics();

    private Set<TeamScore> scores;

    private Ordering<TeamScore> ordering;

    private TeamReport(String title, Site site, Function<Player, Integer> value) {
        this.title = title;
        this.site = site;
        this.value = value;
        this.leaguePitching = site.getLeaguePitching();
    }

    public void sortByTalentLevel() {
        ordering = TeamScore.byWinningPercentage(getExpectedRunsPerGame());
    }

    public void sortByEndOfSeason() {
        ordering = Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<TeamScore, Double>() {
                @Override
                public Double apply(TeamScore score) {
                    Assertions.assertThat(score).isNotNull();

                    return getExpectedEndOfSeason(score.getId()).getWinPercentage();
                }
            });
    }

    private Ordering<TeamScore> getOrdering() {
        return ordering == null
            ? TeamScore.byWinningPercentage(getExpectedRunsPerGame())
            : ordering;
    }

    private Set<TeamScore> getScores() {
        if (scores == null) {
            scores = calculateScores();
        }

        return scores;
    }

    private TeamScore getScore(Id<Team> team) {
        for (TeamScore s : getScores()) {
            if (s.getId().equals(team)) {
                return s;
            }
        }
        throw new IllegalStateException();
    }

    private Set<TeamScore> calculateScores() {
        Set<TeamScore> scores = Sets.newHashSet();

        for (Id<Team> id : site.getTeamIds()) {
            scores.add(calculate(id));
        }

        return normalize(scores);
    }

    private Double getExpectedRunsPerGame() {
        return site.getPitcherSelectionMethod().getEraEstimate(leaguePitching);
    }

    @Override
    public void print(PrintWriter w) {
        Set<TeamScore> scores = getScores();


        w.println();
        w.println(String.format("%-20s | %-5s %-5s %-5s | %-5s %-5s %-5s | (rpg:%.2f)", title, " Bat", " LU", " Ovr", " Pit", " Rot", " Ovr", getExpectedRunsPerGame()));

        for (LeagueStructure.League league : site.getLeagueStructure().getLeagues()) {
            w.println(league.getName());

            for (LeagueStructure.Division division : league.getDivisions()) {
                if (!Strings.isNullOrEmpty(division.getName())) {
                    w.println(division.getName());
                }

                Set<TeamScore> divisionScores = Sets.newHashSet();

                for (Id<Team> team : division.getTeams()) {
                    divisionScores.add(getScore(team));
                }

                for (TeamScore s : getOrdering().sortedCopy(divisionScores)) {
                    printTeamScore(s, w);
                }

                w.println();
            }
        }

        w.println(String.format("%-20s | %11s %5.1f | %11s %5.1f |", "", "", mlHitting.getMean(), "", mlPitching.getMean()));

        w.flush();
    }

    private void printTeamScore(TeamScore s, PrintWriter w) {
        Record current = site.getStandings().getRecord(s.getId());
        Record eos = getExpectedEndOfSeason(s.getId());

        w.println(
            String.format(
                "%-20s | %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f | %s %.3f | %3d-%3d %.3f | %3d-%3d %.3f ",
                StringUtils.abbreviate(site.getSingleTeam(s.getId()).getName(), 20),
                s.getBatting(),
                s.getLineup(),
                s.getOverallBatting(),
                s.getPitching(),
                s.getRotation(),
                s.getOverallPitching(),
                s.getExpectedReocrd(getExpectedRunsPerGame()),
                s.getExpectedWinningPercentage(getExpectedRunsPerGame()),
                current.getWins(),
                current.getLosses(),
                current.getWinPercentage(),
                eos.getWins(),
                eos.getLosses(),
                eos.getWinPercentage()
                ));

    }

    public Record getExpectedEndOfSeason(Id<Team> team) {
        Standings standings = site.getStandings();

        Record current = standings.getRecord(team);

        Long eosWs = current.getWins()
            + Math.round(
                getScore(team)
                    .getExpectedWinningPercentage(getExpectedRunsPerGame())
                    * (162 - current.getGames()));

        Long eosLs = 162 - eosWs;

        return Record.create(eosWs, eosLs);
    }

    private Set<TeamScore> normalize(Iterable<TeamScore> scores) {
        Double battingAverage = getBattingAverage(scores);
        Double lineupAverage = getLineupAverage(scores);
        Double pitchingAverage = getPitchingAverage(scores);
        Double rotationAverage = getRotationAverage(scores);

        Set<TeamScore> normalized = Sets.newHashSet();

        for (TeamScore s : scores) {
            normalized.add(
                TeamScore.create(
                    s.getId(),
                    normalize(s.getBatting(), battingAverage),
                    normalize(s.getLineup(), lineupAverage),
                    normalize(s.getPitching(), pitchingAverage),
                    normalize(s.getRotation(), rotationAverage)));
        }

        return normalized;
    }

    private Double normalize(Double value, Double average) {
        return value * 100 / average;
    }

    private Double getBattingAverage(Iterable<TeamScore> scores) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (TeamScore s : scores) {
            stats.addValue(s.getBatting());
        }

        return stats.getMean();

    }

    private Double getLineupAverage(Iterable<TeamScore> scores) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (TeamScore s : scores) {
            stats.addValue(s.getLineup());
        }
        return stats.getMean();
    }

    private Double getPitchingAverage(Iterable<TeamScore> scores) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (TeamScore s : scores) {
            stats.addValue(s.getPitching());
        }
        return stats.getMean();
    }

    private Double getRotationAverage(Iterable<TeamScore> scores) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (TeamScore s : scores) {
            stats.addValue(s.getRotation());
        }
        return stats.getMean();
    }

    private TeamScore calculate(Id<Team> id) {
        return calculate(id, site.getSingleTeam(id).getRoster().getAllPlayers());
    }

    private TeamScore calculate(Id<Team> id, Iterable<Player> players) {
        return TeamScore.create(
            id,
            calculateBatting(players),
            calculateLineup(players),
            calculatePitching(players),
            calculateRotation(players));
    }

    private Double calculateBatting(Iterable<Player> players) {
        // TODO:
        // Try using SlotSelection to select just 9 players
        return calculateScore(
            HitterSelectionFactory.using(value),
            Selections.onlyHitters(players));
    }

    private Double calculateLineup(Iterable<Player> players) {
        return calculateScore(
            SlotSelection
                .builder()
                .ordering(Ordering.natural().reverse().onResultOf(value))
                .slots(ImmutableMultiset.of(Slot.C, Slot.SS, Slot.IF, Slot.IF, Slot.CF, Slot.OF, Slot.OF, Slot.H, Slot.H))
                .size(9)
                .fillToSize(Slot.H)
                .build(),
            Selections.onlyHitters(players));
    }

    private Double calculatePitching(Iterable<Player> players) {
        return calculateScore(
            PitcherSelectionFactory.using(value, site.getPitcherSelectionMethod()),
            Selections.onlyPitchers(players));
    }

    private Double calculateRotation(Iterable<Player> players) {
        return calculateScore(
            SlotSelection
                .builder()
                .ordering(Ordering.natural().reverse().onResultOf(value))
                .slots(ImmutableMultiset.of(Slot.SP, Slot.SP, Slot.SP, Slot.SP, Slot.MR, Slot.MR, Slot.MR))
                .size(7)
                .fillToSize(Slot.P)
                .build(),
            Selections.onlyPitchers(players));
    }

    private Double calculateScore(
        SelectionFactory selection, Iterable<Player> players) {

        return calculateScore(selection.create(Mode.REGULAR_SEASON), players);
    }

    private Double calculateScore(Selection selection, Iterable<Player> players) {
        Iterable<Player> ml = selection
            .select(ImmutableSet.<Player>of(), players)
            .values();

        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (Player p : ml) {
            stats.addValue(value.apply(p));
            addToAverages(p);
        }

        return stats.getMean();
    }

    private void addToAverages(Player p) {
        if (Selections.isHitter(p)) {
            mlHitting.addValue(value.apply(p));
        }

        if (Selections.isPitcher(p)) {
            mlPitching.addValue(value.apply(p));
        }
    }

    public static TeamReport create(String title, Site site, Function<Player, Integer> value) {
        return new TeamReport(title, site, value);
    }

    private static final class TeamScore {

        private final Id<Team> id;

        private final Double batting;

        private final Double lineup;

        private final Double pitching;

        private final Double rotation;

        private TeamScore(Id<Team> id, Double batting, Double lineup, Double pitching, Double rotation) {
            this.id = id;
            this.batting = batting;
            this.lineup = lineup;
            this.pitching = pitching;
            this.rotation = rotation;
        }

        public Id<Team> getId() {
            return id;
        }

        public Double getBatting() {
            return batting;
        }

        public Double getLineup() {
            return lineup;
        }

        public Double getPitching() {
            return pitching;
        }

        public Double getRotation() {
            return rotation;
        }

        public Double getOverallBatting() {
            return combine(batting, lineup);
        }

        public Double getOverallPitching() {
            return combine(pitching, rotation);
        }

        private Double combine(Double depth, Double strength) {
            double a = .5 * depth / 100;
            double b = .5 * 100 / strength;

            double log5 = (a - a*b) / (a + b - 2*a*b);

            return 100 * log5 / .5;
        }

        /**
         * Assumption - normalized to an average of 100
         * @return
         */
        public Double getExpectedWinningPercentage(double rpg) {
            int averageRuns = (int) (rpg * 162);

            double rs = getOverallBatting() * averageRuns / 100;
            double ra = (100.0 / getOverallPitching() * 100) * averageRuns / 100;

            return 1.0 / (1.0 + Math.pow(ra / rs, 2));
        }

        public String getExpectedReocrd(double rpg) {
            Long wins = Math.round(162 * getExpectedWinningPercentage(rpg));

            return String.format("%3d-%3d", wins, 162-wins);
        }

        public static TeamScore create(
            Id<Team> id, Double batting, Double lineup, Double pitching, Double rotation) {

            return new TeamScore(id, batting, lineup, pitching, rotation);
        }

        public static Ordering<TeamScore> byWinningPercentage(final double rpg) {
            return Ordering
                .natural()
                .reverse()
                .onResultOf(new Function<TeamScore, Double>() {
                    @Override
                    public Double apply(TeamScore score) {
                        Assertions.assertThat(score).isNotNull();

                        return score.getExpectedWinningPercentage(rpg);
                    }
                });
        }

    }

}

