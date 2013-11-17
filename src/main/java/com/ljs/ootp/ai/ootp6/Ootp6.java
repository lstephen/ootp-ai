package com.ljs.ootp.ai.ootp6;

import com.ljs.ootp.ai.ootp5.SiteImpl;
import com.ljs.ootp.ai.ootp5.site.SinglePlayer;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.site.SiteDefinition;

/**
 *
 * @author lstephen
 */
public class Ootp6 {

    public static Site create(SiteDefinition def) {
        SinglePlayer ps = new SinglePlayer();
        SiteImpl site = SiteImpl.create(def, ps);
        ps.setSite(site);
        ps.setSalarySource(site);
        return site;
    }

}
