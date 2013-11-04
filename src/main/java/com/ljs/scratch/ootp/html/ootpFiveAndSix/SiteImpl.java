package com.ljs.scratch.ootp.html.ootpFiveAndSix;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
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
public final class SiteImpl implements Site {

    private final SiteDefinition definition;

    private ImmutableSet<PlayerId> futureFas;

    private ImmutableSet<PlayerId> injured;

    private SiteImpl(SiteDefinition def) {
        this.definition = def;
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
        return getLeagueBatting().extractDate();
    }

    @Override
    public PitcherOverall getPitcherSelectionMethod() {
        return definition.getPitcherSelectionMethod();
    }

    @Override
    public int getNumberOfTeams() {
        return definition.getNumberOfTeams();
    }

    @Override
    public Page getPage(String url) {
        return PageFactory.create(definition.getSiteRoot(), url);
    }

    @Override
    public PlayerList getDraft() {
        return PlayerList.draft(this);
    }

    @Override
    public PlayerList getFreeAgents() {
        return PlayerList.freeAgents(this);
    }

    @Override
    public LeagueBatting getLeagueBatting() {
        return new LeagueBatting(this, definition.getLeague());
    }

    @Override
    public LeaguePitching getLeaguePitching() {
        return new LeaguePitching(this, definition.getLeague());
    }

    @Override
    public MinorLeagues getMinorLeagues() {
        return getMinorLeagues(definition.getTeam());
    }

    @Override
    public MinorLeagues getMinorLeagues(Id<Team> id) {
        return new MinorLeagues(this, id);
    }

    @Override
    public PlayerList getRuleFiveDraft() {
        return PlayerList.ruleFiveDraft(this);
    }

    @Override
    public Salary getSalary() {
        return getSalary(definition.getTeam());
    }

    @Override
    public Salary getSalary(Id<Team> id) {
        return new Salary(this, id);
    }

    @Override
    public Salary getSalary(int teamId) {
        return getSalary(Id.<Team>valueOf(Integer.toString(teamId)));
    }

    @Override
    public String getSalary(Player p) {
        for (int i = 1; i <= getNumberOfTeams(); i++) {
            String salary = getSalary(i).getSalary(p);

            if (salary != null) {
                return salary;
            }
        }
        return "";
    }

    @Override
    public Integer getCurrentSalary(Player p) {
        for (int i = 1; i <= getNumberOfTeams(); i++) {
            Integer salary = getSalary(i).getCurrentSalary(p);

            if (salary != 0) {
                return salary;
            }
        }
        return 0;
    }

    @Override
    public Optional<Integer> getTeamTopProspectPosition(PlayerId id) {
        for (int i = 1; i <= getNumberOfTeams(); i++) {
            Optional<Integer> pos = getTopProspects(i).getPosition(id);

            if (pos.isPresent()) {
                return pos;
            }
        }
        return Optional.absent();
    }

    @Override
    public SingleTeam getSingleTeam() {
        return getSingleTeam(definition.getTeam());
    }

    @Override
    public SingleTeam getSingleTeam(Id<Team> id) {
        return new SingleTeam(this, id);
    }

    @Override
    public SingleTeam getSingleTeam(int teamId) {
        return getSingleTeam(Id.<Team>valueOf(teamId));
    }

    @Override
    public Standings getStandings() {
        return Standings.create(this);
    }

    @Override
    public TeamBatting getTeamBatting() {
        return new TeamBatting(this, definition.getTeam());
    }

    @Override
    public TeamPitching getTeamPitching() {
        return new TeamPitching(this, definition.getTeam());
    }

    @Override
    public TeamRatings getTeamRatings() {
        return getTeamRatings(definition.getTeam());
    }

    @Override
    public TeamRatings getTeamRatings(Integer teamId) {
        return getTeamRatings(Id.<Team>valueOf(teamId));
    }

    @Override
    public TeamRatings getTeamRatings(Id<Team> id) {
        return new TeamRatings(this, id);
    }

    @Override
    public TopProspects getTopProspects(Integer teamId) {
        return getTopProspects(Id.<Team>valueOf(teamId));
    }

    @Override
    public TopProspects getTopProspects(Id<Team> id) {
        return TopProspects.of(this, id);
    }

    @Override
    public SinglePlayer getPlayer(PlayerId id) {
        return new SinglePlayer(this, id);
    }

    @Override
    public PlayerList getWaiverWire() {
        return PlayerList.waiverWire(this);
    }

    @Override
    public Iterable<Player> getPlayers(PlayerId... ids) {
        return getPlayers(Arrays.asList(ids));
    }

    @Override
    public Iterable<Player> getPlayers(Iterable<PlayerId> ids) {
        return Iterables.transform(ids, new Function<PlayerId, Player>() {
            @Override
            public Player apply(PlayerId id) {
                return getPlayer(id).extract();
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

            for (int i = 1; i <= getNumberOfTeams(); i++) {
                Iterables.addAll(is, getSingleTeam(i).extractInjuries());
            }

            injured = ImmutableSet.copyOf(is);
        }

        return injured.contains(p.getId());
    }

    @Override
    public Team extractTeam() {
        return getTeamRatings().extractTeam();
    }

    @Override
    public Roster extractRoster() {
        return getSingleTeam().extractRoster();
    }

    public static Site create(SiteDefinition definition) {
        return new SiteImpl(definition);
    }

}
