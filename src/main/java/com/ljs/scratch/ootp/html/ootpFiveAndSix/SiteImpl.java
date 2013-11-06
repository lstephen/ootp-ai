package com.ljs.scratch.ootp.html.ootpFiveAndSix;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.TeamPitching;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.html.page.PageFactory;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.roster.Roster;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.stats.PitchingStats;
import java.util.Arrays;
import java.util.List;
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
        return getLeagueBattingPage().extractDate();
    }

    @Override
    public PitcherOverall getPitcherSelectionMethod() {
        return definition.getPitcherSelectionMethod();
    }

    @Override
    public Iterable<Id<Team>> getTeamIds() {
        List<Id<Team>> ids = Lists.newArrayList();

        for (int i = 1; i <= definition.getNumberOfTeams(); i++) {
            ids.add(Id.<Team>valueOf(i));
        }

        return ids;
    }

    @Override
    public Page getPage(String url, Object... args) {
        return PageFactory.create(definition.getSiteRoot(), String.format(url, args));
    }

    @Override
    public Iterable<Player> getDraft() {
        return PlayerList.draft(this).extract();
    }

    @Override
    public Iterable<Player> getFreeAgents() {
        return PlayerList.freeAgents(this).extract();
    }

    @Override
    public BattingStats getLeagueBatting() {
        return getLeagueBattingPage().extractTotal();
    }

    private LeagueBatting getLeagueBattingPage() {
        return new LeagueBatting(this, definition.getLeague());
    }

    @Override
    public PitchingStats getLeaguePitching() {
        return new LeaguePitching(this, definition.getLeague()).extractTotal();
    }

    @Override
    public Iterable<Player> getRuleFiveDraft() {
        return PlayerList.ruleFiveDraft(this).extract();
    }

    @Override
    public Iterable<Player> getSalariedPlayers(Id<Team> id) {
        return getSalary(id).getSalariedPlayers();
    }

    @Override
    public SalaryImpl getSalary() {
        return getSalary(definition.getTeam());
    }

    @Override
    public SalaryImpl getSalary(Id<Team> id) {
        return new SalaryImpl(this, id);
    }

    @Override
    public SalaryImpl getSalary(int teamId) {
        return getSalary(Id.<Team>valueOf(Integer.toString(teamId)));
    }

    @Override
    public String getSalary(Player p) {
        for (Id<Team> id : getTeamIds()) {
            String salary = getSalary(id).getSalary(p);

            if (salary != null) {
                return salary;
            }
        }
        return "";
    }

    @Override
    public Integer getCurrentSalary(Player p) {
        for (Id<Team> id : getTeamIds()) {
            Integer salary = getSalary(id).getCurrentSalary(p);

            if (salary != 0) {
                return salary;
            }
        }
        return 0;
    }

    @Override
    public Optional<Integer> getTeamTopProspectPosition(PlayerId id) {
        for (Id<Team> team : getTeamIds()) {
            Optional<Integer> pos = getTopProspects(team).getPosition(id);

            if (pos.isPresent()) {
                return pos;
            }
        }
        return Optional.absent();
    }

    @Override
    public SingleTeamImpl getSingleTeam() {
        return getSingleTeam(definition.getTeam());
    }

    @Override
    public SingleTeamImpl getSingleTeam(Id<Team> id) {
        return new SingleTeamImpl(this, id);
    }

    @Override
    public SingleTeamImpl getSingleTeam(int teamId) {
        return getSingleTeam(Id.<Team>valueOf(teamId));
    }

    @Override
    public StandingsImpl getStandings() {
        return StandingsImpl.create(this);
    }

    @Override
    public TeamBattingImpl getTeamBatting() {
        return new TeamBattingImpl(this, definition.getTeam());
    }

    @Override
    public TeamPitching getTeamPitching() {
        return new TeamPitchingImpl(this, definition.getTeam());
    }

    public TopProspects getTopProspects(Integer teamId) {
        return getTopProspects(Id.<Team>valueOf(teamId));
    }

    public TopProspects getTopProspects(Id<Team> id) {
        return TopProspects.of(this, id);
    }

    @Override
    public Player getPlayer(PlayerId id) {
        return new SinglePlayer(this, id).extract();
    }

    @Override
    public Iterable<Player> getWaiverWire() {
        return PlayerList.waiverWire(this).extract();
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
                return getPlayer(id);
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

            for (Id<Team> id : getTeamIds()) {
                Iterables.addAll(is, getSingleTeam(id).extractInjuries());
            }

            injured = ImmutableSet.copyOf(is);
        }

        return injured.contains(p.getId());
    }

    @Override
    public Team extractTeam() {
        return new TeamRatings(this, definition.getTeam()).extractTeam();
    }

    @Override
    public Roster extractRoster() {
        return getSingleTeam().getRoster();
    }

    public static Site create(SiteDefinition definition) {
        return new SiteImpl(definition);
    }

}
