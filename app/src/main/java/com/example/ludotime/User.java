package com.example.ludotime;

public class User {

    private String name;
    private int score;
    private String icon;

    public User(String name, int score, String icon) {
        this.name = name;
        this.score = score;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
