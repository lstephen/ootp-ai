package com.ljs.ootp.ai.io;

import humanize.Humanize;

/**
 *
 * @author lstephen
 */
public final class SalaryFormat {

    private SalaryFormat() { }

    public static String prettyPrint(Number salary) {
        return prettyPrint(salary.intValue());
    }

    public static String prettyPrint(Integer salary) {
        return salary > 0 ? "$" + Humanize.metricPrefix(salary) : "";
    }

}
