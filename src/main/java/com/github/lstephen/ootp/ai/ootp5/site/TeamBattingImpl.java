package com.github.lstephen.ootp.ai.ootp5.site;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.PlayerId;
import com.github.lstephen.ootp.ai.roster.Team;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.ai.stats.BattingStats;
import com.github.lstephen.ootp.ai.stats.RunningStats;
import com.github.lstephen.ootp.ai.stats.TeamStats;
import com.github.lstephen.scratch.util.ElementsUtil;
import com.google.common.collect.ImmutableMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** @author lstephen */
public class TeamBattingImpl extends SingleTeamStats<BattingStats> {

  private static final int HOMERUN_IDX = 5;
  private static final int ATBAT_IDX = 1;
  private static final int HITS_IDX = 2;
  private static final int DOUBLES_IDX = 3;
  private static final int TRIPLES_IDX = 4;
  private static final int WALKS_IDX = 8;
  private static final int KS_IDX = 9;

  private final BattingStats leagueBatting;

  public TeamBattingImpl(Site site, Id<Team> team) {
    super(site.extractTeam(), site.getPage("team" + team.get() + "b.html"));

    leagueBatting = site.getLeagueBatting();
  }

  public TeamStats.Batting extract() {
    TeamStats<BattingStats> ss = super.extract();

    return TeamStats.Batting.create(ss, extractRunningStats());
  }

  private ImmutableMap<Player, RunningStats> extractRunningStats() {
    Document doc = getDocument();

    Elements el = doc.select("tr:has(td:contains(Overall Batting)) + tr:has(td:contains(SB%))");
    Elements rows = el.select("tbody tr:has(a)");

    ImmutableMap.Builder<Player, RunningStats> stats = ImmutableMap.builder();

    for (Element row : rows) {
      PlayerId id = new PlayerId(row.select("a[href]").get(0).attr("href").replaceAll(".html", ""));

      if (getTeam().containsPlayer(id)) {
        RunningStats rs = new RunningStats();
        rs.setStolenBases(ElementsUtil.getInteger(row.children(), 11));
        rs.setCaughtStealing(ElementsUtil.getInteger(row.children(), 12));
        stats.put(getTeam().get(id), rs);
      }
    }

    return stats.build();
  }

  @Override
  protected BattingStats zero() {
    BattingStats battingStats = new BattingStats();
    battingStats.setLeagueBatting(leagueBatting);
    return battingStats;
  }

  @Override
  protected BattingStats extractStatsRow(Elements data) {
    BattingStats batting = zero();
    batting.setHomeRuns(ElementsUtil.getInteger(data, HOMERUN_IDX));
    batting.setAtBats(ElementsUtil.getInteger(data, ATBAT_IDX));
    batting.setHits(ElementsUtil.getInteger(data, HITS_IDX));
    batting.setDoubles(ElementsUtil.getInteger(data, DOUBLES_IDX));
    batting.setTriples(ElementsUtil.getInteger(data, TRIPLES_IDX));
    batting.setWalks(ElementsUtil.getInteger(data, WALKS_IDX));
    batting.setKs(ElementsUtil.getInteger(data, KS_IDX));
    return batting;
  }

  @Override
  protected ImmutableMap<Player, BattingStats> extractStatsVsLeft(Document doc) {

    Elements vLhpStats = doc.select("tr:has(td:contains(vs. LHP)) + tr");
    return extractStats(vLhpStats);
  }

  @Override
  protected ImmutableMap<Player, BattingStats> extractStatsVsRight(Document doc) {

    Elements vRhpStats = doc.select("tr:has(td:contains(vs. RHP)) + tr");
    return extractStats(vRhpStats);
  }
}
