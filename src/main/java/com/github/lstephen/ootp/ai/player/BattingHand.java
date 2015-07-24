/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lstephen.ootp.ai.player;

/**
 *
 * @author lstephen
 */
public enum BattingHand {
    RIGHT("R"), LEFT("L"), SWITCH("S");

    private final String code;

    BattingHand(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static BattingHand fromCode(String s) {
        for (BattingHand b : BattingHand.values()) {
            if (s.equals(b.code)) {
                return b;
            }
        }
        throw new IllegalStateException("Unknown BattingHand: " + s);
    }
}
