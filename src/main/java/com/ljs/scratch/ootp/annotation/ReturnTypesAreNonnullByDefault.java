/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ljs.scratch.ootp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

@Nonnull
@TypeQualifierDefault(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReturnTypesAreNonnullByDefault { }
