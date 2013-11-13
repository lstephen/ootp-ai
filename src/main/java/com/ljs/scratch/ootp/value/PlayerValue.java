package com.ljs.scratch.ootp.value;

import com.google.common.base.Function;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.player.ratings.BattingRatings;
import com.ljs.scratch.ootp.player.ratings.PitchingRatings;
import com.ljs.scratch.ootp.player.ratings.Splits;
import com.ljs.scratch.ootp.regression.BattingRegression;
import com.ljs.scratch.ootp.regression.PitchingRegression;
import com.ljs.scratch.ootp.regression.Predictions;
import com.ljs.scratch.ootp.selection.Slot;
import com.ljs.scratch.ootp.stats.BattingStats;
import com.ljs.scratch.ootp.stats.PitchingStats;
import com.ljs.scratch.ootp.stats.SplitStats;

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

        return st == Slot.MR
            ? (int) (MR_CONSTANT * getNowAbility(p))
            : getNowAbility(p);
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
        Slot st = Slot.getPrimarySlot(p);

        switch (st) {
            case C:
            case SS:
            case CF:
            case IF:
            case OF:
            case H:
                if (predictions.containsPlayer(p)) {
                    return predictions.getOverallHitting(p);
                } else {
                    return batting.predict(p).getOverall().getWobaPlus();
                }
            case SP:
            case MR:
                Integer value;
                if (predictions.containsPlayer(p)) {
                    value = predictions.getOverallPitching(p);
                } else {
                    value = predictions.getPitcherOverall().getPlus(pitching.predict(p));
                }
                return value;
            default:
                throw new IllegalStateException();
        }
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
        Slot st = Slot.getPrimarySlot(p);

        return st == Slot.MR
            ? (int) (MR_CONSTANT * getFutureAbility(p))
            : getFutureAbility(p);
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
                Splits<BattingRatings> splitsB = p.getBattingPotentialRatings();
                BattingStats vsLeftB = batting.predict(splitsB.getVsLeft());
                BattingStats vsRightB = batting.predict(splitsB.getVsRight());

                value = SplitStats.create(vsLeftB, vsRightB).getOverall().getWobaPlus();
                break;
            case SP:
            case MR:
                Splits<PitchingRatings> splitsP = p.getPitchingPotentialRatings();
                PitchingStats vsLeftP = pitching.predict(splitsP.getVsLeft());
                PitchingStats vsRightP = pitching.predict(splitsP.getVsRight());

                value = predictions.getPitcherOverall().getPlus(SplitStats.create(vsLeftP, vsRightP));
                break;
            default:
                throw new IllegalStateException();
        }

        return Math.max(0, value - getAgingFactor(p));
    }

    private int getAgingFactor(Player p) {
        int age = p.getAge();
        if (age > 33) {
            return (int) Math.pow(age - 33, 2);
        } else {
            return 0;
        }
    }


}
