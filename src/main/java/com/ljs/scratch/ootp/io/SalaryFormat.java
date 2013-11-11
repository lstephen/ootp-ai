package com.ljs.scratch.ootp.io;

import java.math.BigDecimal;

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
        if (salary <= 0) {
            return "";
        }

        Integer displayDigits = Integer.parseInt(salary.toString().substring(0, 3)) + 1;

        int exp = (int) (Math.log(salary) / Math.log(10));

        String display;
        String suffix;

        if (exp < 3) {
            display = displayDigits.toString();
            suffix = " ";
        } else if (exp < 6) {
            suffix = "k";
            display = BigDecimal.valueOf(displayDigits).movePointLeft(5 - exp).toPlainString();
        } else if (exp < 9) {
            suffix = "m";
            display = BigDecimal.valueOf(displayDigits).movePointLeft(8 - exp).toPlainString();
        } else {
            throw new IllegalStateException();
        }

        return String.format("$%s%s", display, suffix);
    }

}
