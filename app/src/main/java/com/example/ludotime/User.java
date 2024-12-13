package com.example.ludotime;

public class User {

    private String name;
    private int score;
    private String icon;
    private String password;
    private String email;

    public User(String email, String password, String name, int score, String icon) {
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
