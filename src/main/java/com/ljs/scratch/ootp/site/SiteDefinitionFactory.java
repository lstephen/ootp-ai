package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.data.Id;
import com.ljs.scratch.ootp.roster.Team;
import com.ljs.scratch.ootp.site.impl.SiteDefinitionImpl;

/**
 *
 * @author lstephen
 */
public final class SiteDefinitionFactory {

    private SiteDefinitionFactory() { }

    public static SiteDefinition ootp5(
        String name,
        String siteRoot,
        Id<Team> team,
        String league,
        int nTeams) {

        return SiteDefinitionImpl.ootp5(name, siteRoot, team, league, nTeams);
    }

    public static SiteDefinition ootp6(
        String name,
        String siteRoot,
        Id<Team> team,
        String league,
        int nTeams) {

        return SiteDefinitionImpl.ootp6(name, siteRoot, team, league, nTeams);
    }

    public static SiteDefinition ootpx(
        String name,
        String siteRoot,
        Id<Team> team,
        String league,
        int nTeams) {

        return SiteDefinitionImpl.ootpx(name, siteRoot, team, league, nTeams);
    }

}
