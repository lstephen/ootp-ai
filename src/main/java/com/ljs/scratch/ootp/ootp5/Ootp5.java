package com.ljs.scratch.ootp.ootp5;

import com.ljs.scratch.ootp.ootp5.site.SinglePlayer;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.SiteDefinition;

/**
 *
 * @author lstephen
 */
public final class Ootp5 {

    private Ootp5() { }

    public static Site create(SiteDefinition def) {
        SinglePlayer ps = new SinglePlayer();
        SiteImpl site = SiteImpl.create(def, ps);
        ps.setSite(site);
        ps.setSalarySource(site);
        return site;
    }

}
