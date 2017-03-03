package com.github.lstephen.ootp.ai.player;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

public enum InjuryRating {
  PRONE,
  NORMAL,
  DURABLE;

  public static InjuryRating extractFrom(Document doc) {
    String starsText = doc.select("td.s4:containsOwn(Stars)").html();
    String[] split = StringUtils.splitByWholeSeparator(starsText, "<br />");
    String injuryRatingText = split[split.length - 3];

    if (injuryRatingText.equalsIgnoreCase("Prone")) {
      return PRONE;
    } else if (injuryRatingText.equalsIgnoreCase("Normal")) {
      return NORMAL;
    } else if (injuryRatingText.equalsIgnoreCase("Durable")) {
      return DURABLE;
    }

    throw new IllegalArgumentException("Unknown Injury Rating: " + injuryRatingText);
  }
}
