package com.github.lstephen.ootp.ai.player;

import com.github.lstephen.ootp.ai.site.Version;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

public enum TeamLeaderSkills {
  NONE,
  SOME,
  GREAT;

  public static TeamLeaderSkills extractFrom(Version v, Document doc) {
    String starsText = doc.select("td.s4:containsOwn(Stars)").html();
    String[] split = StringUtils.splitByWholeSeparator(starsText, "<br />");

    String leaderText = split[split.length - 6];

    if (v == Version.OOTP6) {
      leaderText = split[2];
    }

    if (leaderText.equalsIgnoreCase("None at all")) {
      return NONE;
    } else if (leaderText.equalsIgnoreCase("Some ability")) {
      return SOME;
    } else if (leaderText.equalsIgnoreCase("Great Leader")) {
      return GREAT;
    }

    throw new IllegalArgumentException("Unknown Team Leader Skills Rating: " + leaderText);
  }
}
