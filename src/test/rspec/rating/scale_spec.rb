require 'spec_helper'
require 'java'

java_import com.github.lstephen.ootp.ai.rating.OneToOneHundred
java_import com.github.lstephen.ootp.ai.rating.OneToTen
java_import com.github.lstephen.ootp.ai.rating.OneToTwenty
java_import com.github.lstephen.ootp.ai.rating.PotentialRating
java_import com.github.lstephen.ootp.ai.rating.TwoToEight
java_import com.github.lstephen.ootp.ai.rating.ZeroToTen

{
  OneToOneHundred => 1..100,
  OneToTwenty     => 1..20,
  TwoToEight      => 2..8,
  OneToTen        => 1..10,
  ZeroToTen       => 0..10,
  PotentialRating => PotentialRating.values
}.each do |scale, range|
  RSpec.describe scale do
    let(:range) { range }

    subject { scale.scale }

    describe '#parse' do
      it 'parses from a String' do 
        range.each { |r| expect(subject.parse(r.to_s).get).to eq(r) }
      end
    end

    describe '#normalize' do
      it 'normalizes to between 1 and 100' do
        range.each { |r| expect(subject.normalize(r).get).to be_between(1, 100) }
      end

      it 'is centered on the 1..100 range' do
        expect(subject.normalize(range.min).get - 1).to be_within(1).of(100 - subject.normalize(range.max).get)
      end
    end
  end
end

