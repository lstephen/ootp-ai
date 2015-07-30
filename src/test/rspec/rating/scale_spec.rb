require 'spec_helper'
require 'java'

java_import com.github.lstephen.ootp.ai.rating.OneToFive
java_import com.github.lstephen.ootp.ai.rating.OneToOneHundred
java_import com.github.lstephen.ootp.ai.rating.OneToTen
java_import com.github.lstephen.ootp.ai.rating.OneToTwenty
java_import com.github.lstephen.ootp.ai.rating.Potential
java_import com.github.lstephen.ootp.ai.rating.TwoToEight
java_import com.github.lstephen.ootp.ai.rating.ZeroToTen

{
  OneToFive       => 1..5,
  OneToOneHundred => 1..100,
  OneToTwenty     => 1..20,
  TwoToEight      => 2..8,
  OneToTen        => 1..10,
  ZeroToTen       => 0..10,
  Potential       => ['Poor', 'Fair', 'Average', 'Good', 'Brilliant']
}.each do |scale, range|
  RSpec.describe scale do
    let(:range) { range }

    subject { scale.respond_to?(:scale) ? scale.scale : scale.new }

    describe '#parse' do
      it 'parses from a String' do
        range.each { |r| expect(subject.parse(r.to_s).get).to eq(r) }
      end
    end

    describe '#normalize' do
      let(:expected_step_size) { 100 / range.count }

      it 'normalizes to between 1 and 100' do
        range.each { |r| expect(subject.normalize(r).get).to be_between(1, 100) }
      end

      it 'is centered on the 1..100 range' do
        expect(subject.normalize(range.first).get - 1).to be_within(1).of(100 - subject.normalize(range.last).get)
      end

      it 'is linear' do
        range.each_cons(2) do |a, b|
          expect(subject.normalize(b).get - subject.normalize(a).get)
            .to be_within(1).of(expected_step_size)
        end
      end

      it 'is spread through the entire 1..100 range' do
        expect(subject.normalize(range.first).get - 1).to be <= expected_step_size
        expect(100 - subject.normalize(range.last).get).to be <= expected_step_size
      end
    end
  end
end

