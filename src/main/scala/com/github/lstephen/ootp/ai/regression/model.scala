package com.github.lstephen.ootp.ai.regression

import com.github.lstephen.ootp.ai.io.Printable

import java.io.PrintWriter

object Model {
  trait Predict {
    def apply(in: Seq[Input]): Seq[Double]
    def report(label: String): Printable = new Printable {
      def print(w: PrintWriter) = {}
    }
  }
}

trait Model {
  import Model._

  def train(ds: DataSet): Predict
}
