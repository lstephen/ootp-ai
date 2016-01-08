
require 'spec_helper'

require 'player/examples'
require 'player/ratings/contexts'
require 'player/ratings/matchers'

java_import com.github.lstephen.ootp.ai.player.Player
java_import com.github.lstephen.ootp.ai.rating.AToE
java_import com.github.lstephen.ootp.ai.rating.OneToFive
java_import com.github.lstephen.ootp.ai.rating.OneToTen
java_import com.github.lstephen.ootp.ai.rating.OneToTwenty
java_import com.github.lstephen.ootp.ai.rating.Rating;
java_import com.github.lstephen.ootp.ai.site.Site;
java_import com.github.lstephen.ootp.ai.site.SiteHolder;
java_import com.github.lstephen.scratch.util.Jackson

RSpec.describe Player do
  let(:site) { double('Site', getBuntScale: bunt_scale, getRunningScale: running_scale) }

  before(:each) { SiteHolder.set(site) }

  context 'deserialize from Json' do
    subject(:player) { deserialize json }

    context 'Victor Plata' do
      let(:json) { VICTOR_PLATA_JSON }
      let(:bunt_scale) { OneToFive.new }
      let(:running_scale) { OneToTen.new }

      it_behaves_like :batter, 'Victor Plata', 34

      its(:bunt_for_hit_rating) { is_expected.to eq(Rating.new 2, OneToFive.new) }
      its(:stealing_rating) { is_expected.to eq(Rating.new 2, OneToTen.new) }

      context '#batting_ratings', :property => :batting_ratings do
        let(:ability_scale) { OneToTwenty.new }

        its(:vs_left) { is_expected.to be_batting_ratings 9, 14, 20, 20, 7 }
        its(:vs_right) { is_expected.to be_batting_ratings 9, 14, 20, 20, 7 }
      end

      context '#defensive_ratings', :property => :defensive_ratings do
        its(:position_scores) { is_expected.to eq('-2-----1') }
      end

      it { expect { deserialize(serialize player) }.not_to raise_error }
    end

    context 'Elijah Chausse' do
      let(:json) { ELIJAH_CHAUSEE_JSON }
      let(:bunt_scale) { AToE.new }
      let(:running_scale) { AToE.new }

      it_behaves_like :batter, 'Elijah Chausse', 27

      its(:bunt_for_hit_rating) { is_expected.to eq(Rating.new 'E', AToE.new) }
      its(:stealing_rating) { is_expected.to eq(Rating.new 'D', AToE.new) }

      context '#batting_ratings', :property => :batting_ratings do
        let(:ability_scale) { ZeroToTen.new }

        its(:vs_left) { is_expected.to be_batting_ratings 5, 4, 8, 8, 3 }
        its(:vs_right) { is_expected.to be_batting_ratings 6, 5, 9, 9, 4 }
      end

      context '#defensive_ratings', :property => :defensive_ratings do
        its(:position_scores) { is_expected.to eq('-2---325') }
      end

      it { expect { deserialize(serialize player) }.not_to raise_error }
    end
  end
end

def deserialize(json)
  Jackson.getMapper(nil).readValue json, Player.java_class
end

def serialize(player)
  Jackson.getMapper(nil).writeValueAsString player
end

VICTOR_PLATA_JSON = <<-JSON
{
  "id" : "p241",
  "name" : "Victor Plata",
  "ratings" : {
    "batting" : {
      "vsLeft" : {
        "@type" : "BattingRatings",
        "scale" : {
          "@type" : "OneToTwenty"
        },
        "contact" : 9,
        "gap" : 14,
        "power" : 20,
        "eye" : 20,
        "k" : 7
      },
      "vsRight" : {
        "@type" : "BattingRatings",
        "scale" : {
          "@type" : "OneToTwenty"
        },
        "contact" : 9,
        "gap" : 14,
        "power" : 20,
        "eye" : 20,
        "k" : 7
      }
    },
    "defensive" : {
      "positionRating" : {
        "FIRST_BASE" : 2.0
      },
      "catcher" : {
        "range" : {
          "present" : false
        },
        "errors" : {
          "present" : false
        },
        "arm" : {
          "reference" : 10,
          "present" : true
        },
        "dp" : {
          "present" : false
        },
        "ability" : {
          "reference" : 0,
          "present" : true
        }
      },
      "infield" : {
        "range" : {
          "reference" : 4,
          "present" : true
        },
        "errors" : {
          "reference" : 57,
          "present" : true
        },
        "arm" : {
          "reference" : 20,
          "present" : true
        },
        "dp" : {
          "reference" : 20,
          "present" : true
        },
        "ability" : {
          "present" : false
        }
      },
      "outfield" : {
        "range" : {
          "reference" : 6,
          "present" : true
        },
        "errors" : {
          "reference" : 57,
          "present" : true
        },
        "arm" : {
          "reference" : 20,
          "present" : true
        },
        "dp" : {
          "present" : false
        },
        "ability" : {
          "present" : false
        }
      }
    },
    "pitching" : null,
    "battingPotential" : {
      "scale" : {
        "@type" : "TwoToEight"
      },
      "contact" : 5,
      "gap" : 6,
      "power" : 8,
      "eye" : 8,
      "k" : 4
    },
    "pitchingPotential" : null,
    "buntForHit" : 2,
    "stealing" : 2
  },
  "age" : 34,
  "team" : "Port Adelaide Magpies",
  "salary" : "$7.2Mx3",
  "battingHand" : "SWITCH",
  "injured" : false,
  "upcomingFreeAgent" : false
}
JSON

ELIJAH_CHAUSEE_JSON = <<-JSON
{
  "id" : "p764",
  "name" : "Elijah Chausse",
  "ratings" : {
    "batting" : {
      "vsLeft" : {
        "@type" : "BattingRatings",
        "scale" : {
          "@type" : "ZeroToTen"
        },
        "contact" : 5,
        "gap" : 4,
        "power" : 8,
        "eye" : 8,
        "k" : 3
      },
      "vsRight" : {
        "@type" : "BattingRatings",
        "scale" : {
          "@type" : "ZeroToTen"
        },
        "contact" : 6,
        "gap" : 5,
        "power" : 9,
        "eye" : 9,
        "k" : 4
      }
    },
    "defensive" : {
      "positionRating" : {
        "RIGHT_FIELD" : 3.0
      },
      "catcher" : {
        "range" : {
          "present" : false
        },
        "errors" : {
          "present" : false
        },
        "arm" : {
          "reference" : 0,
          "present" : true
        },
        "dp" : {
          "present" : false
        },
        "ability" : {
          "reference" : 0,
          "present" : true
        }
      },
      "infield" : {
        "range" : {
          "reference" : 0,
          "present" : true
        },
        "errors" : {
          "reference" : 0,
          "present" : true
        },
        "arm" : {
          "reference" : 50,
          "present" : true
        },
        "dp" : {
          "reference" : 50,
          "present" : true
        },
        "ability" : {
          "present" : false
        }
      },
      "outfield" : {
        "range" : {
          "reference" : 21,
          "present" : true
        },
        "errors" : {
          "reference" : 87,
          "present" : true
        },
        "arm" : {
          "reference" : 90,
          "present" : true
        },
        "dp" : {
          "present" : false
        },
        "ability" : {
          "present" : false
        }
      }
    },
    "pitching" : null,
    "battingPotential" : {
      "scale" : {
        "@type" : "PotentialRating$RatingScale"
      },
      "contact" : "Good",
      "gap" : "Average",
      "power" : "Brilliant",
      "eye" : "Brilliant",
      "k" : "Average"
    },
    "pitchingPotential" : null,
    "buntForHit" : "E",
    "stealing" : "D"
  },
  "age" : 27,
  "team" : "Wichita Linemen",
  "salary" : "$4.43M  ",
  "battingHand" : "LEFT",
  "injured" : false,
  "upcomingFreeAgent" : true
}
JSON

