package com.ljs.scratch.ootp.report;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.value.TradeValue;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class TeamReport {

    private Site site;

    private TradeValue value;



    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {

        Set<TeamScore> scores = Sets.newHashSet();

        for (int i = 1; i <= site.getNumberOfTeams(); i++) {
            Iterable<Player> players = site.getSingleTeam(i).extractPlayers();

            TeamScore score = calculate(site.getSingleTeam(i).extractTeamName(), players);



        }

        scores = normalize(scores);





    }

    private Set<TeamScore> normalize(Iterable<TeamScore> scores) {
        return ImmutableSet.copyOf(scores);
    }

    public TeamScore calculate(String name, Iterable<Player> players) {
        TeamScore ts = new TeamScore(name);

        ts.calculateBatting(players);
        ts.calculatePitching(players);

        return ts;
    }

    private class TeamScore {

        private final String teamName;

        private Integer batting;

        private Integer pitching;

        private TeamScore(String teamName) {
            this.teamName = teamName;
        }

        private void calculateBatting(Iterable<Player> players) {
            //for (Player p : value.getOverallNow(p))

        }

        private void calculatePitching(Iterable<Player> players) {

        }

    }

}
