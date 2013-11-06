package com.ljs.scratch.ootp.ootp6.site;

import com.ljs.scratch.ootp.ootp5.site.SiteImpl;
import com.ljs.scratch.ootp.site.Site;
import com.ljs.scratch.ootp.site.SiteDefinition;

/**
 *
 * @author lstephen
 */
public class Ootp6 {

    public static Site create(SiteDefinition def) {
        return SiteImpl.create(def);
    }

}
