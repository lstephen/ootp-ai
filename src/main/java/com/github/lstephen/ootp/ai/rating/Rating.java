package com.github.lstephen.ootp.ai.rating;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonValue;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 *
 * @author lstephen
 */
public final class Rating<T, S extends Scale<T>> {

    private static final Logger LOGGER = Logger.getLogger(Rating.class.getName());

    private final T value;

    private final S scale;

    private Rating(T value, S scale) {
        Preconditions.checkNotNull(value);
        Preconditions.checkNotNull(scale);

        if (String.class.isInstance(value)) {
          Exception backtrace = new RuntimeException();
          LOGGER.log(Level.WARNING, "Received String for Rating: " + value + ":" + scale, backtrace);
          this.value = scale.parse(String.class.cast(value)).value;
        } else {
          this.value = value;
        }

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
