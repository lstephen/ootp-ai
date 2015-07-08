package com.ljs.ootp.ai.site.impl;

import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.SiteDefinition;
import com.ljs.ootp.ai.site.impl.SiteDefinitionImpl;

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

}
