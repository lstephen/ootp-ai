package com.ljs.scratch.ootp.html.ootpFiveAndSix;

import com.ljs.scratch.ootp.ootp5.site.SinglePlayer;
import com.google.common.base.Optional;
import com.ljs.scratch.ootp.html.MockSite;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.ratings.BattingRatings;
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
        "/com/ljs/scratch/ootp/html/andrew_whetzel_ootp5.html";

    private static final String OOTP6_HITTER =
        "/com/ljs/scratch/ootp/html/victor_plata_ootp6.html";

    private static final String OOTP6_PITCHER =
        "/com/ljs/scratch/ootp/html/isidoro_amell_ootp6.html";

    private SinglePlayer singlePlayer;

    private final MockSite site = new MockSite();

    @Before
    public void setUp() {
        site.expectGetPage(ID.unwrap() + ".html");

        singlePlayer = new SinglePlayer(site.toMock(), ID);
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

        Player extracted = singlePlayer.extract();

        Assert.assertEquals("Andrew Whetzel", extracted.getName());
        Assert.assertEquals(32, extracted.getAge());
        Assert.assertEquals("Detroit Tigers", extracted.getTeam());

        BattingRatings expectedRatingsVsRight = BattingRatings
            .builder()
            .contact(8)
            .gap(5)
            .power(11)
            .eye(8)
            .build();

        BattingRatings expectedRatingsVsLeft = BattingRatings
            .builder()
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

        Player extracted = singlePlayer.extract();

        Assert.assertEquals("Victor Plata", extracted.getName());
        Assert.assertEquals(31, extracted.getAge());
        Assert.assertEquals("Port Adelaide Magpies", extracted.getTeam());

        BattingRatings expectedRatings = BattingRatings
            .builder()
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

        Player extracted = singlePlayer.extract();

        Assert.assertEquals("Isidoro Amell", extracted.getName());
        Assert.assertEquals(30, extracted.getAge());
        Assert.assertEquals("Port Adelaide Magpies", extracted.getTeam());

        BattingRatings expectedRatings = BattingRatings
            .builder()
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
