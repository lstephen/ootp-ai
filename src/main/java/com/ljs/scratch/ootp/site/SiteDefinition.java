package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.core.TeamId;
import com.ljs.scratch.ootp.stats.PitcherOverall;

/**
 *
 * @author lstephen
 */
public final class SiteDefinition {

    private final Version type;

    private final String name;

    private final String siteRoot;

    private final TeamId team;

    private final String league;

    private final int nTeams;

    private SiteDefinition(Version type, String name, String siteRoot, TeamId team, String league, int nTeams) {
        this.type = type;
        this.name = name;
        this.siteRoot = siteRoot;
        this.team = team;
        this.league = league;
        this.nTeams = nTeams;
    }

    public String getName() { return name; }
    public TeamId getTeam() { return team; }
    public String getSiteRoot() { return siteRoot; }
    public Version getType() { return type; }
    public String getLeague() { return league; }
    public int getNumberOfTeams() { return nTeams; }


    public PitcherOverall getPitcherSelectionMethod() {
        switch (type) {
            case OOTP5: return PitcherOverall.WOBA_AGAINST;
            case OOTP6: return PitcherOverall.FIP;
            default: throw new IllegalStateException();
        }
    }

    public static SiteDefinition ootp5(String name, String siteRoot, TeamId team, String league, int nTeams) {
        return new SiteDefinition(Version.OOTP5, name, siteRoot, team, league, nTeams);
    }

    public static SiteDefinition ootp6(String name, String siteRoot, TeamId team, String league, int nTeams) {
        return new SiteDefinition(Version.OOTP6, name, siteRoot, team, league, nTeams);
    }

}
