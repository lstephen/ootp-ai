package com.ljs.scratch.ootp.ootp5.site;

import com.ljs.scratch.ootp.html.Page;
import com.ljs.scratch.ootp.site.Site;

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
