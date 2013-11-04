package com.ljs.scratch.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public class Wrapped<T> {

    private final T value;

    protected Wrapped(T value) {
        Assertions.assertThat(value).isNotNull();
        this.value = value;
    }

    public T unwrap() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }

        return getClass().cast(obj).unwrap().equals(this.unwrap());
    }

    @Override
    public int hashCode() {
        return unwrap().hashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
