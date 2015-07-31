require 'spec_helper'

require 'player/examples'
require 'player/ratings/contexts'


java_import com.github.lstephen.ootp.ai.player.PlayerId;
java_import com.github.lstephen.ootp.ai.ootp5.site.SinglePlayer;
java_import com.github.lstephen.ootp.ai.rating.AToE;
java_import com.github.lstephen.ootp.ai.rating.OneToFive;
java_import com.github.lstephen.ootp.ai.rating.OneToTwenty;
java_import com.github.lstephen.ootp.ai.rating.Potential;
java_import com.github.lstephen.ootp.ai.rating.Rating;
java_import com.github.lstephen.ootp.ai.rating.TwoToEight;
java_import com.github.lstephen.ootp.ai.rating.ZeroToTen;
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
        getName: '',
        getAbilityRatingScale: ability_scale,
        getPotentialRatingScale: potential_scale,
        getBuntScale: bunt_scale,
        getRunningScale: running_scale,
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
      let(:ability_scale) { ZeroToTen.new }
      let(:potential_scale) { Potential.new }
      let(:bunt_scale) { AToE.new }
      let(:running_scale) { AToE.new }

      context 'Elijah Chausse' do
        let(:file) { 'elijah_chausse' }

        it_behaves_like :batter, 'Elijah Chausse', 26

        its(:bunt_for_hit_rating) { is_expected.to eq(Rating.new('E', AToE.new)) }
        its(:stealing_rating) { is_expected.to eq(Rating.new('D', AToE.new)) }

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

      let(:bunt_scale) { OneToFive.new }
      let(:running_scale) { OneToTen.new }

      context 'TWML' do
        let(:ability_scale) { OneToTwenty.new }
        let(:potential_scale) { TwoToEight.new }

        context 'Victor Plata' do
          let(:file) { 'victor_plata' }

          it_behaves_like :batter, 'Victor Plata', 34

          its(:bunt_for_hit_rating) { is_expected.to eq(Rating.new(2, OneToFive.new)) }
          its(:stealing_rating) { is_expected.to eq(Rating.new(2, OneToTen.new)) }

          context '#batting_ratings', :property => :batting_ratings do
            its(:vs_left) { is_expected.to be_batting_ratings 9, 14, 20, 20, 7 }
            its(:vs_right) { is_expected.to be_batting_ratings 9, 14, 20, 20, 7 }
          end

          context '#defensive_ratings', :property => :defensive_ratings do
            its(:position_scores) { is_expected.to eq('-2------') }
          end
        end
      end

      context 'Old BTH' do
        let(:ability_scale) { OneToOneHundred.new }
        let(:potential_scale) { OneToTen.new }

        context 'Alonso Ayo' do
          let(:file) { 'alonso_ayo' }

          it_behaves_like :batter, 'Alonso Ayo', 27

          its(:bunt_for_hit_rating) { is_expected.to eq(Rating.new(2, OneToFive.new)) }
          its(:stealing_rating) { is_expected.to eq(Rating.new(2, OneToTen.new)) }

          context '#batting_ratings', :property => :batting_ratings do
            its(:vs_left) { is_expected.to be_batting_ratings 88, 99, 100, 100, 61 }
            its(:vs_right) { is_expected.to be_batting_ratings 88, 99, 100, 100, 61 }
          end

          context '#defensive_ratings', :property => :defensive_ratings do
            its(:position_scores) { is_expected.to eq('-3665---') }
          end
        end
      end

     context 'BTHUSTLE' do
        let(:ability_scale) { OneToOneHundred.new }
        let(:potential_scale) { OneToTen.new }

        context 'Earl Yi' do
          let(:file) { 'earl_yi' }

          it_behaves_like :batter, 'Earl Yi', 31

          its(:bunt_for_hit_rating) { is_expected.to eq(Rating.new(1, OneToFive.new)) }

          context '#batting_ratings', :property => :batting_ratings do
            its(:vs_left) { is_expected.to be_batting_ratings 80, 65, 100, 100, 65 }
            its(:vs_right) { is_expected.to be_batting_ratings 65, 59, 100, 100, 62 }
          end

          context '#defensive_ratings', :property => :defensive_ratings do
            its(:position_scores) { is_expected.to eq('-3-66---') }
          end
        end
      end
    end
  end
end

