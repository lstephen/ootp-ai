package com.ljs.ootp.ai.draft;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ootp.ai.config.Config;
import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.io.Printables;
import com.ljs.ootp.ai.player.Player;
import com.ljs.ootp.ai.site.Site;
import com.ljs.ootp.ai.value.TradeValue;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class DraftReport implements Printable {

    private final Site site;

    private final TradeValue value;

    private DraftClass current;

    private Set<DraftClass> historical = Sets.newHashSet();

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

        current.save(site, getDraftClassFile(site.getDate().getYear()));

        for (int i = 1; i < 5; i++) {
            File dcFile = getDraftClassFile(site.getDate().getYear() - i);

            if (dcFile.exists()) {
                historical.add(DraftClass.load(dcFile, site.getDefinition()));
            }
        }

        // TODO: Historical
    }

    private File getDraftClassFile(int year) {
        try {
            String historyDirectory = Config.createDefault().getValue("history.dir").or("c:/ootp/history");
            return new File(historyDirectory + "/" + site.getName() + year + ".draft.json");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
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

        if (!Iterables.isEmpty(players)) {
            rv.add(players);
        }

        for (DraftClass dc : historical) {
            Iterable<Player> ps = byOverall().sortedCopy(dc.getPlayers());

            ps = Iterables.skip(ps, start);
            ps = Iterables.limit(ps, n);

            if (!Iterables.isEmpty(ps)) {
                rv.addHistorical(ps);
            }
        }

        return rv;
    }

    private Ordering<Player> byOverall() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(value.getOverall());
    }

    public void print(OutputStream out) {
        Printables.print(this).to(out);
    }

    @Override
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

        while (!rv.isEmpty()) {
            rv.print(w, String.format("%2d", round));

            round++;
            rv = getRoundValue(round);
        }
    }

    public static DraftReport create(Site site, TradeValue value) {
        DraftReport report = new DraftReport(site, value);
        report.loadDraftClasses();
        return report;
    }



}
