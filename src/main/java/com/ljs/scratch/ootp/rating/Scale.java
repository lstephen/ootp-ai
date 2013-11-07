package com.ljs.scratch.ootp.rating;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ljs.scratch.ootp.ootp5.site.PotentialRating;
import com.ljs.scratch.ootp.ootp5.site.ZeroToTen;

/**
 *
 * @author lstephen
 */
@JsonSubTypes({
    @JsonSubTypes.Type(PotentialRating.RatingScale.class),
    @JsonSubTypes.Type(ZeroToTen.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public interface Scale<T> {

    Rating<T, ? extends Scale<T>> ratingOf(T value);

    Rating<T, ? extends Scale<T>> parse(String s);

    Rating<Integer, OneToOneHundred> normalize(T value);

}
