package com.ljs.scratch.ootp.html;

import com.google.common.base.Optional;
import com.ljs.scratch.ootp.html.page.Page;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.PlayerId;
import com.ljs.scratch.ootp.ratings.BattingRatings;
import com.ljs.scratch.ootp.site.SiteDefinition;
import com.ljs.scratch.ootp.site.Version;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.easymock.EasyMock;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.api.extension.listener.AnnotationEnabler;
import org.powermock.core.classloader.annotations.PowerMockListener;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author lstephen
 */
@RunWith(PowerMockRunner.class)
@PowerMockListener(AnnotationEnabler.class)
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

    @Mock
    private Site site;

    @Mock
    private SiteDefinition siteDefinition;

    @Mock
    private Page page;

    @Before
    public void setUp() {
        EasyMock
            .expect(site.getPage(ID.unwrap() + ".html"))
            .andStubReturn(page);

        EasyMock.expect(site.getDefinition()).andStubReturn(siteDefinition);

        singlePlayer = new SinglePlayer(site, ID);
    }

    @Test
    public void testOotp5Hitter() throws IOException {
        EasyMock.expect(page.load()).andStubReturn(loadPage(OOTP5_HITTER));

        EasyMock.expect(site.getType()).andStubReturn(Version.OOTP5);

        EasyMock
            .expect(site.isInjured(EasyMock.notNull(Player.class)))
            .andStubReturn(false);

        EasyMock
            .expect(site.isFutureFreeAgent(EasyMock.notNull(Player.class)))
            .andStubReturn(false);

        EasyMock
            .expect(
                site.getTeamTopProspectPosition(
                    EasyMock.notNull(PlayerId.class)))
            .andStubReturn(Optional.<Integer>absent());

        EasyMock
            .expect(site.getSalary(EasyMock.notNull(Player.class)))
            .andStubReturn("");

        PowerMock.replayAll();

        Player extracted = singlePlayer.extract();

        PowerMock.verifyAll();

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
        EasyMock.expect(page.load()).andStubReturn(loadPage(OOTP6_HITTER));

        EasyMock.expect(site.getName()).andStubReturn("TWML");
        EasyMock.expect(site.getType()).andStubReturn(Version.OOTP6);

        EasyMock
            .expect(site.isInjured(EasyMock.notNull(Player.class)))
            .andStubReturn(false);

        EasyMock
            .expect(site.isFutureFreeAgent(EasyMock.notNull(Player.class)))
            .andStubReturn(false);

        EasyMock
            .expect(
                site.getTeamTopProspectPosition(
                    EasyMock.notNull(PlayerId.class)))
            .andStubReturn(Optional.<Integer>absent());

        EasyMock
            .expect(site.getSalary(EasyMock.notNull(Player.class)))
            .andStubReturn("");

        PowerMock.replayAll();

        Player extracted = singlePlayer.extract();

        PowerMock.verifyAll();

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
        EasyMock.expect(page.load()).andStubReturn(loadPage(OOTP6_PITCHER));

        EasyMock.expect(site.getName()).andStubReturn("TWML");
        EasyMock.expect(site.getType()).andStubReturn(Version.OOTP6);

        EasyMock
            .expect(site.isInjured(EasyMock.notNull(Player.class)))
            .andStubReturn(false);

        EasyMock
            .expect(site.isFutureFreeAgent(EasyMock.notNull(Player.class)))
            .andStubReturn(false);

        EasyMock
            .expect(
                site.getTeamTopProspectPosition(
                    EasyMock.notNull(PlayerId.class)))
            .andStubReturn(Optional.<Integer>absent());

        EasyMock
            .expect(site.getSalary(EasyMock.notNull(Player.class)))
            .andStubReturn("");

        PowerMock.replayAll();

        Player extracted = singlePlayer.extract();

        PowerMock.verifyAll();

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

    private Document loadPage(String name) throws IOException {
        InputStream in = getClass().getResourceAsStream(name);

        Assert.assertNotNull(in);

        try {
            Document doc = Jsoup.parse(in, null, "");

            Assert.assertNotNull(doc);

            return doc;
        } finally {
            in.close();
        }
    }

}
