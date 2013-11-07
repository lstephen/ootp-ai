package com.ljs.scratch.ootp.player.ratings.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.ljs.scratch.ootp.player.ratings.BattingRatings;
import com.ljs.scratch.ootp.site.Site;
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
