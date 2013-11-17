package com.ljs.ootp.ai.site.impl;

import com.ljs.ootp.ai.data.Id;
import com.ljs.ootp.ai.ootp5.Ootp5;
import com.ljs.ootp.ai.ootp5.site.PotentialRating;
import com.ljs.ootp.ai.ootp5.site.ZeroToTen;
import com.ljs.ootp.ai.ootp6.site.OneToTen;
import com.ljs.ootp.ai.ootp6.site.OneToTwenty;
import com.ljs.ootp.ai.ootp6.Ootp6;
import com.ljs.ootp.ai.ootp6.site.TwoToEight;
import com.ljs.ootp.ai.ootpx.OotpX;
import com.ljs.ootp.ai.rating.OneToOneHundred;
import com.ljs.ootp.ai.rating.Scale;
import com.ljs.ootp.ai.roster.Team;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.site.SiteDefinition;
import com.ljs.ootp.ai.site.Version;
import com.ljs.ootp.ai.stats.PitcherOverall;

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
        if (getName().equals("BTH") || getName().equals("PSD")) {
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
    public Scale<?> getPotentialRatingsScale() {
        if (type == Version.OOTP5) {
            return PotentialRating.scale();
        }
        if (type == Version.OOTPX) {
            return OneToOneHundred.scale();
        }

        if (name.equals("BTH")) {
            return OneToTen.scale();
        }

        if (name.equals("TWML")) {
            return TwoToEight.scale();
        }

        throw new IllegalArgumentException();
    }

    @Override
    public Scale<?> getAbilityRatingScale() {
        if (type == Version.OOTP5) {
            return ZeroToTen.scale();
        }
        if (type == Version.OOTPX) {
            return OneToOneHundred.scale();
        }

        if (name.equals("BTH")) {
            return OneToOneHundred.scale();
        }

        if (name.equals("TWML")) {
            return OneToTwenty.scale();
        }

        throw new IllegalArgumentException();
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
