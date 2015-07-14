require 'spec_helper'

require 'java'

java_import com.ljs.ootp.ai.report.LeagueBattingReport
java_import com.ljs.ootp.ai.site.Site;
java_import com.ljs.ootp.ai.stats.BattingStats;

java_import java.io.PrintWriter
java_import java.io.StringWriter

RSpec.describe LeagueBattingReport do
  let(:stats) { BattingStats.new }
  let(:site) { double('Site', getLeagueBatting: stats) }

  subject(:report) { LeagueBattingReport.create site }

  describe '#print' do
    let(:out) { StringWriter.new }
    let(:writer) { PrintWriter.new out }

    subject! { report.print writer }

    it { expect(out.to_s.empty?).to eq(false) }
  end
end

