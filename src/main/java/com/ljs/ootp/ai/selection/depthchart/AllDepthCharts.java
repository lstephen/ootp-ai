package com.ljs.ootp.ai.selection.depthchart;

import com.ljs.ootp.ai.io.Printable;
import com.ljs.ootp.ai.selection.lineup.All;
import java.io.PrintWriter;
import org.fest.assertions.api.Assertions;

/**
 *
 * @author lstephen
 */
public final class AllDepthCharts implements Printable {

    private final All<DepthChart> all;

    private AllDepthCharts(All<DepthChart> all) {
        Assertions.assertThat(all).isNotNull();

        this.all = all;
    }

    public static AllDepthCharts create(All<DepthChart> all) {
        return new AllDepthCharts(all);
    }

    public void print(PrintWriter w) {
        w.println();
        w.println("vs RHP");
        all.getVsRhp().print(w);

        w.println();
        w.println("vs RHP+DH");
        all.getVsRhpPlusDh().print(w);

        w.println();
        w.println("vs LHP");
        all.getVsLhp().print(w);

        w.println();
        w.println("vs LHP+DH");
        all.getVsLhpPlusDh().print(w);

        w.flush();
    }
}
