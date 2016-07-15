
RSpec.shared_examples :player do |name, age|
  it { is_expected.to_not be_nil }
  its(:name) { is_expected.to eq(name) }
  its(:age) { is_expected.to eq(age) }
end

RSpec.shared_examples :batter do |name, age|
  it_behaves_like :player, name, age
  its(:pitching_ratings) { is_expected.to be_nil }
end

RSpec.shared_examples :pitcher do |name, age|
  it_behaves_like :player, name, age
  its(:pitching_ratings) { is_expected.to_not be_nil }
end

