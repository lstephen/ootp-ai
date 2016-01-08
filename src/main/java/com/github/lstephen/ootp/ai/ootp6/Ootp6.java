package com.github.lstephen.ootp.ai.ootp6;

import com.github.lstephen.ootp.ai.ootp5.SiteImpl;
import com.github.lstephen.ootp.ai.ootp5.site.SinglePlayer;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.site.SiteDefinition;

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
