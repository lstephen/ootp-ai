package com.ljs.scratch.ootp.selection.lineup;

import java.io.OutputStream;
import java.io.PrintWriter;

public class AllLineups {

    private Lineup vsRhp;
    private Lineup vsRhpPlusDh;
    private Lineup vsLhp;
    private Lineup vsLhpPlusDh;

    public void setVsRhp(Lineup vsRhp) {
        this.vsRhp = vsRhp;
    }

    public void setVsRhpPlusDh(Lineup vsRhpPlusDh) {
        this.vsRhpPlusDh = vsRhpPlusDh;
    }

    public void setVsLhp(Lineup vsLhp) {
        this.vsLhp = vsLhp;
    }

    public void setVsLhpPlusDh(Lineup vsLhpPlusDh) {
        this.vsLhpPlusDh = vsLhpPlusDh;
    }

    public void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter w) {
        w.println();
        w.println(
            String.format(
                "   %-19s %-19s %-19s %-19s",
                "vsRHP",
                "vsRHP+DH",
                "vsLHP",
                "vsLHP+DH"
        ));

        for (int i = 0; i < 9; i++) {
            w.println(
                String.format(
                    "%d. %-19s %-19s %-19s %-19s",
                    Integer.valueOf(i + 1),
                    vsRhp.getEntry(i).format("%2s %-15s"),
                    vsRhpPlusDh.getEntry(i).format("%2s %-15s"),
                    vsLhp.getEntry(i).format("%2s %-15s"),
                    vsLhpPlusDh.getEntry(i).format("%2s %-15s")
                ));
        }

        w.flush();
    }

}
