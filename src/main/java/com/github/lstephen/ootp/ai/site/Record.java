package com.github.lstephen.ootp.ai.site;

/**
 *
 * @author lstephen
 */
public final class Record {

    private final Integer wins;

    private final Integer losses;

    private Record(Integer wins, Integer losses) {
        this.wins = wins;
        this.losses = losses;
    }

    public Integer getWins() {
        return wins;
    }

    public Integer getLosses() {
        return losses;
    }

    public Double getWinPercentage() {
        return getGames() > 0 ? (double) wins / getGames() : .5;
    }

    public Integer getGames() {
        return wins + losses;
    }

    public static Record create(Number wins, Number losses) {
        return new Record(wins.intValue(), losses.intValue());
    }

}
