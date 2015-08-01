
require 'spec_helper'

require 'player/examples'
require 'player/ratings/contexts'
require 'player/ratings/matchers'

java_import com.github.lstephen.ootp.ai.player.Player
java_import com.github.lstephen.ootp.ai.rating.OneToTen
java_import com.github.lstephen.ootp.ai.rating.OneToTwenty
java_import com.github.lstephen.scratch.util.Jackson

RSpec.describe Player do
  context 'deserialize from Json' do
    subject(:player) { deserialize json }

    context 'Victor Plata' do
      let(:json) { VICTOR_PLATA_JSON }

      it_behaves_like :batter, 'Victor Plata', 34

      context '#batting_ratings', :property => :batting_ratings do
        let(:ability_scale) { OneToTwenty.new }

        its(:vs_left) { is_expected.to be_batting_ratings 10, 14, 20, 20, 7 }
        its(:vs_right) { is_expected.to be_batting_ratings 9, 14, 20, 20, 7 }
      end

      context '#defensive_ratings', :property => :defensive_ratings do
        its(:position_scores) { is_expected.to eq('-2------') }
      end

      it { expect { deserialize(serialize player) }.not_to raise_error }
    end

    context 'Elijah Chausse' do
      let(:json) { ELIJAH_CHAUSEE_JSON }

      it_behaves_like :batter, 'Elijah Chausse', 27

      context '#batting_ratings', :property => :batting_ratings do
        let(:ability_scale) { ZeroToTen.new }

        its(:vs_left) { is_expected.to be_batting_ratings 5, 4, 8, 8, 3 }
        its(:vs_right) { is_expected.to be_batting_ratings 6, 5, 9, 9, 4 }
      end

      context '#defensive_ratings', :property => :defensive_ratings do
        its(:position_scores) { is_expected.to eq('-2---3-5') }
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
      "contact" : "GOOD",
      "gap" : "AVERAGE",
      "power" : "BRILLIANT",
      "eye" : "BRILLIANT",
      "k" : "AVERAGE"
    },
    "pitchingPotential" : null
  },
  "age" : 27,
  "team" : "Wichita Linemen",
  "salary" : "$4.43M  ",
  "battingHand" : "LEFT",
  "injured" : false,
  "upcomingFreeAgent" : true
}
JSON

