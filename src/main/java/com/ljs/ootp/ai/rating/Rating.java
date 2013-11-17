package com.ljs.ootp.ai.rating;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

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

    @JsonValue
    public T get() {
        return value;
    }

    public Rating<Integer, OneToOneHundred> normalize() {
        return scale.normalize(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        Rating<?, ?> rhs = Rating.class.cast(obj);

        return Objects.equal(value, rhs.value)
            && Objects.equal(scale, rhs.scale);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, scale);
    }

    @Override
    public String toString() {
        return Objects
            .toStringHelper(this)
            .add("value", value)
            .add("scale", scale)
            .toString();
    }

    public static <T, S extends Scale<T>> Rating<T, S> create(
        T value, S scale) {

        return new Rating<T, S>(value, scale);
    }

}
