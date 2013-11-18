package com.ljs.ootp.ai.ootp5.report;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.player.Slot;
import com.ljs.ootp.ai.player.ratings.BattingRatings;
import com.ljs.ootp.ai.selection.Selections;
import com.ljs.ootp.ai.site.Version;
import com.ljs.ootp.ai.splits.Splits;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public final class SpringTraining implements Printable {

    private static enum HitterSkills { CONTACT, POWER, EYE, DEFENSE }

    private static Integer HITTING_THRESHOLD = 15;

    private final Version version;

    private final Iterable<Player> players;

    private SpringTraining(Version version, Iterable<Player> players) {
        this.version = version;
        this.players = players;
    }

    @Override
    public void print(PrintWriter w) {
        w.println();
        w.println("Spring Training");
        w.format("%-15s C/P/Z/D%n", "Hitters -------");
        for (Player p : Player.byShortName().sortedCopy(Selections.onlyHitters(players))) {
            printHitterPlan(w, p);
        }

        w.println();
        w.format("%-15s S/V/C/S%n", "Pitchers ------");
        for (Player p : Player.byShortName().sortedCopy(Selections.onlyPitchers(players))) {
            printPitcherPlan(w, p);
        }
    }

    private void printHitterPlan(PrintWriter w, Player p) {
        Set<HitterSkills> skills = Sets.newHashSet(HitterSkills.values());

        Splits<BattingRatings<?>> now = p.getBattingRatings();
        Splits<BattingRatings<Integer>> pot = p.getBattingPotentialRatings();

        if (now.getVsLeft().getContact() < HITTING_THRESHOLD
            && now.getVsRight().getContact() < HITTING_THRESHOLD
            && pot.getVsLeft().getContact() < HITTING_THRESHOLD
            && pot.getVsRight().getContact() < HITTING_THRESHOLD) {
            skills.remove(HitterSkills.CONTACT);
        }

        if (now.getVsLeft().getPower() < HITTING_THRESHOLD
            && now.getVsRight().getPower() < HITTING_THRESHOLD
            && pot.getVsLeft().getPower() < HITTING_THRESHOLD
            && pot.getVsRight().getPower() < HITTING_THRESHOLD) {
            skills.remove(HitterSkills.POWER);
        }

        if (now.getVsLeft().getEye() < HITTING_THRESHOLD
            && now.getVsRight().getEye() < HITTING_THRESHOLD
            && pot.getVsLeft().getEye() < HITTING_THRESHOLD
            && pot.getVsRight().getEye() < HITTING_THRESHOLD) {
            skills.remove(HitterSkills.EYE);
        }

        if (p.getAge() > 27 && Slot.getPrimarySlot(p) == Slot.H) {
            skills.remove(HitterSkills.DEFENSE);
        }

        if (skills.size() == 4) {
            return;
        }

        Map<HitterSkills, Integer> values = Maps.newHashMap();

        Integer remaining = 20;

        for (HitterSkills skill : HitterSkills.values()) {
            if (skills.contains(skill)) {
                Integer points = remaining / skills.size();
                remaining -= points;
                values.put(skill, points);
                skills.remove(skill);
            } else {
                values.put(skill, 0);
            }
        }

        w.format(
            "%-15s %s%n",
            StringUtils.abbreviate(p.getShortName(), 15),
            Joiner
                .on('/')
                .join(ImmutableList.of(
                    values.get(HitterSkills.CONTACT),
                    values.get(HitterSkills.POWER),
                    values.get(HitterSkills.EYE),
                    values.get(HitterSkills.DEFENSE))));
    }

    private void printPitcherPlan(PrintWriter w, Player p) {
        Integer endurance = p.getPitchingRatings().getVsRight().getEndurance();

        if (endurance == 1
            || endurance == 5
            || endurance == 10
            || (version == Version.OOTP5 && endurance == 6)) {


            if (p.getAge() < 26) {
                w.format("%-15s 6/7/7/0%n", StringUtils.abbreviate(p.getShortName(), 15));
            } else {
                w.format("%-15s 7/6/7/0%n", StringUtils.abbreviate(p.getShortName(), 15));
            }
        }
    }

    public static SpringTraining create(Version version, Iterable<Player> players) {
        return new SpringTraining(version, players);
    }

}
