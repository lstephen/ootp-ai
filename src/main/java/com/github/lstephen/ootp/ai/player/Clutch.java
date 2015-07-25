package com.github.lstephen.ootp.ai.player;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

public enum Clutch {
  SUFFERS, NORMAL, GREAT;

  public static Clutch extractFrom(Document doc) {
      String starsText = doc.select("td.s4:containsOwn(Stars)").html();
      //System.out.println(starsText);
      String[] split = StringUtils.splitByWholeSeparator(starsText, "<br />");
      //System.out.println(Strings.join(split).with(","));
      String clutchText = split[split.length - 4];

      if (clutchText.equalsIgnoreCase("Suffers")) {
        return SUFFERS;
      } else if (clutchText.equalsIgnoreCase("Normal")) {
        return NORMAL;
      } else if (clutchText.equalsIgnoreCase("GREAT")) {
        return GREAT;
      }

      throw new IllegalArgumentException("Unknown Clutch Rating: " + clutchText);
  }
}



