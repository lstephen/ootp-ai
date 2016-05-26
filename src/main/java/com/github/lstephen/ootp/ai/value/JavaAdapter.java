package com.github.lstephen.ootp.ai.value;

import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.regression.Predictor;

public final class JavaAdapter {

  private JavaAdapter() { }

  public static OverallValue overallValue(Player p, Predictor predictor) {
    return OverallValue$.MODULE$.apply(p, predictor);
  }

  public static NowValue nowValue(Player p, Predictor predictor) {
    return NowValue$.MODULE$.apply(p, predictor);
  }

  public static FutureValue futureValue(Player p, Predictor predictor) {
    return FutureValue$.MODULE$.apply(p, predictor);
  }

}
