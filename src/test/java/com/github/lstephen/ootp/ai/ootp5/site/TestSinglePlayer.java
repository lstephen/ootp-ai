package com.github.lstephen.ootp.ai.ootp5.site;

import com.ljs.ootp.ai.ootp5.site.SalarySource;
import com.ljs.ootp.ai.ootp5.site.SinglePlayer;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.PlayerId;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.site.Version;
import com.ljs.ootp.extract.html.rating.Scale;
import com.ljs.ootp.extract.html.ootp5.rating.PotentialRating;
import com.ljs.ootp.extract.html.ootp5.rating.ZeroToTen;

import java.io.InputStream;
import java.io.IOException;

import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import org.assertj.core.api.Assertions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestSinglePlayer {

  private static final String TEST_ID = "TEST_ID";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Site site;

  @Mock
  private SalarySource salaries;

  private SinglePlayer subject;

  @Before
  public void setUpSubject() {
    subject = new SinglePlayer();
    subject.setSite(site);
    subject.setSalarySource(salaries);
  }

  private void setUpOotp5Site() {
    Mockito.when(site.getType()).thenReturn(Version.OOTP5);

    Mockito.doReturn(ZeroToTen.scale()).when(site).getAbilityRatingScale();
    Mockito.doReturn(PotentialRating.scale()).when(site).getPotentialRatingScale();
  }

  @Test
  public void loadElijahChausse() {
    setUpOotp5Site();

    Mockito.when(site.getPage(TEST_ID + ".html").load()).thenReturn(load("elijah_chausse"));
    Mockito.when(salaries.getSalary(Mockito.isA(Player.class))).thenReturn("$SALARY");

    Player p = subject.get(new PlayerId(TEST_ID));

    Assertions.assertThat(p).isNotNull();
    Assertions.assertThat(p.getShortName()).isEqualTo("Chausse, E");
  }

  private Document load(String player) {
    String name = "com/github/lstephen/ootp/ai/ootp5/site/" + player + ".html";

    ByteSource source = Resources.asByteSource(Resources.getResource(name));

    try (InputStream is = source.openStream()) {
      return Jsoup.parse(is, null, "");
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}

