package com.ljs.scratch.ootp.ootp5.site;

import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.SiteDefinition;

/**
 *
 * @author lstephen
 */
public final class Ootp5 {

    public static Site create(SiteDefinition def) {
        SinglePlayer ps = new SinglePlayer();
        Site site = SiteImpl.create(def, ps);
        ps.setSite(site);
        return site;
    }

}
