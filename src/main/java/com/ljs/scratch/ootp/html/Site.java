package com.ljs.scratch.ootp.html;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.html.page.PageFactory;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.roster.TeamId;
import java.util.Arrays;
import java.util.Set;
import org.joda.time.LocalDate;

/**
 *
 * @author lstephen
 */
public class Site {

    private final SiteDefinition def;

    private final TeamId team;

    private ImmutableSet<PlayerId> futureFas;

    private ImmutableSet<PlayerId> injured;

    public Site(SiteDefinition def) {
        this.def = def;
        this.team = def.getTeam();
    }

    public SiteDefinition getDefinition() {
        return def;
    }
    
    public String getName() {
        return def.getName();
    }

    public Version getType() {
        return def.getType();
    }

    public LocalDate getDate() {
        return getLeagueBatting().extractDate();
    }

    public PitcherOverall getPitcherSelectionMethod() {
        return def.getPitcherSelectionMethod();
    }

    public int getNumberOfTeams() {
        return def.getNumberOfTeams();
    }

    public Page getPage(String url) {
        return PageFactory.create(def.getSiteRoot(), url);
    }

    public PlayerList getDraft() {
        return PlayerList.draft(this);
    }

    public PlayerList getFreeAgents() {
        return PlayerList.freeAgents(this);
    }

    public LeagueBatting getLeagueBatting() {
        return new LeagueBatting(this, def.getLeague());
    }

    public LeaguePitching getLeaguePitching() {
        return new LeaguePitching(this, def.getLeague());
    }

    public MinorLeagues getMinorLeagues() {
        return getMinorLeagues(team);
    }

    public MinorLeagues getMinorLeagues(TeamId id) {
        return new MinorLeagues(this, id);
    }

    public PlayerList getRuleFiveDraft() {
        return PlayerList.ruleFiveDraft(this);
    }

    public Salary getSalary() {
        return getSalary(team);
    }

    public Salary getSalary(TeamId id) {
        return new Salary(this, id);
    }

    public Salary getSalary(int teamId) {
        return getSalary(new TeamId(Integer.toString(teamId)));
    }

    public String getSalary(Player p) {
        for (int i = 1; i <= getNumberOfTeams(); i++) {
            String salary = getSalary(i).getSalary(p);

            if (salary != null) {
                return salary;
            }
        }
        return "";
    }

    public Integer getCurrentSalary(Player p) {
        for (int i = 1; i <= getNumberOfTeams(); i++) {
            Integer salary = getSalary(i).getCurrentSalary(p);

            if (salary != 0) {
                return salary;
            }
        }
        return 0;
    }

    public Optional<Integer> getTeamTopProspectPosition(PlayerId id) {
        for (int i = 1; i <= getNumberOfTeams(); i++) {
            Optional<Integer> pos = getTopProspects(i).getPosition(id);

            if (pos.isPresent()) {
                return pos;
            }
        }
        return Optional.absent();
    }

    public SingleTeam getSingleTeam() {
        return getSingleTeam(team);
    }

    public SingleTeam getSingleTeam(TeamId id) {
        return new SingleTeam(this, id);
    }

    public SingleTeam getSingleTeam(int teamId) {
        return getSingleTeam(new TeamId(Integer.toString(teamId)));
    }

    public Standings getStandings() {
        return Standings.create(this);
    }

    public TeamBatting getTeamBatting() {
        return new TeamBatting(this, team);
    }

    public TeamPitching getTeamPitching() {
        return new TeamPitching(this, team);
    }

    public TeamRatings getTeamRatings() {
        return getTeamRatings(team);
    }

    public TeamRatings getTeamRatings(Integer teamId) {
        return getTeamRatings(new TeamId(Integer.toString(teamId)));
    }

    public TeamRatings getTeamRatings(TeamId id) {
        return new TeamRatings(this, id);
    }

    public TopProspects getTopProspects(Integer teamId) {
        return getTopProspects(new TeamId(teamId.toString()));
    }

    public TopProspects getTopProspects(TeamId id) {
        return TopProspects.of(this, id);
    }

    public SinglePlayer getPlayer(PlayerId id) {
        return new SinglePlayer(this, id);
    }

    public PlayerList getWaiverWire() {
        return PlayerList.waiverWire(this);
    }

    public Iterable<Player> getPlayers(PlayerId... ids) {
        return getPlayers(Arrays.asList(ids));
    }

    public Iterable<Player> getPlayers(Iterable<PlayerId> ids) {
        return Iterables.transform(ids, new Function<PlayerId, Player>() {
            @Override
            public Player apply(PlayerId id) {
                return getPlayer(id).extract();
            }
        });
    }

    public boolean isFutureFreeAgent(Player p) {
        if (futureFas == null) {
            futureFas =
                ImmutableSet.copyOf(
                    PlayerList.futureFreeAgents(this).extractIds());
        }

        return futureFas.contains(p.getId());
    }

    public Predicate<Player> isFutureFreeAgent() {
        return new Predicate<Player>() {
            @Override
            public boolean apply(Player p) {
                return isFutureFreeAgent(p);
            }
        };
    }

    public boolean isInjured(Player p) {
        if (injured == null) {
            Set<PlayerId> is = Sets.newHashSet();

            for (int i = 1; i <= getNumberOfTeams(); i++) {
                Iterables.addAll(is, getSingleTeam(i).extractInjuries());
            }

            injured = ImmutableSet.copyOf(is);
        }

        return injured.contains(p.getId());
    }

    public Team extractTeam() {
        return getTeamRatings().extractTeam();
    }

    public Roster extractRoster() {
        return getSingleTeam().extractRoster();
    }

}
