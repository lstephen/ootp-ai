package com.ljs.scratch.ootp.html;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.PlayerId;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.BattingRatings.BattingRatingsType;
import com.ljs.scratch.ootp.ratings.DefensiveRatings;
import com.ljs.scratch.ootp.ratings.PitchingRatings;
import com.ljs.scratch.ootp.ratings.PitchingRatings.PitchingRatingsType;
import com.ljs.scratch.ootp.ratings.PlayerRatings;
import com.ljs.scratch.ootp.ratings.Position;
import com.ljs.scratch.ootp.ratings.RatingsBuilder;
import com.ljs.scratch.ootp.ratings.Splits;
import com.ljs.scratch.ootp.site.Version;
import com.ljs.scratch.util.ElementsUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public class SinglePlayer {

    private static final Logger LOG = Logger.getLogger(SinglePlayer.class.getName());

    private static final ImmutableMap<String, Integer> OOTP5_POTENTIAL =
        ImmutableMap.of(
            "Poor", 1,
            "Fair", 3,
            "Average", 5,
            "Good", 7,
            "Brilliant", 9);

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

    private final PlayerId id;

    private final Site site;

    private final Page page;

    public SinglePlayer(Site site, PlayerId id) {
        this.id = id;
        this.site = site;
        page = site.getPage(id.unwrap() + ".html");
    }

    public Player extract() {
        Document doc;
        try {
            doc = page.load();
        } catch (Exception e) {
            Throwable root = Throwables.getRootCause(e);

            if (HttpStatusException.class.isInstance(root)
                && HttpStatusException.class.cast(root).getStatusCode() == 404) {
                LOG.log(Level.WARNING, "Player not found. ID: {0}", id);
                return null;
            } else {
                throw Throwables.propagate(e);
            }
        }

        Elements title = doc.select("title");

        String team = StringUtils.substringBefore(StringUtils.substringAfterLast(title.text(), ","), "-").trim();

        Elements info = doc.select("td.s4:has(b:contains(Name)) + td.s4");

        String[] splitInfo =
            StringUtils.splitByWholeSeparatorPreserveAllTokens(
                info.html(), "<br />");

        String name = splitInfo[0];
        Integer age = Integer.valueOf(splitInfo[3]);

        PlayerRatings ratings =
            PlayerRatings.create(
                extractBattingRatings(),
                extractDefensiveRatings(),
                extractPitchingRatings(),
                site.getDefinition());

        ratings.setBattingPotential(extractBattingPotential());

        if (ratings.hasPitching()) {
            ratings.setPitchingPotential(extractPitchingPotential());
        }

        Player player = Player.create(id, name, ratings);

        player.setAge(age);
        player.setTeam(team);

        if (site.isInjured(player)) {
            player.setTeam("*INJ* " + player.getTeam());
        }

        if (doc.html().contains("out for entire career")) {
            player.setTeam("*CEI* " + player.getTeam());
        }

        if (site.isFutureFreeAgent(player)) {
            player.setTeam("*FA* " + player.getTeam());
        }

        if (doc.html().contains("Rule 5 Draft Eligibility")) {
            player.setRuleFiveEligible(isRuleFiveEligible(doc));
        }

        if (doc.html().contains("Rule 5 Draft Eligibility")) {
            player.setOn40Man(doc.html().contains("on 40 Man Roster"));
        }

        if (doc.html().contains("Minor League Option Years")) {
            player.setOutOfOptions(doc.html().contains("Out of Option Years"));
        }

        if (doc.html().contains("Minor League Option Years")) {
            player.setClearedWaivers(doc.html().contains("Waivers cleared"));
        }

        if (doc.html().contains("Years of Pro Service")) {
            player.setYearsOfProService(getYearsOfProService(doc));
        }

        Optional<Integer> teamTopProspectPosition =
            site.getTeamTopProspectPosition(id);

        if (teamTopProspectPosition.isPresent()) {
            player.setTeamTopProspectPosition(teamTopProspectPosition.get());
        }


        player.setSalary(site.getSalary(player));

        return player;
    }

    private Splits<BattingRatings> extractBattingRatings() {
        Document doc = page.load();

        Elements ratings = doc.select("tr:has(td:contains(Batting Ratings)) + tr");

        if (ratings.isEmpty()) {
            ratings = doc.select("tr:has(td:contains(Ratings)) + tr");
        }

        Elements vsLhp = ratings.select("tr.g:has(td:contains(LHP)), tr.g2:has(td:contains(LHP))");
        Elements vsRhp = ratings.select("tr.g:has(td:contains(RHP)), tr.g2:has(td:contains(RHP))");

        return Splits.<BattingRatings>create(
            extractBattingRatings(vsLhp.get(0)),
            extractBattingRatings(vsRhp.get(0)));
    }

    private BattingRatings extractBattingPotential() {
        Document doc = page.load();

        Elements ratingsEls = doc.select("tr:has(td:contains(Batting Ratings)) + tr");

        if (ratingsEls.isEmpty()) {
            ratingsEls = doc.select("tr:has(td:contains(Ratings)) + tr");
        }

        Elements potential = ratingsEls.select("tr.g:has(td:contains(Talent))");

        if (site.getType() == Version.OOTP5) {
            Elements els = potential.get(0).children();

            return RatingsBuilder
                .batting()
                .contact(getOotp5Potential(els, OOTP5_HITTING.get(BattingRatingsType.CONTACT)))
                .gap(getOotp5Potential(els, OOTP5_HITTING.get(BattingRatingsType.GAP)))
                .power(getOotp5Potential(els, OOTP5_HITTING.get(BattingRatingsType.POWER)))
                .eye(getOotp5Potential(els, OOTP5_HITTING.get(BattingRatingsType.EYE)))
                .build();

        } else {
            BattingRatings unscaled = extractBattingRatings(potential.get(0));

            return RatingsBuilder
                .batting()
                .contact(scaleOotp6PotentialRating(unscaled.getContact()))
                .gap(scaleOotp6PotentialRating(unscaled.getGap()))
                .power(scaleOotp6PotentialRating(unscaled.getPower()))
                .eye(scaleOotp6PotentialRating(unscaled.getEye()))
                .build();
        }
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

    private Integer getOotp5Potential(Elements els, int idx) {
        return OOTP5_POTENTIAL.get(els.get(idx).text().trim());

    }

    private DefensiveRatings extractDefensiveRatings() {
        Document doc = page.load();

        DefensiveRatings ratings = new DefensiveRatings();

        String raw = doc.select("td.s4:contains(Fielding Ratings)").text();

        for (Position p : Position.values()) {
            if (raw.contains(p.getAbbreviation() + " :")) {
                Double rating = extractPositionRating(raw, p.getAbbreviation());

                if (p == Position.CATCHER && rating == 0) {
                    rating = 1.0;
                }
                ratings.setPositionRating(p, rating);
            }
        }

        if (raw.contains("Infield Arm :")) {
            ratings.setInfieldArm(Integer.parseInt(StringUtils.substringBetween(
                raw, "Infield Arm :", "Outfield").trim()));
        }

        if (raw.contains("Outfield Arm :")) {
            Integer ofArm = ratingFromString(StringUtils.substringAfter(
                raw, "Outfield Arm :").trim());

            if (site.getType() == Version.OOTP5 && ofArm > 0) {
                ofArm = (ofArm - 5) / 2 + 5;
            }

            ratings.setOutfieldArm(ofArm);
        }

        return ratings;
    }

    private Splits<PitchingRatings> extractPitchingRatings() {
        Document doc = page.load();

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
                throw new IllegalStateException();
            default:
                throw new IllegalStateException();
        }

        PitchingRatings l = extractPitchingRatings(vsLhb.get(0));
        PitchingRatings r = extractPitchingRatings(vsRhb.get(0));

        l.setEndurance(endurance);
        r.setEndurance(endurance);

        return Splits.create(l, r);
    }

    private PitchingRatings extractPitchingPotential() {
        Document doc = page.load();

        Elements talent = doc.select("tr.g:has(td:contains(Talent))");

        if (site.getType() == Version.OOTP5) {
            PitchingRatings ratings = new PitchingRatings();

            Elements els = talent.get(0).children();

            ratings.setHits(getOotp5Potential(els, OOTP5_PITCHING.get(PitchingRatingsType.HITS)));
            ratings.setGap(getOotp5Potential(els, OOTP5_PITCHING.get(PitchingRatingsType.GAP)));
            ratings.setStuff(getOotp5Potential(els, OOTP5_PITCHING.get(PitchingRatingsType.STUFF)));
            ratings.setMovement(getOotp5Potential(els, OOTP5_PITCHING.get(PitchingRatingsType.MOVEMENT)));
            ratings.setControl(getOotp5Potential(els, OOTP5_PITCHING.get(PitchingRatingsType.CONTROL)));

            ratings.setEndurance(extractPitchingRatings().getVsLeft().getEndurance());

            return ratings;
        } else {
            PitchingRatings unscaled = extractPitchingRatings(talent.get(0));

            PitchingRatings scaled = new PitchingRatings();
            scaled.setStuff(scaleOotp6PotentialRating(unscaled.getStuff()));
            scaled.setControl(scaleOotp6PotentialRating(unscaled.getControl()));
            scaled.setMovement(scaleOotp6PotentialRating(unscaled.getMovement()));
            scaled.setHits(scaleOotp6PotentialRating(unscaled.getHits()));
            scaled.setGap(scaleOotp6PotentialRating(unscaled.getGap()));

            scaled.setEndurance(extractPitchingRatings().getVsLeft().getEndurance());

            return scaled;
        }
    }

    private Double extractPositionRating(String raw, String position) {
        String rawPosStr = StringUtils.substringBetween(raw, position + " :", "(Fielding Pct.)");

        Integer range = ratingFromString(StringUtils.substringBefore(rawPosStr, "(Range)").trim());
        //Integer range = ratingFromString(StringUtils.substringBetween(rawPosStr, position + " :", "(Range)").trim());

        Double fpct = Double.valueOf(StringUtils.substringAfter(rawPosStr, "(Range),").trim());

        return range.doubleValue() + fpct;
    }

    private Integer ratingFromString(String s) {
        if (s == null) { return 0; }

        switch (site.getType()) {
            case OOTP6:
                return Integer.parseInt(s.trim());
            case OOTP5:
                switch (s.trim()) {
                    case "A": return 10;
                    case "B": return 7;
                    case "C": return 5;
                    case "D": return 3;
                    case "E": return 0;
                    case "-": return 0;
                    default: throw new IllegalStateException();
                }
            default:
                throw new IllegalStateException();
        }
    }

    private BattingRatings extractBattingRatings(Element el) {

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

        return RatingsBuilder
            .batting()
            .contact(ElementsUtil.getInteger(line, idx.get(BattingRatingsType.CONTACT)))
            .gap(ElementsUtil.getInteger(line, idx.get(BattingRatingsType.GAP)))
            .power(ElementsUtil.getInteger(line, idx.get(BattingRatingsType.POWER)))
            .eye(ElementsUtil.getInteger(line, idx.get(BattingRatingsType.EYE)))
            .build();
    }

    private PitchingRatings extractPitchingRatings(Element el) {
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

        PitchingRatings ratings = new PitchingRatings();
        ratings.setHits(ElementsUtil.getInteger(line, idx.get(PitchingRatingsType.HITS)));
        ratings.setGap(ElementsUtil.getInteger(line, idx.get(PitchingRatingsType.GAP)));
        ratings.setStuff(ElementsUtil.getInteger(line, idx.get(PitchingRatingsType.STUFF)));
        ratings.setControl(ElementsUtil.getInteger(line, idx.get(PitchingRatingsType.CONTROL)));
        ratings.setMovement(ElementsUtil.getInteger(line, idx.get(PitchingRatingsType.MOVEMENT)));
        return ratings;
    }

    private int scaleOotp6PotentialRating(int r) {
        if (site.getName().equals("BTH")) {
            // scale 1-10 to 1-100 scale
            return r * 10 - 5;
        }

        // scale 2-8 to 1-20 scale
        return r * 2 + (r - 5);
    }


}
