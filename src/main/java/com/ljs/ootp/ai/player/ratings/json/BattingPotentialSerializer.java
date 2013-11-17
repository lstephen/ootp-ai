package com.ljs.ootp.ai.player.ratings.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.ljs.ootp.ai.player.ratings.BattingRatings;
import com.ljs.ootp.ai.site.Site;
import java.io.IOException;

/**
 *
 * @author lstephen
 */
public class BattingPotentialSerializer extends JsonSerializer<BattingRatings> {

    private static Site site;

    public static void setSite(Site site) {
        BattingPotentialSerializer.site = site;
    }

    @Override
    public void serialize(
        BattingRatings ratings, JsonGenerator json, SerializerProvider provider)
        throws IOException {

        json.writeObject(ratings);
    }

}
