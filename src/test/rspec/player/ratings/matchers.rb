
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

RSpec::Matchers.define :be_ootp5_pitching_ratings do |hits, doubles, hrs, bbs, ks|
  def normalize(value)
    scale.normalize(value).get
  end

  def ratings_to_string(r)
    "#{r.hits}/#{r.gap}/#{r.movement}/#{r.control}/#{r.stuff}"
  end

  match do |ratings|
    ratings != nil &&
      ratings.hits == normalize(hits) &&
      ratings.gap == normalize(doubles) &&
      ratings.movement == normalize(hrs) &&
      ratings.control == normalize(bbs) &&
      ratings.stuff == normalize(ks)
  end

  description do
    "be ratings of #{hits}/#{doubles}/#{hrs}/#{bbs}/#{ks}"
  end

  failure_message do |actual|
    "expected that #{ratings_to_string(actual)} would #{description}"
  end
end

