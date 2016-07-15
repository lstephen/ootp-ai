
RSpec.shared_context 'Batting Ratings', :property => :batting_ratings do
  let(:scale) { ability_scale }
  subject { player.batting_ratings }
end

RSpec.shared_context 'Pitching Ratings', :property => :pitching_ratings do
  let(:scale) { ability_scale }
  subject { player.pitching_ratings }
end

RSpec.shared_context 'Defensive Ratings', :property => :defensive_ratings do
  subject { player.defensive_ratings }
end

