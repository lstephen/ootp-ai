package com.ljs.ootp.ai.ootp5.site;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.ljs.ootp.ai.player.BattingHand;
import com.ljs.ootp.ai.player.Clutch;
import com.ljs.ootp.ai.player.Consistency;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.player.PlayerSource;
import com.ljs.ootp.ai.player.ratings.DefensiveRatings;
import com.ljs.ootp.ai.player.ratings.FieldingRatings;
import com.ljs.ootp.ai.player.ratings.PitchingRatings;
import com.ljs.ootp.ai.player.ratings.PlayerRatings;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.ai.player.ratings.StarRating;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.site.Version;
import com.ljs.ootp.ai.splits.Splits;
import com.ljs.ootp.extract.html.rating.Rating;
import com.ljs.ootp.extract.html.rating.Scale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class SinglePlayer implements PlayerSource {

  private static final Logger LOG = Logger.getLogger(SinglePlayer.class.getName());

  private static enum BattingRatingsType { CONTACT, GAP, POWER, EYE }

  private static enum PitchingRatingsType { HITS, GAP, TRIPLES, STUFF, CONTROL, MOVEMENT }

  private static final ImmutableMap<Position, Double> AVERAGE_FPCT =
    ImmutableMap
      .<Position, Double>builder()
      .put(Position.CATCHER, .952)
      .put(Position.FIRST_BASE, .993)
      .put(Position.SECOND_BASE, .981)
      .put(Position.THIRD_BASE, .953)
      .put(Position.SHORTSTOP, .967)
      .put(Position.LEFT_FIELD, .977)
      .put(Position.CENTER_FIELD, .984)
      .put(Position.RIGHT_FIELD, .981)
      .build();

  private static final ImmutableMap<PitchingRatingsType, Integer> OOTP6_PITCHING =
    ImmutableMap.<PitchingRatingsType, Integer>builder()
      .put(PitchingRatingsType.HITS, 1)
      .put(PitchingRatingsType.GAP, 3)
      .put(PitchingRatingsType.STUFF, 1)
      .put(PitchingRatingsType.CONTROL, 2)
      .put(PitchingRatingsType.MOVEMENT, 3)
      .build();

  private static final ImmutableMap<PitchingRatingsType, Integer> OOTP5_PITCHING =
    ImmutableMap.<PitchingRatingsType, Integer>builder()
      .put(PitchingRatingsType.HITS, 2)
      .put(PitchingRatingsType.GAP, 3)
      .put(PitchingRatingsType.STUFF, 6)
      .put(PitchingRatingsType.CONTROL, 5)
      .put(PitchingRatingsType.MOVEMENT, 4)
      .build();

  private Site site;

  private SalarySource salaries;

  public void setSite(Site site) {
    this.site = site;
  }

  public void setSalarySource(SalarySource salaries) {
    this.salaries = salaries;
  }

  private Document loadPage(PlayerId id) {
    return site.getPage(id.unwrap() + ".html").load();
  }

  @Override
  public Player get(PlayerId id) {
    try {
      return extract(id, loadPage(id));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Player not found. ID: {0}", id);
      return null;
    }
  }

  private Player extract(PlayerId id, Document doc) {
    Elements title = doc.select("title");

    String team = CharMatcher.WHITESPACE.trimAndCollapseFrom(
        StringUtils.substringBefore(StringUtils.substringAfterLast(title.text(), ","), "-"),
        ' ');

    Elements info = doc.select("td.s4:has(b:contains(Name)) + td.s4");

    String[] splitInfo =
      StringUtils.splitByWholeSeparatorPreserveAllTokens(
          info.html(), "<br />");

    if (splitInfo.length < 9) {
      LOG.log(Level.WARNING, "Error extracting player. ID: {0}", id);
      return null;
    }

    String name = Parser.unescapeEntities(splitInfo[0], false);
    Integer age = Integer.valueOf(splitInfo[3]);


    String listedPosition = getListedPosition(splitInfo[8]);

    PlayerPage page = PlayerPage.create(doc, site);

    PlayerRatings ratings =
      PlayerRatings.create(
          page.extractBattingRatings(),
          extractDefensiveRatings(doc),
          extractPitchingRatings(doc),
          site.getDefinition());

    ratings.setBattingPotential(page.extractBattingPotential());

    if (ratings.hasPitching()) {
      ratings.setPitchingPotential(extractPitchingPotential(doc));
    }

    Player player = Player.create(id, name, ratings);

    player.setAge(age);
    player.setTeam(team);
    player.setListedPosition(listedPosition);
    player.setBattingHand(BattingHand.fromCode(splitInfo[9]));

    player.setStars(StarRating.extractFrom(doc));
    player.setClutch(Clutch.extractFrom(doc));
    player.setConsistency(Consistency.extractFrom(doc));

    if (site.isInjured(player)) {
      player.setInjured(Boolean.TRUE);
    }

    if (doc.html().contains("out for entire career")) {
      player.setTeam("*CEI* " + player.getTeam());
    }

    if (site.isFutureFreeAgent(player)) {
      player.setUpcomingFreeAgent(Boolean.TRUE);
    }

    if (doc.html().contains("Rule 5 Draft Eligibility")) {
      player.setRuleFiveEligible(isRuleFiveEligible(doc));
    }

    if (doc.html().contains("Rule 5 Draft Eligibility")) {
      player.setOn40Man(doc.html().contains("on 40 Man Roster"));
    }

    if (doc.html().contains("Minor League Option Years")) {
      if (!(site.getName().equals("BTH") || site.getName().contains("OLD_BTH"))) {
        player.setOutOfOptions(doc.html().contains("Out of Option Years"));
      }
    }

    if (doc.html().contains("Minor League Option Years")) {
      if (!(site.getName().equals("BTH") || site.getName().contains("OLD_BTH"))) {
        player.setClearedWaivers(doc.html().contains("Waivers cleared"));
      }
    }

    if (doc.html().contains("Years of Pro Service")) {
      player.setYearsOfProService(getYearsOfProService(doc));
    }

    Optional<Integer> teamTopProspectPosition =
      site.getTeamTopProspectPosition(id);

    if (teamTopProspectPosition.isPresent()) {
      player.setTeamTopProspectPosition(teamTopProspectPosition.get());
    }


    player.setSalary(salaries.getSalary(player));

    return player;
  }

  private String getListedPosition(String src) {
    String p = CharMatcher.WHITESPACE.trimFrom(src);

    ImmutableMap<String, String> ps = ImmutableMap
      .<String, String>builder()
      .put("Starting Pitcher", "SP")
      .put("Pitcher", "P")
      .put("Reliever", "MR")
      .put("Closer", "CL")
      .put("Catcher", "C")
      .put("First Base", "1B")
      .put("Second Base", "2B")
      .put("Third Base", "3B")
      .put("Shortstop", "SS")
      .put("Leftfield", "LF")
      .put("Centerfield", "CF")
      .put("Rightfield", "RF")
      .put("Designated Hitter", "DH")
      .build();

    if (!ps.containsKey(p)) {
      throw new IllegalStateException("Unknown Position:" + p);
    }

    return ps.get(p);
  }

  private Boolean isRuleFiveEligible(Document doc) {
    return extractContractText(doc, "Rule 5 Draft Eligibility :").contains("Eligible");
  }

  private Integer getYearsOfProService(Document doc) {
    String raw = extractContractText(doc, "Years of Pro Service :");

    return Integer.parseInt(StringUtils.substringBefore(raw, " "));
  }

  private String extractContractText(Document doc, String title) {
    String titles = doc.select("td.s4:contains(Contract)").html();

    String[] splitTitles = StringUtils.splitByWholeSeparatorPreserveAllTokens(titles, "<br />");

    int idx = -1;

    for (int i = 0; i < splitTitles.length; i++) {
      if (splitTitles[i].equals(title)) {
        idx = i;
        break;
      }
    }

    if (idx < 0) {
      return null;
    }

    String raw = doc.select("td.s4:contains(Contract) + td.s4").html();

    String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(raw, "<br />");

    return split[idx];
  }

  private DefensiveRatings extractDefensiveRatings(Document doc) {
    DefensiveRatings ratings = new DefensiveRatings();

    String raw = doc.select("td.s4:contains(Fielding Ratings)").text();

    for (Position p : Position.values()) {
      if (raw.contains(p.getAbbreviation() + " :")) {
        Integer rating = extractPositionRating(raw, p.getAbbreviation());

        if (p == Position.CATCHER && rating == 0) {
          rating = 0;
        }
        ratings.setPositionRating(p, (double) rating);
      }
    }

    ratings.setCatcher(extractCatcherRating(raw));
    ratings.setInfield(extractInfieldRating(raw));
    ratings.setOutfield(extractOutfieldRating(raw));

    return ratings;
  }

  private Splits<PitchingRatings<?>> extractPitchingRatings(Document doc) {
    Elements vsLhb = doc.select("tr.g:has(td:contains(Versus LHB))");
    Elements vsRhb = doc.select("tr.g2:has(td:contains(Versus RHB))");

    if (site.getType() == Version.OOTP5 && doc.html().contains("Pitching Ratings")) {
      vsLhb = doc.select("tr.g:has(td:contains(vs. LHP)");
      vsRhb = doc.select("tr.g2:has(td:contains(vs. RHP)");
    }

    if (vsLhb.isEmpty() || vsRhb.isEmpty()) {
      return null;
    }

    String raw = doc.select("td.s1:contains(Pitching Ratings) + td").html();

    int endurance;
    switch (site.getType()) {
      case OOTP6:
        endurance = Integer.parseInt(StringUtils.substringBetween(raw, "<br />", "<br />").trim());
        break;
      case OOTP5:
        String starter = StringUtils.substringBetween(raw, "<br />", "<br />").trim();

        if (!starter.equals("-")) {
          endurance = 10 - (starter.charAt(0) - 'A');
          break;
        }

        String reliever = StringUtils.substringBetween(raw, "-<br />", "<br />").trim();

        if (!reliever.equals("-")) {
          endurance = 5 - (reliever.charAt(0) - 'A');
          break;
        }

        endurance = 1;
        break;
      default:
        throw new IllegalStateException();
    }

    PitchingRatings<?> l = extractPitchingRatings(vsLhb.get(0), site.getAbilityRatingScale(), endurance);
    PitchingRatings<?> r = extractPitchingRatings(vsRhb.get(0), site.getAbilityRatingScale(), endurance);

    return Splits.create(l, r);
  }

  private PitchingRatings<?> extractPitchingPotential(Document doc) {
    Elements talent = doc.select("tr.g:has(td:contains(Talent))");

    return extractPitchingRatings(
        talent.get(0),
        site.getPotentialRatingScale(),
        extractPitchingRatings(doc).getVsLeft().getEndurance());
  }

  private Integer extractRange(String raw, String position) {
    if (!raw.contains(position + " :")) {
      return 0;
    }
    return ratingFromString(StringUtils.substringBetween(raw, position + " :", "(Range)").trim());
  }

  private Double extractFieldingPct(String raw, String position) {
    if (!raw.contains(position + " :")) {
      return 0.0;
    }

    String rawPosStr = StringUtils.substringBetween(raw, position + " :", "(Fielding Pct.)");

    return Double.valueOf(StringUtils.substringAfter(rawPosStr, "(Range),").trim());
  }

  private Integer extractPositionRating(String raw, String position) {
    /*String rawPosStr = StringUtils.substringBetween(raw, position + " :", "(Fielding Pct.)");

      Double fpct = Double.valueOf(StringUtils.substringAfter(rawPosStr, "(Range),").trim());

      return extractRange(raw, position).doubleValue() + (fpct / 1000.0);*/
    return extractRange(raw, position);
  }

  private FieldingRatings extractCatcherRating(String raw) {
    return FieldingRatings
      .builder()
      .ability(extractRange(raw, "C") * 10)
      .arm(ratingFromString(StringUtils.substringBetween(raw, "Catcher Arm :", raw.contains("Infield") ? "Infield" : "Outfield")) * 10)
      .build();
  }

  private FieldingRatings extractInfieldRating(String raw) {
    Integer first = 2 * extractRange(raw, "1B");
    Integer second = 8 * extractRange(raw, "2B");
    Integer third = 8 * extractRange(raw, "3B");
    Integer shortstop = 10 * extractRange(raw, "SS");

    Integer range = Ordering.natural().max(first, second, third, shortstop);
    Integer arm = site.getType() == Version.OOTP5
      ? 50
      : ratingFromString(StringUtils.substringBetween(raw, "Infield Arm :", "Outfield")) * 10;


    Integer firstErrors = extractAndAdapt(raw, Position.FIRST_BASE);
    Integer secondErrors = extractAndAdapt(raw, Position.SECOND_BASE);
    Integer thirdErrors = extractAndAdapt(raw, Position.THIRD_BASE);
    Integer shortstopErrors = extractAndAdapt(raw, Position.SHORTSTOP);

    Integer n = 2*firstErrors + 8*secondErrors + 8*thirdErrors + 10*shortstopErrors;

    Integer d = (first == 0 ? 0 : 2)
      + (second == 0 ? 0 : 8)
      + (third == 0 ? 0 : 8)
      + (shortstop == 0 ? 0 : 10);

    Integer errors = d == 0 ? 0 : (int) Math.round((double) n / d);

    return FieldingRatings
      .builder()
      .range(range)
      .arm(arm)
      .errors(errors)
      .dp(arm)
      .build();
  }

  private FieldingRatings extractOutfieldRating(String raw) {
    Integer first = 3 * extractRange(raw, "1B");
    Integer lf = 7 * extractRange(raw, "LF");
    Integer cf = 10 * extractRange(raw, "CF");
    Integer rf = 7 * extractRange(raw, "RF");

    Integer range = Ordering.natural().max(first, lf, cf, rf);
    Integer arm = ratingFromString(StringUtils.substringAfter(raw, "Outfield Arm :")) * 10;

    Integer firstErrors = extractAndAdapt(raw, Position.FIRST_BASE);
    Integer lfErrors = extractAndAdapt(raw, Position.LEFT_FIELD);
    Integer cfErrors = extractAndAdapt(raw, Position.CENTER_FIELD);
    Integer rfErrors = extractAndAdapt(raw, Position.RIGHT_FIELD);

    Integer n = 3*firstErrors + 7*lfErrors + 10*cfErrors + 7*rfErrors;
    Integer d = (first == 0 ? 0 : 3)
      + (lf == 0 ? 0 : 7)
      + (cf == 0 ? 0 : 10)
      + (rf == 0 ? 0 : 7);

    Integer errors = d == 0 ? 0 : (int) Math.round((double) n / d);

    return FieldingRatings
      .builder()
      .range(range)
      .arm(arm)
      .errors(errors)
      .build();
  }

  private Integer extractAndAdapt(String raw, Position p) {
    return adaptFpct(p, extractFieldingPct(raw, p.getAbbreviation()));
  }

  private Integer adaptFpct(Position p, Double fpct) {
    Double avg = AVERAGE_FPCT.get(p);

    Double ptsPerPct = 50.0 / (1.0 - avg);

    Double result = 50.0 + (fpct - avg) * ptsPerPct;

    return (int) Math.max(0, Math.round(result));
  }

  private Integer ratingFromString(String s) {
    if (s == null) { return 0; }

    switch (site.getType()) {
      case OOTP6:
        if (s.trim().equals("-")) {
          return 0;
        }
        return Integer.parseInt(s.trim());
      case OOTP5:
        switch (s.trim()) {
          case "A": return 9;
          case "B": return 7;
          case "C": return 5;
          case "D": return 3;
          case "E": return 1;
          case "-": return 0;
          default: throw new IllegalStateException();
        }
      default:
        throw new IllegalStateException();
    }
  }

  private <T> PitchingRatings<T> extractPitchingRatings(Element el, Scale<T> scale, int endurance) {
    Elements line = el.children();

    ImmutableMap<PitchingRatingsType, Integer> idx;
    switch (site.getType()) {
      case OOTP5:
        idx = OOTP5_PITCHING;
        break;
      case OOTP6:
        idx = OOTP6_PITCHING;
        break;
      default:
        throw new IllegalStateException();
    }

    PitchingRatings<T> ratings = PitchingRatings
      .builder(scale)
      .hits(line.get(idx.get(PitchingRatingsType.HITS)).text())
      .gap(line.get(idx.get(PitchingRatingsType.GAP)).text())
      .stuff(line.get(idx.get(PitchingRatingsType.STUFF)).text())
      .control(line.get(idx.get(PitchingRatingsType.CONTROL)).text())
      .movement(line.get(idx.get(PitchingRatingsType.MOVEMENT)).text())
      .endurance(endurance)
      .build();

    return ratings;
  }

}
