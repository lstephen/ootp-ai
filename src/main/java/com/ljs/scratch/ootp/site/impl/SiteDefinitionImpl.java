package com.ljs.scratch.ootp.site.impl;

import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.PitcherOverall;
import com.ljs.scratch.ootp.team.TeamId;

/**
 *
 * @author lstephen
 */
public final class SiteDefinitionImpl implements SiteDefinition {

    private final Version type;

    private final String name;

    private final String siteRoot;

    private final TeamId team;

    private final String league;

    private final int nTeams;

    private SiteDefinitionImpl(
        Version type,
        String name,
        String siteRoot,
        TeamId team,
        String league,
        int nTeams) {

        this.type = type;
        this.name = name;
        this.siteRoot = siteRoot;
        this.team = team;
        this.league = league;
        this.nTeams = nTeams;
    }

    @Override
    public String getName() { return name; }

    @Override
    public TeamId getTeam() { return team; }

    @Override
    public String getSiteRoot() { return siteRoot; }

    @Override
    public Version getType() { return type; }

    @Override
    public String getLeague() { return league; }

    @Override
    public Integer getNumberOfTeams() { return nTeams; }

    @Override
    public PitcherOverall getPitcherSelectionMethod() {
        switch (type) {
            case OOTP5: return PitcherOverall.WOBA_AGAINST;
            case OOTP6: return PitcherOverall.FIP;
            default: throw new IllegalStateException();
        }
    }

    public static SiteDefinitionImpl ootp5(
        String name,
        String siteRoot,
        TeamId team,
        String league,
        int nTeams) {

        return new SiteDefinitionImpl(
            Version.OOTP5, name, siteRoot, team, league, nTeams);
    }

    public static SiteDefinitionImpl ootp6(
        String name,
        String siteRoot,
        TeamId team,
        String league,
        int nTeams) {

        return new SiteDefinitionImpl(
            Version.OOTP6, name, siteRoot, team, league, nTeams);
    }

}
