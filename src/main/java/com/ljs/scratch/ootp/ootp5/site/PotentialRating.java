package com.ljs.scratch.ootp.ootp5.site;

import com.google.common.base.CharMatcher;
import com.ljs.scratch.ootp.rating.OneToOneHundred;
import com.ljs.scratch.ootp.rating.Rating;
import com.ljs.scratch.ootp.rating.Scale;
import java.util.Locale;
import org.jsoup.nodes.Element;

/**
 *
 * @author lstephen
 */
public enum PotentialRating {
    POOR(10), FAIR(30), AVERAGE(50), GOOD(70), BRILLIANT(90);

    private final Integer value;

    private PotentialRating(Integer value) {
        this.value = value;
    }

    private static PotentialRating valueOfCaseInsensitive(String potential) {
        return PotentialRating.valueOf(potential.toUpperCase(Locale.ENGLISH));
    }

    public static Rating<PotentialRating, RatingScale> from(Element el) {
        return Rating.create(
            valueOfCaseInsensitive(CharMatcher.WHITESPACE.trimFrom(el.text())),
            RatingScale.INSTANCE);
    }

    public static class RatingScale implements Scale<PotentialRating> {
        private static final RatingScale INSTANCE = new RatingScale();

        private RatingScale() { }

        @Override
        public Rating<Integer, OneToOneHundred> normalize(
            PotentialRating value) {

            return OneToOneHundred.valueOf(value.value);
        }
    }

}
