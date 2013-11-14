package com.ljs.scratch.ootp.ootp6;

import com.ljs.scratch.ootp.ootp5.site.SinglePlayer;
import com.ljs.scratch.ootp.ootp5.SiteImpl;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.SiteDefinition;

/**
 *
 * @author lstephen
 */
public class Ootp6 {

    public static Site create(SiteDefinition def) {
        SinglePlayer ps = new SinglePlayer();
        Site site = SiteImpl.create(def, ps);
        ps.setSite(site);
        return site;
    }

}
