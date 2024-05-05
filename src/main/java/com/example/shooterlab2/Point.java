package com.example.shooterlab2;
public class Point {
    public double x = 0.0, y = 0.0;

    public Point(double x, double y) {
        move(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void move(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    @Override
    public String toString() {
        return "MyPoint {" + "x = " + x + ", y = " + y + '}';
    }
}
