package com.github.lstephen.ootp.ai.report;

import com.github.lstephen.ootp.ai.io.Printable;
import java.io.PrintWriter;

public class StolenBaseReport implements Printable {

  // http://www.fangraphs.com/blogs/the-changing-caught-stealing-calculus-2/
  public Double getBreakEvenPoint() {
    // calculate expected home runs per pa of our lineups
    // 0.590 + 3.33 * (hr/pa);
    return 0.0;
  }

  public void print(PrintWriter w) {
    // Print break even point
    // for each hitter in the ML squad
    //   If expect sb > break even
    //     print as green light
  }
}
