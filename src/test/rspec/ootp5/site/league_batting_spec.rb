
require 'java'
require 'spec_helper'

java_import com.github.lstephen.ootp.ai.ootp5.site.LeagueBatting

java_import org.joda.time.LocalDate

RSpec.describe LeagueBatting do

  let(:league) { 'National' }

  let(:html) { "com/github/lstephen/ootp/ai/ootp6/site/leagueb.html" }
  let(:resource) { Resources.asByteSource(Resources.getResource(html)) }

  let(:document) do
    with_closeable(resource.openStream) { |is| Documents.load is }
  end

  let(:page) do
    double('Page')
  end

  let(:site) do
    double('Site',
      :getName => 'TEST'
    )
  end

  before(:each) { allow(page).to receive(:load).and_return document }
  before(:each) { allow(site).to receive(:getPage).and_return page }

  subject(:league_batting) { LeagueBatting.new site, league }

  describe '#extractTotal' do

    subject { league_batting.extractTotal }

    it { is_expected.to_not be_nil }
    it { expect(subject.runs).to eq(8469) }
    its(:at_bats) { is_expected.to eq(59351) }
    its(:runs) { is_expected.to eq(8469) }
    its(:hits) { is_expected.to eq(16370) }
    its(:doubles) { is_expected.to eq(3287) }
    its(:triples) { is_expected.to eq(220) }
    its(:walks) { is_expected.to eq(4815) }
    its(:home_runs) { is_expected.to eq(1656) }
  end

  describe '#extractDate' do
    subject { league_batting.extractDate }

    it { is_expected.to eq(LocalDate.new(2026, 8, 1)) }
  end

end

