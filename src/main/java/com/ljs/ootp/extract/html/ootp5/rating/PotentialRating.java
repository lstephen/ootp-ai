package com.ljs.ootp.extract.html.ootp5.rating;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.CharMatcher;
import com.ljs.ootp.extract.html.rating.OneToOneHundred;
import com.ljs.ootp.extract.html.rating.Rating;
import com.ljs.ootp.extract.html.rating.Scale;
import java.util.Locale;

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

	@JsonCreator
    private static PotentialRating valueOfCaseInsensitive(String potential) {
        return PotentialRating.valueOf(potential.toUpperCase(Locale.ENGLISH));
    }

    public static Rating<PotentialRating, RatingScale> from(String s) {
        return Rating.create(
            valueOfCaseInsensitive(CharMatcher.WHITESPACE.trimFrom(s)),
            RatingScale.INSTANCE);
    }

    public static RatingScale scale() {
        return RatingScale.INSTANCE;
    }

    public static final class RatingScale implements Scale<PotentialRating> {
        private static final RatingScale INSTANCE = new RatingScale();

        private RatingScale() { }

        @Override
        public Rating<PotentialRating, RatingScale> parse(String s) {
            return PotentialRating.from(s);
        }

        @Override
        public Rating<Integer, OneToOneHundred> normalize(
            PotentialRating value) {

            return OneToOneHundred.valueOf(value.value);
        }
    }

}
