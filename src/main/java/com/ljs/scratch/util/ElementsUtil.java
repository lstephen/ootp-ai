package com.ljs.scratch.util;

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

}
