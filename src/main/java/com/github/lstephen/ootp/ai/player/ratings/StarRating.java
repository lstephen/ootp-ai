package com.github.lstephen.ootp.ai.player.ratings;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

/**
 *
 * @author lstephen
 */
public final class StarRating {

    private static enum Type { BLUE, GOLD };

    private final Type type;

    private final Integer number;

    private final Boolean half;

    private StarRating(Type type, Integer number, Boolean half) {
        this.type = type;
        this.number = number;
        this.half = half;
    }

    public String getFormattedText() {
        switch (type) {
            case BLUE:
                return getFormattedText('+', '-');
            case GOLD:
                return getFormattedText('*', '^');
            default:
                throw new IllegalArgumentException();
        }
    }

    private String getFormattedText(Character full, Character half) {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < number; i++) {
            str.append(full);
        }

        if (this.half) {
            str.append(half);
        }
        
        return str.toString();
    }

    public static StarRating extractFrom(Document doc) {
        String starsText = doc.select("td.s4:containsOwn(Stars)").html();
        //System.out.println(starsText);
        String[] split = StringUtils.splitByWholeSeparator(starsText, "<br />");
        //System.out.println(Strings.join(split).with(","));
        starsText = split[split.length - 2];

        //System.out.println(starsText);

        if (doc.text().contains("Prospect rating :")) {
            return blueValueOf(starsText);
        } else {
            return goldValueOf(starsText);
        }
    }

    private static StarRating blueValueOf(String stars) {
        return new StarRating(Type.BLUE, number(stars), half(stars));

    }

    private static StarRating goldValueOf(String stars) {
        return new StarRating(Type.GOLD, number(stars), half(stars));
    }

    private static Integer number(String stars) {
        return Integer.parseInt(stars.trim().substring(0, 1));
    }

    private static Boolean half(String stars) {
        return stars.contains(".5");
    }

}
