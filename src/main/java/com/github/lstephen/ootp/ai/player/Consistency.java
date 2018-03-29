package com.github.lstephen.ootp.ai.player;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

public enum Consistency {
  VERY_INCONSISTENT,
  AVERAGE,
  GOOD;

  public static Consistency extractFrom(Document doc) {
    String starsText = doc.select("td.s4:containsOwn(Stars)").html();
    // System.out.println(starsText);
    String[] split = StringUtils.splitByWholeSeparator(starsText, "<br />");
    // System.out.println(Strings.join(split).with(","));
    String clutchText = split[split.length - 5];

    if (clutchText.equalsIgnoreCase("Very inconsistent")) {
      return VERY_INCONSISTENT;
    } else if (clutchText.equalsIgnoreCase("Average")) {
      return AVERAGE;
    } else if (clutchText.equalsIgnoreCase("Good")) {
      return GOOD;
    }

    throw new IllegalArgumentException("Unknown Consistency Rating: " + clutchText);
  }
}
