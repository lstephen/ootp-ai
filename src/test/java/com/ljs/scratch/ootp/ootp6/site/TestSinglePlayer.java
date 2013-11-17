package com.ljs.scratch.ootp.ootp6.site;

import com.ljs.ootp.ai.ootp6.site.TwoToEight;
import com.ljs.ootp.ai.ootp6.site.OneToTwenty;
import com.google.common.base.Optional;
import com.ljs.scratch.ootp.ootp5.site.MockSite;
import com.ljs.ootp.ai.ootp5.site.SinglePlayer;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.player.ratings.BattingRatings;
import com.ljs.ootp.ai.site.Version;
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

    private static final String OOTP6_HITTER =
        "/com/ljs/scratch/ootp/ootp6/site/victor_plata.html";

    private static final String OOTP6_PITCHER =
        "/com/ljs/scratch/ootp/ootp6/site/isidoro_amell.html";

    private SinglePlayer singlePlayer;

    private final MockSite site = new MockSite();

    @Before
    public void setUp() {
        site.expectGetPage(ID.unwrap() + ".html");
        site.abilityScale(OneToTwenty.scale());
        site.potentialScale(TwoToEight.scale());

        singlePlayer = new SinglePlayer();
        singlePlayer.setSite(site.toMock());
        singlePlayer.setSalarySource(site.toMock());
    }

    @Test
    public void testOotp6Hitter() throws IOException {
        site.onLoadPage(OOTP6_HITTER);

        site.name("TWML");
        site.type(Version.OOTP6);

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

        Assert.assertEquals("Victor Plata", extracted.getName());
        Assert.assertEquals(31, extracted.getAge());
        Assert.assertEquals("Port Adelaide Magpies", extracted.getTeam());

        BattingRatings expectedRatings = BattingRatings
            .builder(OneToTwenty.scale())
            .contact(11)
            .gap(15)
            .power(20)
            .eye(20)
            .build();

        Assert.assertEquals(
            expectedRatings,
            extracted.getBattingRatings().getVsRight());

        Assert.assertEquals(
            expectedRatings,
            extracted.getBattingRatings().getVsLeft());
    }

    @Test
    public void testOotp6Pitcher() throws IOException {
        site.onLoadPage(OOTP6_PITCHER);

        site.name("TWML");
        site.type(Version.OOTP6);

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

        Assert.assertEquals("Isidoro Amell", extracted.getName());
        Assert.assertEquals(30, extracted.getAge());
        Assert.assertEquals("Port Adelaide Magpies", extracted.getTeam());

        BattingRatings expectedRatings = BattingRatings
            .builder(OneToTwenty.scale())
            .contact(5)
            .gap(6)
            .power(3)
            .eye(3)
            .build();

        Assert.assertEquals(
            expectedRatings,
            extracted.getBattingRatings().getVsRight());

        Assert.assertEquals(
            expectedRatings,
            extracted.getBattingRatings().getVsLeft());

    }

}
