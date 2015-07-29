
require 'spec_helper'

require 'player/ratings/contexts'
require 'player/ratings/matchers'

java_import com.github.lstephen.ootp.ai.player.Player
java_import com.github.lstephen.ootp.ai.rating.OneToTwenty
java_import com.github.lstephen.scratch.util.Jackson

RSpec.describe Player do
  context 'deserialize' do
    let(:json) { VICTOR_PLATA_JSON }

    subject(:player) { Jackson.getMapper(nil).readValue(json, Player.java_class) }

    it { is_expected.to_not be_nil }
    its(:name) { is_expected.to eq('Victor Plata') }
    its(:age) { is_expected.to eq(34) }
    its(:pitching_ratings) { is_expected.to be_nil }

    context '#batting_ratings', :property => :batting_ratings do
      let(:ability_scale) { OneToTwenty.new }

      its(:vs_left) { is_expected.to be_batting_ratings 10, 14, 20, 20, 7 }
      its(:vs_right) { is_expected.to be_batting_ratings 9, 14, 20, 20, 7 }
    end

    context '#defensive_ratings', :property => :defensive_ratings do
      its(:position_scores) { is_expected.to eq('-2------') }
    end
  end
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
        "contact" : 10,
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
    "pitchingPotential" : null,
    "battingPotential" : {
      "scale" : {
        "@type" : "TwoToEight"
      },
      "contact" : 5,
      "gap" : 7,
      "power" : 8,
      "eye" : 8,
      "k" : 4
    }
  },
  "age" : 34,
  "team" : "Port Adelaide Magpies",
  "salary" : "$7.2Mx3",
  "battingHand" : "SWITCH",
  "upcomingFreeAgent" : false,
  "injured" : false
}
JSON
