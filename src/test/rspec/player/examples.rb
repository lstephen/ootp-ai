
RSpec.shared_examples :batter do |name, age|
  it { is_expected.to_not be_nil }
  its(:name) { is_expected.to eq(name) }
  its(:age) { is_expected.to eq(age) }
  its(:pitching_ratings) { is_expected.to be_nil }
end

