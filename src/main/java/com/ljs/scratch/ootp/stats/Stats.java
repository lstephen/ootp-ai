package com.ljs.scratch.ootp.stats;

/**
 *
 * @author lstephen
 */
public interface Stats<Self extends Stats<Self>> {

    Self multiply(double factor);

    Self add(Self rhs);

}
