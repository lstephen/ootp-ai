package com.github.lstephen.ootp.ai.ootp5;

import com.github.lstephen.ootp.ai.ootp5.site.SinglePlayer;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.site.SiteDefinition;

/** @author lstephen */
public final class Ootp5 {

  private Ootp5() {}

  public static Site create(SiteDefinition def) {
    SinglePlayer ps = new SinglePlayer();
    SiteImpl site = SiteImpl.create(def, ps);
    ps.setSite(site);
    ps.setSalarySource(site);
    return site;
  }
}
