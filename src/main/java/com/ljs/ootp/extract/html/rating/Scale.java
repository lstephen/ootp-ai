package com.ljs.ootp.extract.html.rating;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ljs.ootp.extract.html.ootp5.rating.PotentialRating;
import com.ljs.ootp.extract.html.ootp5.rating.ZeroToTen;
import com.ljs.ootp.extract.html.ootp6.rating.OneToTen;
import com.ljs.ootp.extract.html.ootp6.rating.OneToTwenty;
import com.ljs.ootp.extract.html.ootp6.rating.TwoToEight;

/**
 *
 * @author lstephen
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
    @Type(ZeroToTen.class),
    @Type(OneToTen.class),
    @Type(PotentialRating.RatingScale.class),
    @Type(OneToOneHundred.class),
    @Type(OneToTwenty.class),
    @Type(TwoToEight.class)
})
public interface Scale<T> {

    Rating<T, ? extends Scale<T>> parse(String s);

    Rating<Integer, OneToOneHundred> normalize(T value);

}
