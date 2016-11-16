package com.github.lstephen.ootp.ai.ootp5.site;

import static com.github.lstephen.ootp.ai.site.Version.OOTP5;
import static com.github.lstephen.ootp.ai.site.Version.OOTP6;

import com.github.lstephen.ootp.ai.player.ratings.BattingRatings;
import com.github.lstephen.ootp.ai.rating.Rating;
import com.github.lstephen.ootp.ai.rating.Scale;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.splits.Splits;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** @author lstephen */
public final class PlayerPage {

  private static enum BattingRatingsType {
    CONTACT,
    GAP,
    POWER,
    EYE,
    K
  }

  private static final ImmutableMap<BattingRatingsType, Integer> OOTP6_HITTING =
      ImmutableMap.of(
          BattingRatingsType.CONTACT, 1,
          BattingRatingsType.GAP, 2,
          BattingRatingsType.POWER, 3,
          BattingRatingsType.EYE, 4,
          BattingRatingsType.K, 5);

  private static final ImmutableMap<BattingRatingsType, Integer> OOTP5_HITTING =
      ImmutableMap.of(
          BattingRatingsType.CONTACT, 1,
          BattingRatingsType.GAP, 2,
          BattingRatingsType.POWER, 4,
          BattingRatingsType.EYE, 5,
          BattingRatingsType.K, 6);

  private static final ImmutableMap<String, Integer> OOTP5_POTENTIAL =
      ImmutableMap.of(
          "Poor", 1,
          "Fair", 3,
          "Average", 5,
          "Good", 7,
          "Brilliant", 9);

  @Nonnull private final Document html;

  @Nonnull private final Site site;

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
    Preconditions.checkNotNull(scale);

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

    return BattingRatings.builder(scale)
        .contact(line.get(idx.get(BattingRatingsType.CONTACT)).text())
        .gap(line.get(idx.get(BattingRatingsType.GAP)).text())
        .power(line.get(idx.get(BattingRatingsType.POWER)).text())
        .eye(line.get(idx.get(BattingRatingsType.EYE)).text())
        .k(line.get(idx.get(BattingRatingsType.K)).text())
        .runningSpeed(extractRunningSpeed().normalize())
        .stealingAbility(extractStealing().normalize())
        .build();
  }

  private Rating<?, ?> getOotp5Potential(Elements els, int idx) {

    return site.getPotentialRatingScale().parse(els.get(idx).text());
  }

  private Rating<?, ?> parseOotp6PotentialRating(String s) {
    return site.getPotentialRatingScale().parse(s);
  }

  public Rating<?, ?> extractBuntForHit() {
    return site.getBuntScale().parse(extractRunningText("Bunt for Hit"));
  }

  public Rating<?, ?> extractRunningSpeed() {
    return site.getRunningScale().parse(extractRunningText("Running Speed"));
  }

  public Rating<?, ?> extractStealing() {
    return site.getRunningScale().parse(extractRunningText("Stealing Ability"));
  }

  private String extractRunningText(String title) {
    return extractLabelledText("Bunting Ratings", title);
  }

  public String extractLabelledText(String category, String title) {
    String titles = html.select("td.s4:contains(" + category + ")").html();

    String[] splitTitles = StringUtils.splitByWholeSeparatorPreserveAllTokens(titles, "<br />");

    int idx = -1;

    for (int i = 0; i < splitTitles.length; i++) {
      if (splitTitles[i].trim().startsWith(title)) {
        idx = i;
        break;
      }
    }

    if (idx < 0) {
      throw new IllegalStateException("Could not find labelled text: " + category + "/" + title);
    }

    String raw = html.select("td.s4:contains(" + category + ") + td.s4").html();

    String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(raw, "<br />");

    return split[idx];
  }

  public static PlayerPage create(Document html, Site site) {
    return new PlayerPage(html, site);
  }
}
