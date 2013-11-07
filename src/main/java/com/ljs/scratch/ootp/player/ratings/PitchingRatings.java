package com.ljs.scratch.ootp.player.ratings;

import com.google.common.base.Objects;

/**
 *
 * @author lstephen
 */
public class PitchingRatings {


    private int hits;

    private int gap;

    private int stuff;

    private int control;

    private int movement;

    private int endurance;

    public int getHits() { return hits; }
    public void setHits(int hits) { this.hits = hits; }

    public int getGap() { return gap; }
    public void setGap(int gap) { this.gap = gap; }

    public int getStuff() { return stuff; }
    public void setStuff(int stuff) { this.stuff = stuff; }

    public int getControl() { return control; }
    public void setControl(int control) { this.control = control; }

    public int getMovement() { return movement; }
    public void setMovement(int movement) { this.movement = movement; }

    public int getEndurance() { return endurance; }
    public void setEndurance(int endurance) { this.endurance = endurance; }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("stuff", stuff)
            .add("control", control)
            .add("movement", movement)
            .add("hits", hits)
            .add("gap", gap)
            .toString();
    }

}
