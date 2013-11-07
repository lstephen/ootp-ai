package com.ljs.scratch.ootp.rating;

/**
 *
 * @author lstephen
 */
public interface Scale<T> {

    Rating<Integer, OneToOneHundred> normalize(T value);

}
