package com.ljs.ootp.ai.ootpx.report;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.elo.EloRatings;
import com.ljs.ootp.ai.elo.GameResult;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.ootpx.site.BoxScores;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Record;
import com.ljs.ootp.ai.site.RecordPredictor;
import com.ljs.ootp.ai.site.Site;
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

    private final RecordPredictor recordPredictor;

    private final EloRatings ratings = EloRatings.create();

    public PowerRankingsReport(Site site, RecordPredictor recordPredictor) {
        this.site = site;
        this.recordPredictor = recordPredictor;
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
            Double we = recordPredictor.getExpectedEndOfSeason(team).getWinPercentage();

            Long elo = 1500 + Math.round(
                (400 * Math.log((double) -we / (we-1)))
                / Math.log(10));

            ratings.setRating(team, elo);
        }
    }

    public static PowerRankingsReport create(Site site, RecordPredictor recordPredictor) {
        return new PowerRankingsReport(site, recordPredictor);
    }

}
