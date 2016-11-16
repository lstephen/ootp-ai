package com.github.lstephen.ootp.ai.site.impl;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.ootp5.Ootp5;
import com.github.lstephen.ootp.ai.ootp6.Ootp6;
import com.github.lstephen.ootp.ai.rating.AToE;
import com.github.lstephen.ootp.ai.rating.OneToFive;
import com.github.lstephen.ootp.ai.rating.OneToOneHundred;
import com.github.lstephen.ootp.ai.rating.OneToTen;
import com.github.lstephen.ootp.ai.rating.OneToTwenty;
import com.github.lstephen.ootp.ai.rating.Potential;
import com.github.lstephen.ootp.ai.rating.Scale;
import com.github.lstephen.ootp.ai.rating.TwoToEight;
import com.github.lstephen.ootp.ai.rating.ZeroToTen;
import com.github.lstephen.ootp.ai.roster.Team;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.site.SiteDefinition;
import com.github.lstephen.ootp.ai.site.Version;

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
    public Double getYearlyRatingsIncrease() {
        if (getName().contains("BTH") || getName().equals("PSD")) {
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
            return new Potential();
        }

        if (name.contains("BTH")) {
            return new OneToTen();
        }

        if (name.equals("TWML")) {
            return new TwoToEight();
        }

        throw new IllegalArgumentException();
    }

    @Override
    public Scale<?> getAbilityRatingScale() {
        if (type == Version.OOTP5) {
            return new ZeroToTen();
        }

        if (name.contains("BTH")) {
            return new OneToOneHundred();
        }

        if (name.equals("TWML")) {
            return new OneToTwenty();
        }

        throw new IllegalArgumentException();
    }

    @Override
    public Scale<?> getBuntScale() {
      switch (type) {
        case OOTP5:
          return new AToE();
        case OOTP6:
          return new OneToFive();
        default:
          throw new IllegalStateException();
      }
    }

    @Override
    public Scale<?> getRunningScale() {
      switch (type) {
        case OOTP5:
          return new AToE();
        case OOTP6:
          return new OneToTen();
        default:
          throw new IllegalStateException();
      }
    }

    @Override
    public Site getSite() {
        switch (type) {
            case OOTP5:
                return Ootp5.create(this);
            case OOTP6:
                return Ootp6.create(this);
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

}
