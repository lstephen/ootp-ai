package com.ljs.scratch.ootp.site.impl;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.Ootp5;
import com.ljs.scratch.ootp.html.ootpFiveAndSix.Ootp6;
import com.ljs.scratch.ootp.html.ootpx.OotpX;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.ootp.stats.PitcherOverall;

/**
 *
 * @author lstephen
 */
public final class SiteDefinitionImpl implements SiteDefinition {

    private final Version type;

    private final String name;

    private final String siteRoot;

    private final Id<Team> team;

    private final String league;

    private final int nTeams;

    private SiteDefinitionImpl(
        Version type,
        String name,
        String siteRoot,
        Id<Team> team,
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
    public Id<Team> getTeam() { return team; }

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

    @Override
    public Double getYearlyRatingsIncrease() {
        if (getName().equals("BTH")) {
            return 8.0;
        }

        if (getName().equals("TWML")) {
            return 1.5;
        }

        return 1.0;
    }

    @Override
    public Boolean isFreezeOneRatings() {
        return type == Version.OOTP6;
    }

    @Override
    public Site getSite() {
        switch (type) {
            case OOTP5:
                return Ootp5.create(this);
            case OOTP6:
                return Ootp6.create(this);
            case OOTPX:
                return OotpX.create(this);
            default:
                throw new IllegalStateException();
        }
    }

    public static SiteDefinitionImpl ootp5(
        String name,
        String siteRoot,
        Id<Team> team,
        String league,
        int nTeams) {

        return new SiteDefinitionImpl(
            Version.OOTP5, name, siteRoot, team, league, nTeams);
    }

    public static SiteDefinitionImpl ootp6(
        String name,
        String siteRoot,
        Id<Team> team,
        String league,
        int nTeams) {

        return new SiteDefinitionImpl(
            Version.OOTP6, name, siteRoot, team, league, nTeams);
    }

    public static SiteDefinitionImpl ootpx(
        String name,
        String siteRoot,
        Id<Team> team,
        String league,
        int nTeams) {

        return new SiteDefinitionImpl(
            Version.OOTPX, name, siteRoot, team, league, nTeams);

    }

}
