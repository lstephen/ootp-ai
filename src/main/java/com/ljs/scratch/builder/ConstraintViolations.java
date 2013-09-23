package com.ljs.scratch.builder;

import com.google.common.base.Function;
import javax.validation.ConstraintViolation;

/**
 *
 * @author lstephen
 */
public final class ConstraintViolations {

    private ConstraintViolations() { }

    public static <T> Function<ConstraintViolation<T>, String> getMessage() {
        return new Function<ConstraintViolation<T>, String>() {
            @Override
            public String apply(ConstraintViolation<T> violation) {
                return violation.getMessage();
            }};
    }

}
