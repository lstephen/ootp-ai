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
    let(:id) { PlayerId.new('ELIJAH_CHAUSSE') }

    let(:file) { 'elijah_chausse' }
    let(:html) { "com/github/lstephen/ootp/ai/ootp5/site/#{file}.html" }
    let(:resource) { Resources.getResource(html) }
    let(:document) { JsoupLoader.new.load resource }

    let(:ability_scale) { ZeroToTen.scale }

    let(:site) do
      double('Site',
        getAbilityRatingScale: ability_scale,
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

    subject(:player) { single_player.get(id) }

    it { is_expected.to_not be(nil) }
    its(:name) { is_expected.to eq('Elijah Chausse') }

    context '#batting_ratings' do
      let(:scale) { ability_scale }
      subject { player.batting_ratings }

      its(:vs_left) { is_expected.to be_batting_ratings 5, 4, 7, 8, 3 }
      its(:vs_right) { is_expected.to be_batting_ratings 6, 5, 8, 9, 4 }
    end

    context '#defensive_ratings' do
      subject { player.defensive_ratings }

      its(:position_scores) { is_expected.to eq('-2---3-5') }
    end
  end
end

