package com.github.lstephen.ootp.ai.report;

import com.ljs.ootp.ai.report.TeamReport;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.stats.PitchingStats;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.assertj.core.api.Assertions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestTeamReport {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Site site;

  @Test
  public void whenNoLeagues() {
    String title = "TEST_TITLE";

    TeamReport report = TeamReport.create(title, null, site, null);

    Mockito
      .when(site.getPitcherSelectionMethod().getEraEstimate(Mockito.isA(PitchingStats.class)))
      .thenReturn(3.00);

    StringWriter out = new StringWriter();

    report.print(new PrintWriter(out));

    Assertions.assertThat(out.toString()).isNotEmpty().contains(title);
  }

}

