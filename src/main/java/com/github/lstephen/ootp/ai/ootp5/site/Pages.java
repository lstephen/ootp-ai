package com.github.lstephen.ootp.ai.ootp5.site;

import com.github.lstephen.ootp.extract.html.Page;
import com.github.lstephen.ootp.ai.site.Site;

/**
 *
 * @author lstephen
 */
public final class Pages {

    private Pages() { }

    public static Page standings(Site site) {
        return site.getPage("standr.html");
    }

}
