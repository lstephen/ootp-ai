package com.ljs.scratch.ootp.site;

import com.ljs.scratch.ootp.site.impl.SiteDefinitionImpl;
import com.ljs.scratch.ootp.team.TeamId;

/**
 *
 * @author lstephen
 */
public final class SiteDefinitionFactory {

    private SiteDefinitionFactory() { }

    public static SiteDefinition ootp5(
        String name,
        String siteRoot,
        TeamId team,
        String league,
        int nTeams) {

        return SiteDefinitionImpl.ootp5(name, siteRoot, team, league, nTeams);
    }

    public static SiteDefinition ootp6(
        String name,
        String siteRoot,
        TeamId team,
        String league,
        int nTeams) {

        return SiteDefinitionImpl.ootp6(name, siteRoot, team, league, nTeams);
    }

}
