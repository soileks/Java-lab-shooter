package com.example.shooterlab2;

public class PlayerData {
    public String name;
    public int score = 0;
    public int shots = 0;
    public boolean isReady = false;

    public final Point arrowEndPos;
    public boolean isArrowFlying = false;

    public PlayerData(String name, int arrowXStart, int arrowYStart) {
        this.name = name;
        arrowEndPos = new Point(arrowXStart, arrowYStart);
    }

    public void setArrowEndPos(double newX, double newY) {
        this.arrowEndPos.move(newX, newY);
    }
}
