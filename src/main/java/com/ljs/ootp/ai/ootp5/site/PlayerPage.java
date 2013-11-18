package com.ljs.ootp.ai.ootp5.site;

import com.google.common.collect.ImmutableMap;
import com.ljs.ootp.ai.player.ratings.BattingRatings;
import com.ljs.ootp.ai.rating.Rating;
import com.ljs.ootp.ai.rating.Scale;
import com.ljs.ootp.ai.site.Site;
import static com.ljs.ootp.ai.site.Version.OOTP5;
import static com.ljs.ootp.ai.site.Version.OOTP6;
import com.ljs.ootp.ai.splits.Splits;
import javax.annotation.Nonnull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class PlayerPage {

    private static enum BattingRatingsType { CONTACT, GAP, POWER, EYE }

    private static final ImmutableMap<BattingRatingsType, Integer> OOTP6_HITTING =
        ImmutableMap.of(
            BattingRatingsType.CONTACT, 1,
            BattingRatingsType.GAP, 2,
            BattingRatingsType.POWER, 3,
            BattingRatingsType.EYE, 4);

    private static final ImmutableMap<BattingRatingsType, Integer> OOTP5_HITTING =
        ImmutableMap.of(
            BattingRatingsType.CONTACT, 1,
            BattingRatingsType.GAP, 2,
            BattingRatingsType.POWER, 4,
            BattingRatingsType.EYE, 5);

    private static final ImmutableMap<String, Integer> OOTP5_POTENTIAL =
        ImmutableMap.of(
            "Poor", 1,
            "Fair", 3,
            "Average", 5,
            "Good", 7,
            "Brilliant", 9);

    @Nonnull
    private final Document html;

    @Nonnull
    private final Site site;

    private PlayerPage(Document html, Site site) {
        this.html = html;
        this.site = site;
    }

    public Splits<BattingRatings<?>> extractBattingRatings() {
        Elements ratings = html.select("tr:has(td:contains(Batting Ratings)) + tr");

        if (ratings.isEmpty()) {
            ratings = html.select("tr:has(td:contains(Ratings)) + tr");
        }

        Elements vsLhp = ratings.select("tr.g:has(td:contains(LHP)), tr.g2:has(td:contains(LHP))");
        Elements vsRhp = ratings.select("tr.g:has(td:contains(RHP)), tr.g2:has(td:contains(RHP))");


        return Splits.<BattingRatings<?>>create(
            extractBattingRatings(vsLhp.first(), site.getAbilityRatingScale()),
            extractBattingRatings(vsRhp.first(), site.getAbilityRatingScale()));
    }

    public BattingRatings extractBattingPotential() {
        Elements ratingsEls = html.select("tr:has(td:contains(Batting Ratings)) + tr");

        if (ratingsEls.isEmpty()) {
            ratingsEls = html.select("tr:has(td:contains(Ratings)) + tr");
        }

        Elements potential = ratingsEls.select("tr.g:has(td:contains(Talent))");

        return extractBattingRatings(potential.first(), site.getPotentialRatingScale());
    }

    public <T> BattingRatings<T> extractBattingRatings(Element el, Scale<T> scale) {

        ImmutableMap<BattingRatingsType, Integer> idx;
        switch (site.getType()) {
            case OOTP5:
                idx = OOTP5_HITTING;
                break;
            case OOTP6:
                idx = OOTP6_HITTING;
                break;
            default:
                throw new IllegalStateException();
        }

        Elements line = el.children();

        return BattingRatings
            .builder(scale)
            .contact(line.get(idx.get(BattingRatingsType.CONTACT)).text())
            .gap(line.get(idx.get(BattingRatingsType.GAP)).text())
            .power(line.get(idx.get(BattingRatingsType.POWER)).text())
            .eye(line.get(idx.get(BattingRatingsType.EYE)).text())
            .build();
    }

    private Rating<?, ?>
        getOotp5Potential(Elements els, int idx) {

        return site.getPotentialRatingScale().parse(els.get(idx).text());
    }

    private Rating<?, ?> parseOotp6PotentialRating(String s) {
        site.getPotentialRatingScale();
        return site.getPotentialRatingScale().parse(s);
    }


    public static PlayerPage create(Document html, Site site) {
        return new PlayerPage(html, site);
    }

}
