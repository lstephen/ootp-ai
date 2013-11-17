package com.ljs.ootp.ai.rating;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ljs.ootp.ai.ootp5.site.PotentialRating;
import com.ljs.ootp.ai.ootp5.site.ZeroToTen;
import com.ljs.ootp.ai.ootp6.site.OneToTwenty;

/**
 *
 * @author lstephen
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
    @Type(ZeroToTen.class),
	@Type(PotentialRating.RatingScale.class),
    @Type(OneToOneHundred.class),
    @Type(OneToTwenty.class)
})
public interface Scale<T> {

    Rating<T, ? extends Scale<T>> parse(String s);

    Rating<Integer, OneToOneHundred> normalize(T value);

}
