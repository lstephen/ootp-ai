package com.github.lstephen.ootp.ai.selection.depthchart;

import com.github.lstephen.ootp.ai.io.Printable;
import com.github.lstephen.ootp.ai.selection.lineup.All;
import com.google.common.base.Preconditions;
import java.io.PrintWriter;

/** @author lstephen */
public final class AllDepthCharts implements Printable {

  private final All<DepthChart> all;

  private AllDepthCharts(All<DepthChart> all) {
    Preconditions.checkNotNull(all);

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
