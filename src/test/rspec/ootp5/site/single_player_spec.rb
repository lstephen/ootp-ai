require 'spec_helper'

require 'player/ratings/contexts'


java_import com.github.lstephen.ootp.ai.player.PlayerId;
java_import com.github.lstephen.ootp.ai.ootp5.site.SinglePlayer;
java_import com.github.lstephen.ootp.ai.rating.PotentialRating;
java_import com.github.lstephen.ootp.ai.rating.ZeroToTen;
java_import com.github.lstephen.ootp.ai.rating.OneToTwenty;
java_import com.github.lstephen.ootp.ai.rating.TwoToEight;
java_import com.github.lstephen.ootp.ai.site.Version;
java_import com.github.lstephen.ootp.extract.html.loader.JsoupLoader;

java_import com.google.common.base.Optional;
java_import com.google.common.io.Resources;

java_import org.jsoup.Jsoup;

RSpec.describe SinglePlayer do

  subject(:single_player) { SinglePlayer.new }

  describe '#get' do
    let(:id) { PlayerId.new(file.upcase) }

    let(:html) { "com/github/lstephen/ootp/ai/#{version.to_s.downcase}/site/#{file}.html" }
    let(:resource) { Resources.getResource(html) }
    let(:document) { JsoupLoader.new.load resource }

    let(:site) do
      double('Site',
        getAbilityRatingScale: ability_scale,
        getPotentialRatingScale: potential_scale,
        getType: version,
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

    context 'OOTP5' do
      let(:version) { Version::OOTP5 }
      let(:ability_scale) { ZeroToTen.scale }
      let(:potential_scale) { PotentialRating.scale }

      context 'Elijah Chausse' do
        let(:file) { 'elijah_chausse' }

        it { is_expected.to_not be(nil) }
        its(:name) { is_expected.to eq('Elijah Chausse') }

        context '#batting_ratings', :property => :batting_ratings do
          its(:vs_left) { is_expected.to be_batting_ratings 5, 4, 7, 8, 3 }
          its(:vs_right) { is_expected.to be_batting_ratings 6, 5, 8, 9, 4 }
        end

        context '#defensive_ratings', :property => :defensive_ratings do
          its(:position_scores) { is_expected.to eq('-2---3-5') }
        end
      end
    end

    context 'OOTP6' do
      let(:version) { Version::OOTP6 }
      let(:ability_scale) { OneToTwenty.new }
      let(:potential_scale) { TwoToEight.scale }

      context 'Victor Plata' do
        let(:file) { 'victor_plata' }

        it { is_expected.to_not be(nil) }
        its(:name) { is_expected.to eq('Victor Plata') }

        context '#batting_ratings', :property => :batting_ratings do
          its(:vs_left) { is_expected.to be_batting_ratings 9, 14, 20, 20, 7 }
          its(:vs_right) { is_expected.to be_batting_ratings 9, 14, 20, 20, 7 }
        end

        context '#defensive_ratings', :property => :defensive_ratings do
          its(:position_scores) { is_expected.to eq('-2------') }
        end
      end

    end
  end
end

