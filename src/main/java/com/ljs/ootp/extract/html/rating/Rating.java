package com.ljs.ootp.extract.html.rating;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 *
 * @author lstephen
 */
public final class Rating<T, S extends Scale<T>> {

    private final T value;

    private final S scale;

    private Rating(T value, S scale) {
        Preconditions.checkNotNull(value);
        Preconditions.checkNotNull(scale);

        // Seems that occaisionally a String gets through here.
        // This fix hides a problem somewhere else.
        // The error occurs when loading from JSON
        this.value = (String.class.isInstance(value)
            ? scale.parse(String.class.cast(value)).value
            : value);

        this.scale = scale;
    }

    @JsonValue
    public T get() {
        return value;
    }

    public Rating<Integer, OneToOneHundred> normalize() {
        try {
            return scale.normalize(value);
        } catch (ClassCastException e) {
            System.out.println(scale + ":" + value);
            throw Throwables.propagate(e);
        }
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
