/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ljs.ootp.ai.selection.depthchart;

/**
 *
 * @author lstephen
 */
public enum BackupPlayingTime {
    EVERY_2ND_DAY("2", 100/2),
    EVERY_3RD_DAY("3", 100/3),
    EVERY_5TH_DAY("5", 100/5),
    EVERY_10TH_DAY("10", 100/10),
    EVERY_20TH_DAY("20", 100/20),
    WHEN_STARTER_TIRED("T", 1);

    private final String days;
    private final Integer pct;

    private BackupPlayingTime(String days, Integer pct) {
        this.pct = pct;
        this.days = days;
    }

    public String format() {
        return String.format("%2s/%2d%%", days, pct);
    }

    public static BackupPlayingTime roundFrom(Integer pct) {
        BackupPlayingTime previous = null;

        for (BackupPlayingTime current : BackupPlayingTime.values()) {
            if (previous == null) {
                previous = current;
                continue;
            }

            if (pct > (previous.pct + current.pct) / 2) {
                return previous;
            };
            previous = current;
        }

        return WHEN_STARTER_TIRED;
    }

}
