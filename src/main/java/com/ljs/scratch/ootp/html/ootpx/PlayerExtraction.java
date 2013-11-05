package com.ljs.scratch.ootp.html.ootpx;

import com.google.common.base.CharMatcher;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.ratings.DefensiveRatings;
import com.ljs.scratch.ootp.ratings.PitchingRatings;
import com.ljs.scratch.ootp.ratings.PlayerRatings;
import com.ljs.scratch.ootp.ratings.Position;
import com.ljs.scratch.ootp.ratings.Splits;
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

        return player;
    }

    private String extractName(Document doc) {
        return CharMatcher.WHITESPACE.trimFrom(
            doc.select("td.capt1 > a").text());
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

    private Splits<BattingRatings> extractBattingRatings(Document doc) {
        if (!hasBattingRatings(doc)) {
            return null;
        }

        BattingRatings vsL = BattingRatings
            .builder()
            .contact(Integer.parseInt(doc.select("td:containsOwn(Contact) + td + td").text()))
            .gap(Integer.parseInt(doc.select("td:containsOwn(Gap) + td + td").text()))
            .power(Integer.parseInt(doc.select("td:containsOwn(Power) + td + td").text()))
            .eye(Integer.parseInt(doc.select("td:containsOwn(Eye) + td + td").text()))
            .build();

        BattingRatings vsR = BattingRatings
            .builder()
            .contact(Integer.parseInt(doc.select("td:containsOwn(Contact) + td + td + td").text()))
            .gap(Integer.parseInt(doc.select("td:containsOwn(Gap) + td + td + td").text()))
            .power(Integer.parseInt(doc.select("td:containsOwn(Power) + td + td + td").text()))
            .eye(Integer.parseInt(doc.select("td:containsOwn(Eye) + td + td + td").text()))
            .build();

        return Splits.create(vsL, vsR);
    }

    private BattingRatings extractBattingPotential(Document doc) {
        if (!hasBattingRatings(doc)) {
            return null;
        }

        return BattingRatings
            .builder()
            .contact(Integer.parseInt(doc.select("td:containsOwn(Contact) + td + td + td + td").text()))
            .gap(Integer.parseInt(doc.select("td:containsOwn(Gap) + td + td + td + td").text()))
            .power(Integer.parseInt(doc.select("td:containsOwn(Power) + td + td + td + td").text()))
            .eye(Integer.parseInt(doc.select("td:containsOwn(Eye) + td + td + td + td").text()))
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

        return defense;
    }

    private Double extractDefensiveRating(Document doc, String needle) {
        String raw = doc.select("td:containsOwn(" + needle + ") + td").text();

        if (!Strings.isNullOrEmpty(raw) && !raw.equals("-")) {
            return (double) Integer.parseInt(raw) / 10;
        }

        return 0.0;
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

        if (doc.select("td:containsOwn(Suggested Role) + td").text().contains("Starter")) {
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
