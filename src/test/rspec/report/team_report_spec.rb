require 'spec_helper'

require 'java'

java_import com.github.lstephen.ootp.ai.report.TeamReport
java_import com.github.lstephen.ootp.ai.site.Site;
java_import com.github.lstephen.ootp.ai.stats.BattingStats;
java_import com.github.lstephen.ootp.ai.stats.FipBaseRuns;
java_import com.github.lstephen.ootp.ai.stats.PitcherOverall;
java_import com.github.lstephen.ootp.ai.stats.PitchingStats;

java_import java.io.PrintWriter
java_import java.io.StringWriter

RSpec.describe TeamReport do
  let(:title) { 'TEST_TITLE' }

  let(:stats) { PitchingStats.new }
  let(:pitcher_overall) { PitcherOverall::FIP }

  before(:each) { FipBaseRuns.factor = 0 }
  before(:each) { FipBaseRuns.leagueContext = BattingStats.new }

  let(:league_structure) { double('LeagueStructure', :getLeagues => []) }

  let(:site) do
    double('Site',
      getLeaguePitching: stats,
      getPitcherSelectionMethod: pitcher_overall,
      getLeagueStructure: league_structure)
  end

  subject(:report) { TeamReport.create title, nil, site, nil }

  describe '#print' do
    let(:out) { StringWriter.new }
    let(:writer) { PrintWriter.new out }

    subject! { report.print writer }

    it { expect(out.to_s.empty?).to_not be_truthy }
    it { expect(out.to_s).to include(title) }
  end
end

