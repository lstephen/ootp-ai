
RSpec::Matchers.define :be_batting_ratings do |contact, gap, power, eye, k|
  def normalize(value)
    scale.normalize(value).get
  end

  def ratings_to_string(r)
    "#{r.contact}/#{r.gap}/#{r.power}/#{r.eye}/#{r.k}"
  end

  match do |ratings|
    ratings != nil &&
      ratings.contact == normalize(contact) &&
      ratings.gap == normalize(gap) &&
      ratings.power == normalize(power) &&
      ratings.eye == normalize(eye) &&
      ratings.k.present? && ratings.k.get == normalize(k)
  end

  description do
    "be ratings of #{contact}/#{gap}/#{power}/#{eye}/#{k}"
  end

  failure_message do |actual|
    "expected that #{ratings_to_string(actual)} would #{description}"
  end
end

RSpec::Matchers.define :be_ootp6_pitching_ratings do |stuff, control, movement|
  def normalize(value)
    scale.normalize(value).get
  end

  def ratings_to_string(r)
    "#{r.stuff}/#{r.control}/#{r.movement}"
  end

  match do |ratings|
    ratings != nil &&
      ratings.stuff == normalize(stuff) &&
      ratings.control == normalize(control) &&
      ratings.movement == normalize(movement)
  end

  description do
    "be ratings of #{stuff}/#{control}/#{movement}"
  end

  failure_message do |actual|
    "expected that #{ratings_to_string(actual)} would #{description}"
  end
end

