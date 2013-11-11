package com.ljs.scratch.ootp.rating;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author lstephen
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public interface Scale<T> {

    Rating<T, ? extends Scale<T>> ratingOf(T value);

    Rating<T, ? extends Scale<T>> parse(String s);

    Rating<Integer, OneToOneHundred> normalize(T value);

}
