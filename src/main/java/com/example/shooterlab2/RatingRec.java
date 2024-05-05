package com.example.shooterlab2;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class RatingRec {
    @Id
    public String name;
    public int winsCount;

    public RatingRec() {
    }

    public RatingRec(String name, int winsCount) {
        this.name = name;
        this.winsCount = winsCount;
    }

    public String getName() {
        return name;
    }

    public int getWinsCount() {
        return winsCount;
    }
}
