package com.ljs.scratch.ootp.ootp5.site;

import com.google.common.collect.ImmutableMap;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.Splits;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.Version;
import static com.ljs.scratch.ootp.site.Version.OOTP5;
import static com.ljs.scratch.ootp.site.Version.OOTP6;
import com.ljs.scratch.util.ElementsUtil;
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

    public Splits<BattingRatings> extractBattingRatings() {
        Elements ratings = html.select("tr:has(td:contains(Batting Ratings)) + tr");

        if (ratings.isEmpty()) {
            ratings = html.select("tr:has(td:contains(Ratings)) + tr");
        }

        Elements vsLhp = ratings.select("tr.g:has(td:contains(LHP)), tr.g2:has(td:contains(LHP))");
        Elements vsRhp = ratings.select("tr.g:has(td:contains(RHP)), tr.g2:has(td:contains(RHP))");


        return Splits.<BattingRatings>create(
            extractBattingRatings(vsLhp.first()),
            extractBattingRatings(vsRhp.first()));
    }

    public BattingRatings extractBattingPotential() {
        Elements ratingsEls = html.select("tr:has(td:contains(Batting Ratings)) + tr");

        if (ratingsEls.isEmpty()) {
            ratingsEls = html.select("tr:has(td:contains(Ratings)) + tr");
        }

        Elements potential = ratingsEls.select("tr.g:has(td:contains(Talent))");

        if (site.getType() == Version.OOTP5) {
            Elements els = potential.get(0).children();

            return BattingRatings
                .builder()
                .contact(getOotp5Potential(els, OOTP5_HITTING.get(BattingRatingsType.CONTACT)))
                .gap(getOotp5Potential(els, OOTP5_HITTING.get(BattingRatingsType.GAP)))
                .power(getOotp5Potential(els, OOTP5_HITTING.get(BattingRatingsType.POWER)))
                .eye(getOotp5Potential(els, OOTP5_HITTING.get(BattingRatingsType.EYE)))
                .build();
        } else {
            BattingRatings unscaled = extractBattingRatings(potential.first());

            return BattingRatings
                .builder()
                .contact(scaleOotp6PotentialRating(unscaled.getContact()))
                .gap(scaleOotp6PotentialRating(unscaled.getGap()))
                .power(scaleOotp6PotentialRating(unscaled.getPower()))
                .eye(scaleOotp6PotentialRating(unscaled.getEye()))
                .build();
        }
    }

    public BattingRatings extractBattingRatings(Element el) {

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
            .builder()
            .contact(ElementsUtil.getInteger(line, idx.get(BattingRatingsType.CONTACT)))
            .gap(ElementsUtil.getInteger(line, idx.get(BattingRatingsType.GAP)))
            .power(ElementsUtil.getInteger(line, idx.get(BattingRatingsType.POWER)))
            .eye(ElementsUtil.getInteger(line, idx.get(BattingRatingsType.EYE)))
            .build();
    }

    private Integer getOotp5Potential(Elements els, int idx) {
        return PotentialRating.from(els.get(idx)).normalize().get() / 10;
    }

    private int scaleOotp6PotentialRating(int r) {
        if (site.getName().equals("BTH")) {
            // scale 1-10 to 1-100 scale
            return r * 10 - 5;
        }

        if (site.getName().equals("PSD")) {
            return r;
        }

        // scale 2-8 to 1-20 scale
        return r * 2 + (r - 5);
    }


    public static PlayerPage create(Document html, Site site) {
        return new PlayerPage(html, site);
    }

}
