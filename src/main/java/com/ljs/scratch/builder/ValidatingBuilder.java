package com.ljs.scratch.builder;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 *
 * @author lstephen
 */
public final class ValidatingBuilder<T> implements Builder<T> {

    private final T building;

    private ValidatingBuilder(T building) {
        this.building = building;
    }

    public T getBuilding() {
        return building;
    }

    @Override
    public T build() {
        Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<T>> violations = validator.validate(building);

        if (!violations.isEmpty()) {
            String violationMessage = Joiner
                .on("; ")
                .join(
                    Iterables.transform(
                        violations, ConstraintViolations.<T>getMessage()));


            throw new BuilderException(
                "Built object not valid: " + violationMessage);
        }

        return building;
    }

    public static <T> ValidatingBuilder<T> build(T building) {
        return new ValidatingBuilder<T>(building);
    }

}
