package com.ljs.ootp.ai.ootpx.site;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.ljs.ootp.ai.io.SalaryFormat;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.player.ratings.BattingRatings;
import com.ljs.ootp.ai.player.ratings.DefensiveRatings;
import com.ljs.ootp.ai.player.ratings.FieldingRatings;
import com.ljs.ootp.ai.player.ratings.PitchingRatings;
import com.ljs.ootp.ai.player.ratings.PlayerRatings;
import com.ljs.ootp.ai.player.ratings.Position;
import com.ljs.ootp.extract.html.rating.Scale;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.splits.Splits;
import java.text.NumberFormat;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.api.Assertions;
import org.fest.util.Strings;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public class PlayerExtraction {

    private final Site site;

    private PlayerExtraction(Site site) {
        Assertions.assertThat(site).isNotNull();
        this.site = site;
    }

    public Player extract(PlayerId id) {
        Document doc = Pages.player(site, id).load();

        PlayerRatings ratings = extractRatings(doc);

        ratings.setBattingPotential(extractBattingPotential(doc));
        ratings.setPitchingPotential(extractPitchingPotential(doc));

        Player player = Player.create(id, extractName(doc), ratings);

        player.setAge(Integer.parseInt(StringUtils.substringAfterLast(doc.select("td:containsOwn(Age:)").text(), " ")));
        player.setTeam(doc.select("a.title3[href~=teams]").text());

        player.setSalary(extractSalary(doc, player));

        if (site.isInjured(player)) {
            player.setTeam("*INJ* " + player.getTeam());
        }

        if (site.isFutureFreeAgent(player)) {
            player.setTeam("*FA* " + player.getTeam());
        }

        player.setListedPosition(StringUtils.substringBefore(
            doc.select("td:containsOwn(" + player.getFirstName() + ")").first().text(),
            " "));

        Optional<Integer> teamTopProspectPosition =
            site.getTeamTopProspectPosition(id);

        if (teamTopProspectPosition.isPresent()) {
            player.setTeamTopProspectPosition(teamTopProspectPosition.get());
        }

        return player;
    }

    private String extractName(Document doc) {
        return CharMatcher.WHITESPACE.trimFrom(
            doc.select("td.capt1 > a").text());
    }

    private String extractSalary(Document doc, Player p) {
        Integer currentSalary = getCurrentSalary(doc);

        if (currentSalary == 0) {
            return "";
        } else {
            String salarySuffix = "  ";

            Integer signedThrough;
            try {
                signedThrough = NumberFormat.getNumberInstance().parse(
                    doc.select("td:containsOwn(Signed Through:) + td").text()).intValue();
            } catch (ParseException e) {
                throw Throwables.propagate(e);
            }

            Integer currentYear = site.getDate().getYear();

            Integer signedFor = signedThrough - currentYear + 1;

            if (signedFor > 1) {
                salarySuffix = "x" + signedFor;
            }

            if (signedFor == 1 && !site.isFutureFreeAgent(p)) {
                salarySuffix = " r";
            }
            if (doc.select("td:containsOwn(Arbitration Eligibility:) + td").text().contains("Arbitration eligible")) {
                salarySuffix = " a";
            }
            if (!doc.select("td:containsOwn(Contract Extension:) + td").text().contains("-")) {
                salarySuffix = " e";
            }

            return SalaryFormat.prettyPrint(currentSalary) + salarySuffix;
        }
    }

    public Integer getCurrentSalary(PlayerId id) {
        return getCurrentSalary(Pages.player(site, id).load());
    }

    private Integer getCurrentSalary(Document doc) {
        String salary = CharMatcher.WHITESPACE.trimFrom(doc.select("td:containsOwn(Salary:) + td").text());

        if (!Strings.isNullOrEmpty(salary) && salary.charAt(0) == '$') {
            try {
                return NumberFormat.getNumberInstance().parse(salary.substring(1)).intValue();
            } catch (ParseException e) {
                throw Throwables.propagate(e);
            }
        }
        return 0;
    }

    private PlayerRatings extractRatings(Document doc) {
        return PlayerRatings.create(
            extractBattingRatings(doc),
            extractDefensiveRatings(doc),
            extractPitchingRatings(doc),
            site.getDefinition());
    }

    private boolean hasBattingRatings(Document doc) {
        return !doc.select("td:containsOwn(Contact):not(.sl,.slg)").isEmpty();
    }

    private Splits<BattingRatings<?>> extractBattingRatings(Document doc) {
        if (!hasBattingRatings(doc)) {
            return null;
        }

        Scale<?> s = site.getAbilityRatingScale();

        BattingRatings<?> vsL = BattingRatings
            .builder(s)
            .contact(doc.select("td:containsOwn(Contact) + td + td").text())
            .gap(doc.select("td:containsOwn(Gap) + td + td").text())
            .power(doc.select("td:containsOwn(Power) + td + td").text())
            .eye(doc.select("td:containsOwn(Eye) + td + td").text())
            .build();

        BattingRatings<?> vsR = BattingRatings
            .builder(s)
            .contact(doc.select("td:containsOwn(Contact) + td + td + td").text())
            .gap(doc.select("td:containsOwn(Gap) + td + td + td").text())
            .power(doc.select("td:containsOwn(Power) + td + td + td").text())
            .eye(doc.select("td:containsOwn(Eye) + td + td + td").text())
            .build();

        return Splits.create(vsL, vsR);
    }

    private BattingRatings extractBattingPotential(Document doc) {
        if (!hasBattingRatings(doc)) {
            return null;
        }

        Scale<?> s = site.getPotentialRatingScale();

        return BattingRatings
            .builder(s)
            .contact(doc.select("td:containsOwn(Contact) + td + td + td + td").text())
            .gap(doc.select("td:containsOwn(Gap) + td + td + td + td").text())
            .power(doc.select("td:containsOwn(Power) + td + td + td + td").text())
            .eye(doc.select("td:containsOwn(Eye) + td + td + td + td").text())
            .build();
    }

    private DefensiveRatings extractDefensiveRatings(Document doc) {
        DefensiveRatings defense = new DefensiveRatings();

        defense.setPositionRating(Position.CATCHER, extractDefensiveRating(doc, "Catcher:"));
        defense.setPositionRating(Position.FIRST_BASE, extractDefensiveRating(doc, "1st Base:"));
        defense.setPositionRating(Position.SECOND_BASE, extractDefensiveRating(doc, "2nd Base:"));
        defense.setPositionRating(Position.THIRD_BASE, extractDefensiveRating(doc, "3rd Base:"));
        defense.setPositionRating(Position.SHORTSTOP, extractDefensiveRating(doc, "Shortstop:"));
        defense.setPositionRating(Position.LEFT_FIELD, extractDefensiveRating(doc, "Left Field:"));
        defense.setPositionRating(Position.CENTER_FIELD, extractDefensiveRating(doc, "Center Field:"));
        defense.setPositionRating(Position.RIGHT_FIELD, extractDefensiveRating(doc, "Right Field:"));

        defense.setCatcher(extractCatcherRating(doc));
        defense.setInfield(extractInfieldRating(doc));
        defense.setOutfield(extractOutfieldRating(doc));

        return defense;
    }

    private Double extractDefensiveRating(Document doc, String needle) {
        String raw = doc.select("td:containsOwn(" + needle + ") + td").text();

        if (!Strings.isNullOrEmpty(raw) && !raw.equals("-")) {
            return (double) Integer.parseInt(raw) / 10;
        }

        return 0.0;
    }

    private FieldingRatings extractCatcherRating(Document doc) {
        return extractFieldingRatings(doc, "table.lposhadow:contains(Fielding Ratings) td:containsOwn(%s) + td");
    }

    private FieldingRatings extractInfieldRating(Document doc) {
        return extractFieldingRatings(doc, "table.lposhadow:contains(Fielding Ratings) td:containsOwn(%s) + td + td");
    }

    private FieldingRatings extractOutfieldRating(Document doc) {
        return extractFieldingRatings(doc, "table.lposhadow:contains(Fielding Ratings) td:containsOwn(%s) + td + td + td");
    }

    private FieldingRatings extractFieldingRatings(Document doc, String selector) {
        return FieldingRatings.builder()
            .range(parse(doc.select(String.format(selector, "Range:")).text()))
            .errors(parse(doc.select(String.format(selector, "Errors:")).text()))
            .arm(parse(doc.select(String.format(selector, "Arm:")).text()))
            .dp(parse(doc.select(String.format(selector, "Turn DP:")).text()))
            .ability(parse(doc.select(String.format(selector, "Ability:")).text()))
            .build();
    }

    private Integer parse(String s) {
        String trimmed = CharMatcher.WHITESPACE.trimFrom(s);

        if (Strings.isNullOrEmpty(trimmed) || trimmed.equals("-")) {
            return 0;
        }

        return Integer.parseInt(trimmed);
    }

    private boolean hasPitchingRatings(Document doc) {
        return !doc.select("td:containsOwn(Stuff):not(.sl,.slg)").isEmpty();

    }
    private Splits<PitchingRatings> extractPitchingRatings(Document doc) {
        if (!hasPitchingRatings(doc)) {
            return null;
        }
        PitchingRatings vsL = new PitchingRatings();
        vsL.setStuff(Integer.parseInt(doc.select("td:containsOwn(Stuff) + td + td").text()));
        vsL.setControl(Integer.parseInt(doc.select("td:containsOwn(Control) + td + td").text()));
        vsL.setMovement(Integer.parseInt(doc.select("td:containsOwn(Movement) + td + td").text()));

        vsL.setHits(vsL.getStuff());
        vsL.setGap(vsL.getMovement());

        PitchingRatings vsR = new PitchingRatings();
        vsR.setStuff(Integer.parseInt(doc.select("td:containsOwn(Stuff) + td + td + td").text()));
        vsR.setControl(Integer.parseInt(doc.select("td:containsOwn(Control) + td + td + td").text()));
        vsR.setMovement(Integer.parseInt(doc.select("td:containsOwn(Movement) + td + td + td").text()));

        vsR.setHits(vsR.getStuff());
        vsR.setGap(vsR.getMovement());

        //System.out.println(doc.select("td:containsOwn(Suggested Role) + td").text());
        String suggestedRole = doc.select("td:containsOwn(Suggested Role) + td").text();
        if (suggestedRole.contains("Starter") || suggestedRole.contains("SP")) {
            vsL.setEndurance(8);
            vsR.setEndurance(8);
        } else {
            vsL.setEndurance(3);
            vsR.setEndurance(3);
        };

        return Splits.create(vsL, vsR);
    }

    private PitchingRatings extractPitchingPotential(Document doc) {
        if (!hasPitchingRatings(doc)) {
            return null;
        }
        PitchingRatings potential = new PitchingRatings();
        potential.setStuff(Integer.parseInt(doc.select("td:containsOwn(Stuff) + td + td + td + td").text()));
        potential.setControl(Integer.parseInt(doc.select("td:containsOwn(Control) + td + td + td + td").text()));
        potential.setMovement(Integer.parseInt(doc.select("td:containsOwn(Movement) + td + td + td + td").text()));

        potential.setHits(potential.getStuff());
        potential.setGap(potential.getMovement());

        return potential;

    }

    public static PlayerExtraction create(Site site) {
        return new PlayerExtraction(site);
    }


}
