package com.ljs.scratch.ootp.ootp5.site;

import com.google.common.base.Optional;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.player.ratings.BattingRatings;
import com.ljs.scratch.ootp.site.Version;
import java.io.IOException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author lstephen
 */
public class TestSinglePlayer {

    private static final PlayerId ID =
        new PlayerId(RandomStringUtils.randomAlphanumeric(10));

    private static final String OOTP5_HITTER =
        "/com/ljs/scratch/ootp/ootp5/site/andrew_whetzel.html";

    private SinglePlayer singlePlayer;

    private final MockSite site = new MockSite();

    @Before
    public void setUp() {
        site.expectGetPage(ID.unwrap() + ".html");
        site.type(Version.OOTP5);
        site.abilityScale(ZeroToTen.scale());
        site.potentialScale(PotentialRating.scale());

        singlePlayer = new SinglePlayer();
        singlePlayer.setSite(site.toMock());
    }

    @Test
    public void testOotp5Hitter() throws IOException {
        site.onLoadPage(OOTP5_HITTER);

        site.type(Version.OOTP5);

        Mockito
            .when(site.toMock().isInjured(Mockito.notNull(Player.class)))
            .thenReturn(false);

        Mockito
            .when(site.toMock().isFutureFreeAgent(Mockito.notNull(Player.class)))
            .thenReturn(false);

        Mockito
            .when(
                site.toMock().getTeamTopProspectPosition(
                    Mockito.notNull(PlayerId.class)))
            .thenReturn(Optional.<Integer>absent());

        Mockito
            .when(site.toMock().getSalary(Mockito.notNull(Player.class)))
            .thenReturn("");

        Player extracted = singlePlayer.get(ID);

        Assert.assertEquals("Andrew Whetzel", extracted.getName());
        Assert.assertEquals(32, extracted.getAge());
        Assert.assertEquals("Detroit Tigers", extracted.getTeam());

        BattingRatings<Integer> expectedRatingsVsRight = BattingRatings
            .builder(ZeroToTen.scale())
            .contact(8)
            .gap(5)
            .power(11)
            .eye(8)
            .build();

        BattingRatings<Integer> expectedRatingsVsLeft = BattingRatings
            .builder(ZeroToTen.scale())
            .contact(8)
            .gap(5)
            .power(11)
            .eye(9)
            .build();

        Assert.assertEquals(
            expectedRatingsVsRight,
            extracted.getBattingRatings().getVsRight());

        Assert.assertEquals(
            expectedRatingsVsLeft,
            extracted.getBattingRatings().getVsLeft());

    }

}
