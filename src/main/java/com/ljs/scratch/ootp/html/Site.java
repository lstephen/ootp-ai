package com.ljs.scratch.ootp.html;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.html.page.PageFactory;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import java.util.Arrays;
import java.util.Set;
import org.joda.time.LocalDate;

/**
 *
 * @author lstephen
 */
public class Site {

    private final SiteDefinition definition;

    private ImmutableSet<PlayerId> futureFas;

    private ImmutableSet<PlayerId> injured;

    private Site(SiteDefinition def) {
        this.definition = def;
    }

    public SiteDefinition getDefinition() {
        return definition;
    }
    
    public String getName() {
        return definition.getName();
    }

    public Version getType() {
        return definition.getType();
    }

    public LocalDate getDate() {
        return getLeagueBatting().extractDate();
    }

    public PitcherOverall getPitcherSelectionMethod() {
        return definition.getPitcherSelectionMethod();
    }

    public int getNumberOfTeams() {
        return definition.getNumberOfTeams();
    }

    public Page getPage(String url) {
        return PageFactory.create(definition.getSiteRoot(), url);
    }

    public PlayerList getDraft() {
        return PlayerList.draft(this);
    }

    public PlayerList getFreeAgents() {
        return PlayerList.freeAgents(this);
    }

    public LeagueBatting getLeagueBatting() {
        return new LeagueBatting(this, definition.getLeague());
    }

    public LeaguePitching getLeaguePitching() {
        return new LeaguePitching(this, definition.getLeague());
    }

    public MinorLeagues getMinorLeagues() {
        return getMinorLeagues(definition.getTeam());
    }

    public MinorLeagues getMinorLeagues(Id<Team> id) {
        return new MinorLeagues(this, id);
    }

    public PlayerList getRuleFiveDraft() {
        return PlayerList.ruleFiveDraft(this);
    }

    public Salary getSalary() {
        return getSalary(definition.getTeam());
    }

    public Salary getSalary(Id<Team> id) {
        return new Salary(this, id);
    }

    public Salary getSalary(int teamId) {
        return getSalary(Id.<Team>valueOf(Integer.toString(teamId)));
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
        return getSingleTeam(definition.getTeam());
    }

    public SingleTeam getSingleTeam(Id<Team> id) {
        return new SingleTeam(this, id);
    }

    public SingleTeam getSingleTeam(int teamId) {
        return getSingleTeam(Id.<Team>valueOf(teamId));
    }

    public Standings getStandings() {
        return Standings.create(this);
    }

    public TeamBatting getTeamBatting() {
        return new TeamBatting(this, definition.getTeam());
    }

    public TeamPitching getTeamPitching() {
        return new TeamPitching(this, definition.getTeam());
    }

    public TeamRatings getTeamRatings() {
        return getTeamRatings(definition.getTeam());
    }

    public TeamRatings getTeamRatings(Integer teamId) {
        return getTeamRatings(Id.<Team>valueOf(teamId));
    }

    public TeamRatings getTeamRatings(Id<Team> id) {
        return new TeamRatings(this, id);
    }

    public TopProspects getTopProspects(Integer teamId) {
        return getTopProspects(Id.<Team>valueOf(teamId));
    }

    public TopProspects getTopProspects(Id<Team> id) {
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

    public static Site create(SiteDefinition definition) {
        return new Site(definition);
    }

}
