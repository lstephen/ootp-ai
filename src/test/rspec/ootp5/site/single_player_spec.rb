require 'spec_helper'

require 'java'

java_import com.github.lstephen.ootp.ai.player.PlayerId;
java_import com.github.lstephen.ootp.ai.ootp5.site.SinglePlayer;
java_import com.github.lstephen.ootp.ai.site.Version;

java_import com.google.common.base.Optional;
java_import com.google.common.io.Resources;

java_import com.github.lstephen.ootp.extract.html.loader.JsoupLoader;
java_import com.github.lstephen.ootp.extract.html.ootp5.rating.PotentialRating;
java_import com.github.lstephen.ootp.extract.html.ootp5.rating.ZeroToTen;
java_import org.jsoup.Jsoup;

RSpec.describe SinglePlayer do

  subject(:single_player) { SinglePlayer.new }

  describe '#get' do
    let(:id) { PlayerId.new('TEST_ID') }

    let(:player) { 'elijah_chausse' }
    let(:html) { "com/github/lstephen/ootp/ai/ootp5/site/#{player}.html" }
    let(:resource) { Resources.asByteSource(Resources.getResource(html)) }

    let(:document) do
      with_closeable(resource.openStream) { |is| JsoupLoader.new.load is }
    end

    let(:site) do
      double('Site',
        getAbilityRatingScale: ZeroToTen.scale,
        getPotentialRatingScale: PotentialRating.scale,
        getType: Version::OOTP5,
        getDefinition: nil,
        isInjured: false,
        isFutureFreeAgent: false,
        getTeamTopProspectPosition: Optional.absent)
    end

    before(:each) do
      allow(site).to receive_message_chain(:getPage, :load).and_return document
    end

    before(:each) { single_player.site = site }

    before(:each) { single_player.salary_source = double('SalarySource', getSalary: '$SALARY') }

    subject { single_player.get(id) }

    it { is_expected.to_not be(nil) }
  end
end

def with_closeable(cl)
  yield cl
ensure
  cl.close
end

