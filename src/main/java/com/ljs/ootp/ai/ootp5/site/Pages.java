package com.ljs.ootp.ai.ootp5.site;

import com.ljs.ootp.extract.html.Page;
import com.ljs.ootp.ai.site.Site;

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
