package com.github.lstephen.scratch.util;

import org.jsoup.select.Elements;

/**
 *
 * @author lstephen
 */
public final class ElementsUtil {

    private ElementsUtil() { }

    public static Integer getInteger(Elements line, int idx) {
        return Integer.valueOf(line.get(idx).text());
    }

    public static Double getDouble(Elements line, int idx) {
        return Double.valueOf(line.get(idx).text());
    }

}
