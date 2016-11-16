package com.github.lstephen.ootp.ai.site;

/** @author lstephen */
public final class SiteHolder {

  private static Site site;

  private SiteHolder() {}

  public static void set(Site site) {
    SiteHolder.site = site;
  }

  public static Site get() {
    return site;
  }
}
