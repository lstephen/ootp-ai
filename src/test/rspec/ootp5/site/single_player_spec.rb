require 'spec_helper'

require 'player/examples'
require 'player/ratings/contexts'


java_import com.github.lstephen.ootp.ai.player.PlayerId;
java_import com.github.lstephen.ootp.ai.player.ratings.PlayerRatings;
java_import com.github.lstephen.ootp.ai.ootp5.site.SinglePlayer;
java_import com.github.lstephen.ootp.ai.rating.AToE;
java_import com.github.lstephen.ootp.ai.rating.OneToFive;
java_import com.github.lstephen.ootp.ai.rating.OneToTwenty;
java_import com.github.lstephen.ootp.ai.rating.Potential;
java_import com.github.lstephen.ootp.ai.rating.Rating;
java_import com.github.lstephen.ootp.ai.rating.TwoToEight;
java_import com.github.lstephen.ootp.ai.rating.ZeroToTen;
java_import com.github.lstephen.ootp.ai.site.Version;
java_import com.github.lstephen.ootp.ai.stats.SplitPercentages
java_import com.github.lstephen.ootp.extract.html.loader.JsoupLoader;
java_import com.google.common.base.Optional;
java_import com.google.common.io.Resources;
java_import org.jsoup.Jsoup;

class MockSplitPercentages < SplitPercentages
  def getVsLhbPercentage
    0.3
  end

  def getVsRhbPercentage
    0.7
  end
end

RSpec.describe SinglePlayer do

  subject(:single_player) { SinglePlayer.new }

  describe '#get' do
    let(:id) { PlayerId.new(file.upcase) }

    let(:html) { "com/github/lstephen/ootp/ai/#{version.to_s.downcase}/site/#{file}.html" }
    let(:resource) { Resources.getResource(html) }
    let(:document) { JsoupLoader.new.load(resource).blockingGet }

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

    let(:ratings_definition) do
      double('RatingsDefintion',
         isFreezeOneRatings: false)
    end

    before(:each) do
      allow(site).to receive_message_chain(:getPage, :load).and_return document
    end

    before(:each) { single_player.site = site }
    before(:each) { PlayerRatings.percentages = MockSplitPercentages.new }

    before(:each) { single_player.salary_source = double('SalarySource', getSalary: '$SALARY') }

    subject(:player) do
      p = single_player.get(id)
      p.ratings_definition = ratings_definition
      p
    end

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
        its('years_of_minor_leagues.get') { is_expected.to eq(2) }

        context '#batting_ratings', :property => :batting_ratings do
          its(:vs_left) { is_expected.to be_batting_ratings 5, 4, 7, 8, 3 }
          its(:vs_right) { is_expected.to be_batting_ratings 6, 5, 8, 9, 4 }
        end

        context '#defensive_ratings', :property => :defensive_ratings do
          its(:position_scores) { is_expected.to eq('-2---325') }
        end
      end

      context 'Calvin Rosati' do
        let(:file) { 'calvin_rosati' }

        it_behaves_like :pitcher, 'Calvin Rosati', 26

        context '#pitching_ratings', :property => :pitching_ratings do
          its(:vs_left) { is_expected.to be_ootp5_pitching_ratings 5, 7, 8, 6, 10 }
          its(:vs_right) { is_expected.to be_ootp5_pitching_ratings 7, 7, 9, 6, 10 }

          its("vs_right.endurance") { is_expected.to eq(6) }
          its("vs_right.ground_ball_pct.get") { is_expected.to eq(48) }
          its("vs_right.runs.get") { is_expected.to eq(65) }
        end

        context '#pitching_potential_ratings' do
          let(:scale) { OneToOneHundred.new }
          subject { player.pitching_potential_ratings }

          #its(:vs_left) { is_expected.to be_ootp5_pitching_ratings 56, 70, 83, 60, 100 }
          #its(:vs_right) { is_expected.to be_ootp5_pitching_ratings 76, 70, 93, 60, 100 }

          [ 'vs_left', 'vs_right' ].each do |vs|
            its("#{vs}.endurance") { is_expected.to eq(6) }
            its("#{vs}.ground_ball_pct.get") { is_expected.to eq(48) }
            its("#{vs}.runs") { is_expected.to_not be_present }
          end
        end
      end

      context 'Earl Putz' do
        let(:file) { 'earl_putz' }

        it_behaves_like :pitcher, 'Earl Putz', 20

        context '#pitching_ratings', :property => :pitching_ratings do
          its(:vs_left) { is_expected.to be_ootp5_pitching_ratings 1, 4, 5, 5, 3 }
          its(:vs_right) { is_expected.to be_ootp5_pitching_ratings 2, 4, 5, 5, 4 }

          [ 'vs_left', 'vs_right' ].each do |vs|
            its("#{vs}.endurance") { is_expected.to eq(10) }
            its("#{vs}.ground_ball_pct.get") { is_expected.to eq(68) }
            its("#{vs}.runs.get") { is_expected.to eq(-5) }
          end
        end

        context '#pitching_potential_ratings' do
          let(:scale) { OneToOneHundred.new }
          subject { player.pitching_potential_ratings }

          #its(:vs_left) { is_expected.to be_ootp5_pitching_ratings 63, 50, 70, 70, 43 }
          #its(:vs_right) { is_expected.to be_ootp5_pitching_ratings 73, 50, 70, 70, 53 }

          [ 'vs_left', 'vs_right' ].each do |vs|
            its("#{vs}.endurance") { is_expected.to eq(10) }
            its("#{vs}.ground_ball_pct.get") { is_expected.to eq(68) }
            its("#{vs}.runs") { is_expected.to_not be_present }
          end
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
            its(:position_scores) { is_expected.to eq('-2-----1') }
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
            its(:position_scores) { is_expected.to eq('-3366---') }
          end
        end

        context 'Alex Gutierrez' do
          let(:file) { 'alex_gutierrez' }

          it_behaves_like :pitcher, 'Alex Gutierrez', 31

          context '#pitching_ratings', :property => :pitching_ratings do
            its(:vs_left) { is_expected.to be_ootp6_pitching_ratings 100, 68, 92 }
            its(:vs_right) { is_expected.to be_ootp6_pitching_ratings 100, 70, 92 }

            its("vs_right.endurance") { is_expected.to eq(4) }
            its("vs_right.ground_ball_pct.get") { is_expected.to eq(63) }
          end
        end
      end
    end
  end
end

