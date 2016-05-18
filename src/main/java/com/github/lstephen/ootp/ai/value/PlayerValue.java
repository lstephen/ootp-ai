package com.github.lstephen.ootp.ai.value;

import com.google.common.base.Function;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.Slot;
import com.github.lstephen.ootp.ai.player.ratings.BattingRatings;
import com.github.lstephen.ootp.ai.player.ratings.PitchingRatings;
import com.github.lstephen.ootp.ai.regression.BattingRegression;
import com.github.lstephen.ootp.ai.regression.PitchingRegression;
import com.github.lstephen.ootp.ai.regression.Predictions;
import com.github.lstephen.ootp.ai.selection.lineup.PlayerDefenseScore$;
import com.github.lstephen.ootp.ai.selection.Selections;
import com.github.lstephen.ootp.ai.splits.Splits;

/**
 *
 * @author lstephen
 */
public class PlayerValue {

    public static final Double MR_CONSTANT = .865;

    private final Predictions predictions;

    private final BattingRegression batting;

    private final PitchingRegression pitching;

    public PlayerValue(Predictions predictions, BattingRegression batting, PitchingRegression pitching) {
        this.predictions = predictions;
        this.batting = batting;
        this.pitching = pitching;
    }

    public Function<Player, Integer> getNowValue() {
        return new Function<Player, Integer>() {
            public Integer apply(Player p) {
                return getNowValue(p);
            }
        };
    }

    public Integer getNowValue(Player p) {
        Slot st = Slot.getPrimarySlot(p);

        Double factor = st == Slot.MR ? MR_CONSTANT : Double.valueOf(1.0);

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

    public Function<Player, Integer> getNowAbility() {
        return new Function<Player, Integer>() {
            @Override
            public Integer apply(Player p) {
                return getNowAbility(p);
            }
        };
    }

    public Integer getNowAbility(Player p) {
        if (Selections.isHitter(p)) {
          if (predictions.containsHitter(p)) {
            return predictions.getOverallHitting(p);
          } else {
            return batting.predict(p).getOverall().getWobaPlus();
          }
        }

        if (Selections.isPitcher(p)) {
          if (predictions.containsPitcher(p)) {
            return predictions.getOverallPitching(p);
          } else {
            return predictions.getPitcherOverall().getPlus(pitching.predict(p));
          }
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

    public Function<Player, Integer> getFutureAbility() {
        return new Function<Player, Integer>() {
            @Override
            public Integer apply(Player p) {
                return getFutureAbility(p);
            }
        };
    }

    public Integer getFutureAbility(Player p) {
        Slot st = Slot.getPrimarySlot(p);

        Integer value;

        switch (st) {
            case C:
            case SS:
            case CF:
            case IF:
            case OF:
            case H:
                Splits<BattingRatings<Integer>> splitsB = p.getBattingPotentialRatings();

                value = batting.predict(splitsB).getOverall().getWobaPlus();
                break;
            case SP:
            case MR:
                Splits<PitchingRatings<Integer>> splitsP = p.getPitchingPotentialRatings();

                value = predictions.getPitcherOverall().getPlus(pitching.predict(splitsP).getOverall());
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
