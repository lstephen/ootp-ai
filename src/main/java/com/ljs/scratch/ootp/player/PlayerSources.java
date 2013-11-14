package com.ljs.scratch.ootp.player;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class PlayerSources {

    private PlayerSources() { }

    public static Iterable<Player> get(PlayerSource src, PlayerId... ids) {
        return get(src, Arrays.asList(ids));

    }

    public static Iterable<Player> get(PlayerSource src, Iterable<PlayerId> ids) {
        Set<Player> ps = Sets.newHashSet();

        for (PlayerId id : ids) {
            ps.add(src.get(id));
        }

        return ps;
    }

}
