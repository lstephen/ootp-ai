require 'spec_helper'

java_import com.github.lstephen.ootp.ai.data.Id;
java_import com.github.lstephen.ootp.ai.player.PlayerId;
java_import com.github.lstephen.ootp.ai.ootp5.site.SalaryImpl;
java_import com.github.lstephen.ootp.ai.site.Version;
java_import com.github.lstephen.ootp.extract.html.loader.JsoupLoader;
java_import com.google.common.io.Resources;

RSpec.describe SalaryImpl do
  let(:id) { Id.valueOf(team_id) }

  let(:html) { "com/github/lstephen/ootp/ai/#{version.to_s.downcase}/site/team#{team_id}sa.html" }
  let(:resource) { Resources.getResource(html) }
  let(:document) { JsoupLoader.new.load resource }

  let(:site) do
    double('Site',
        getName: '')
  end

  before(:each) do
    allow(site).to receive_message_chain(:getPage, :load).and_return document
  end

  subject { SalaryImpl.new site, id }

  context 'OOTP5' do
    let(:version) { Version::OOTP5 }

    context 'LBB' do
      let(:team_id) { 3 }

      it { expect(subject.getCurrentSalary(PlayerId.new 'p1682')).to eq(3_021_429) }

      it { expect(subject.getCurrentSalary(PlayerId.new 'p53')).to eq(885_714) }
      it { expect(subject.getNextSalary(PlayerId.new 'p53')).to eq(990_000) }
    end
  end
end


