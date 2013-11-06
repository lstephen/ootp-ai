package com.ljs.scratch.ootp.ootp5.site;

import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.SiteDefinition;

/**
 *
 * @author lstephen
 */
public class Ootp5 {

    public static Site create(SiteDefinition def) {
        return SiteImpl.create(def);
    }

}
