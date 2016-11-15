package com.github.lstephen.ootp.ai.value;

import com.google.common.base.Function;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.player.ratings.BattingRatings;
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings;
import com.github.lstephen.ootp.ai.player.ratings.Position;
import com.github.lstephen.ootp.ai.regression.Predictor;
import com.github.lstephen.ootp.ai.selection.lineup.PlayerDefenseScore;
import com.github.lstephen.ootp.ai.selection.lineup.PlayerDefenseScore$;
import com.github.lstephen.ootp.ai.selection.Selections;
import com.github.lstephen.ootp.ai.splits.Splits;

/**
 *
 * @author lstephen
 */
public class PlayerValue {

    public static final Double MR_CONSTANT = .865;

    private final Predictor predictor;

    public PlayerValue(Predictor predictor) {
        this.predictor = predictor;
    }

    public Function<Player, Integer> getNowValue() {
        return new Function<Player, Integer>() {
            public Integer apply(Player p) {
                return getNowValue(p);
            }
        };
    }

    public Integer getNowValue(Player p) {
        Double defense = Selections.isHitter(p)
          ? PlayerDefenseScore$.MODULE$.atBestPosition(p, true).score()
          : 0.0;

        Double endurance = 1.0;

        if (Selections.isPitcher(p)) {
            Integer end = p.getPitchingRatings().getVsRight().getEndurance();
            endurance = (1000.0 - Math.pow(10 - end, 3)) / 1000.0;
        }

        if (endurance < MR_CONSTANT) {
          endurance = MR_CONSTANT;
        }

        return (int) Math.round(endurance * getNowAbility(p) + defense);
    }

    public Integer getNowValue(Player ply, Position pos) {
        if (Selections.isHitter(ply) && pos.isPitching()) {
          return 0;
        }

        if (Selections.isPitcher(ply) && pos.isHitting()) {
          return 0;
        }


        Double defense = Selections.isHitter(ply)
          ? new PlayerDefenseScore(ply, pos).score()
          : 0.0;

        Double endurance = 1.0;


        if (Selections.isPitcher(ply)) {
          switch (pos) {
            case STARTING_PITCHER:
              Integer end = ply.getPitchingRatings().getVsRight().getEndurance();
              endurance = (1000.0 - Math.pow(10 - end, 3)) / 1000.0;
              break;
            case MIDDLE_RELIEVER:
              endurance = MR_CONSTANT;
              break;
            default:
              // do nothing
          }
        }

        return (int) Math.round(endurance * getNowAbility(ply) + defense);
    }

    public Function<Player, Double> getNowAbility() {
        return new Function<Player, Double>() {
            @Override
            public Double apply(Player p) {
                return getNowAbility(p);
            }
        };
    }

    public double getNowAbility(Player p) {
        if (Selections.isHitter(p)) {
          return predictor.predictBatting(p).overall();
        }

        if (Selections.isPitcher(p)) {
          return predictor.predictPitching(p).overall();
        }

        throw new IllegalStateException("Player is neither a hitter or pitcher: " + p.getName());
    }

    public Function<Player, Integer> getFutureValue() {
        return new Function<Player, Integer>() {
            public Integer apply(Player p) {
                return getFutureValue(p);
            }
        };
    }

    public Integer getFutureValue(Player p) {
        Integer now = getNowValue(p);
        Integer ceiling = getCeilingValue(p);

        Double risk = ceiling > now
            ? Math.pow((double) (ceiling - now) / ceiling, 2)
            : 0.0;


        return (int) Math.round(risk * now + (1.0 - risk) * ceiling);
    }

    public Integer getCeilingValue(Player p) {
        Double defense = Selections.isHitter(p)
          ? PlayerDefenseScore$.MODULE$.atBestPosition(p, true).score()
          : 0.0;

        Double endurance = 1.0;

        if (Selections.isPitcher(p)) {
            Integer end = p.getPitchingRatings().getVsRight().getEndurance();
            endurance = (1000.0 - Math.pow(10 - end, 3)) / 1000.0;
        }

        if (endurance < MR_CONSTANT) {
          endurance = MR_CONSTANT;
        }

        return (int) Math.round(endurance * getFutureAbility(p) + defense);
    }

    public Function<Player, Double> getFutureAbility() {
        return new Function<Player, Double>() {
            @Override
            public Double apply(Player p) {
                return getFutureAbility(p);
            }
        };
    }

    public double getFutureAbility(Player p) {
        Slot st = Slot.getPrimarySlot(p);

        double value;

        switch (st) {
            case C:
            case SS:
            case CF:
            case IF:
            case OF:
            case H:
                value = predictor.predictFutureBatting(p).overall();
                break;
            case SP:
            case MR:
                value = predictor.predictFuturePitching(p).overall();
                break;
            default:
                throw new IllegalStateException();
        }

        return Math.max(0, value - getAgingFactor(p));
    }

    public static int getAgingFactor(Player p) {
        int age = p.getAge();
        if (age > 33) {
            return (int) Math.pow(age - 33, 2);
        } else {
            return 0;
        }
    }


}
