package com.ljs.scratch.ootp.draft;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.ljs.scratch.ootp.html.Site;
import com.ljs.scratch.ootp.player.Player;
import com.ljs.scratch.ootp.value.TradeValue;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public final class DraftReport {

    private final Site site;

    private final TradeValue value;

    private DraftClass current;

    private DraftReport(Site site, TradeValue value) {
        this.site = site;
        this.value = value;
    }

    private void loadDraftClasses() {
        current = DraftClass.load(
            getDraftClassFile(site.getDate().getYear()), site.getDefinition());

        for (Player p : site.getDraft()) {
            current.addIfNotPresent(p);
        }

        current.save(getDraftClassFile(site.getDate().getYear()));

        // TODO: Historical
    }

    private File getDraftClassFile(int year) {
        return new File("c:/ootp/history/" + site.getName() + year + ".draft.json");
    }

    private RoundValue getRoundValue(int round) {
        Integer not = Iterables.size(site.getTeamIds());

        return getValueOfPicks((round - 1) * not, not);
    }

    private RoundValue getValueOfPicks(int n) {
        return getValueOfPicks(0, n);
    }

    private RoundValue getValueOfPicks(int start, int n) {
        RoundValue rv = RoundValue.create(value);

        Iterable<Player> players = byOverall().sortedCopy(current.getPlayers());

        players = Iterables.skip(players, start);
        players = Iterables.limit(players, n);

        if (Iterables.isEmpty(players)) {
            return null;
        }

        rv.add(players);

        return rv;
    }

    private Ordering<Player> byOverall() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(value.getOverall());
    }

    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {
        w.println();

        Integer not = Iterables.size(site.getTeamIds());

        int idx = 0;
        int n = not / 3;

        RoundValue rv = getValueOfPicks(n);
        if (rv == null) {
            return;
        }

        rv.print(w, "1E");

        idx += n;

        rv = getValueOfPicks(idx, n);
        rv.print(w, "1M");

        idx += n;
        n = not - idx;

        rv = getValueOfPicks(idx, n);
        rv.print(w, "1L");

        idx = not;
        n = not / 2;

        rv = getValueOfPicks(idx, n);
        rv.print(w, "2E");

        idx += n;
        n = not * 2 - idx;

        rv = getValueOfPicks(idx, n);
        rv.print(w, "2L");

        w.println();

        int round = 1;
        rv = getRoundValue(round);

        while (rv != null) {
            rv.print(w, String.format("%2d", round));

            round++;
            rv = getRoundValue(round);
        }

        w.flush();
    }

    public static DraftReport create(Site site, TradeValue value) {
        DraftReport report = new DraftReport(site, value);
        report.loadDraftClasses();
        return report;
    }



}
