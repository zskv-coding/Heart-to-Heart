package com.zskv.heartToHeart;

public class PlayerData {

    private int hearts;
    private boolean alive;

    public PlayerData(int hearts, boolean alive) {
        this.hearts = hearts;
        this.alive = alive;
    }

    public int getHearts() {
        return hearts;
    }

    public void setHearts(int hearts) {
        this.hearts = hearts;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}