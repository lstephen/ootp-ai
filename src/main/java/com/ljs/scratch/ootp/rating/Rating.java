package com.ljs.scratch.ootp.rating;

/**
 *
 * @author lstephen
 */
public final class Rating<T, S extends Scale<T>> {

    private final T value;

    private final S scale;

    private Rating(T value, S scale) {
        this.value = value;
        this.scale = scale;
    }

    public T get() {
        return value;
    }

    public Rating<Integer, OneToOneHundred> normalize() {
        return scale.normalize(value);
    }

    public static <T, S extends Scale<T>> Rating<T, S> create(
        T value, S scale) {

        return new Rating<T, S>(value, scale);
    }

}
