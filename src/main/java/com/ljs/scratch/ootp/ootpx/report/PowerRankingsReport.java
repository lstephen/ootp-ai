package com.ljs.scratch.ootp.ootpx.report;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.elo.EloRatings;
import com.ljs.scratch.ootp.elo.GameResult;
import com.ljs.scratch.ootp.io.Printable;
import com.ljs.scratch.ootp.ootpx.site.BoxScores;
import com.ljs.scratch.ootp.report.TeamReport;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.Record;
import com.ljs.scratch.ootp.site.Site;
import java.io.PrintWriter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 *
 * @author lstephen
 */
public class PowerRankingsReport implements Printable {

    private final Site site;

    private final TeamReport teamReport;

    private final EloRatings ratings = EloRatings.create();

    public PowerRankingsReport(Site site, TeamReport teamReport) {
        this.site = site;
        this.teamReport = teamReport;
    }


    @Override
    public void print(PrintWriter w) {
        populateInitialRatings();

        LocalDate now = site.getDate();

        for (BoxScores scores : BoxScores.create(site, new LocalDate(now.getYear(), DateTimeConstants.APRIL, 1), now)) {
            for (GameResult result : scores.getResults()) {
                ratings.update(result);
            }
        }

        Iterable<Id<Team>> ids = Ordering
            .natural()
            .reverse()
            .onResultOf(
                new Function<Id<Team>, Long>() {
                    public Long apply(Id<Team> id) {
                        return ratings.get(id);
                    }
                })
            .sortedCopy(site.getTeamIds());

        w.println();
        w.println("** Power Rankings **");

        for (Id<Team> id : ids) {
            Record current = site.getStandings().getRecord(id);

            w.println(
                String.format(
                    "%-20s | %3d-%3d %.3f | %4d ",
                    StringUtils.abbreviate(site.getSingleTeam(id).getName(), 20),
                    current.getWins(),
                    current.getLosses(),
                    current.getWinPercentage(),
                    ratings.get(id)
                ));

        }
    }

    private void populateInitialRatings() {
        for (Id<Team> team : site.getTeamIds()) {
            Double we = teamReport.getExpectedEndOfSeason(team).getWinPercentage();

            Long elo = 1500 + Math.round(
                (400 * Math.log((double) -we / (we-1)))
                / Math.log(10));

            ratings.setRating(team, elo);
        }
    }

    public static PowerRankingsReport create(Site site, TeamReport teamReport) {
        return new PowerRankingsReport(site, teamReport);
    }

}
