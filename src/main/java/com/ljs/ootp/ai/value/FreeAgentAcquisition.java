package com.ljs.ootp.ai.value;

import com.google.common.base.Function;
import com.ljs.ootp.ai.player.Player;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public class FreeAgentAcquisition {

    private final Player fa;

    private final Player release;

    private FreeAgentAcquisition(Player fa, Player release) {
        this.fa = fa;
        this.release = release;
    }

    public Player getFreeAgent() {
        return fa;
    }

    public Player getRelease() {
        return release;
    }

    public static FreeAgentAcquisition create(Player fa, Player release) {
        return new FreeAgentAcquisition(fa, release);
    }

    public final static class Meta {
        private Meta() { }

        public static Function<FreeAgentAcquisition, Player> getRelease() {
            return new Function<FreeAgentAcquisition, Player>() {
                @Override
                public Player apply(FreeAgentAcquisition faa) {
                    Assertions.assertThat(faa).isNotNull();
                    return faa.getRelease();
                }
            };
        }

    }

}
