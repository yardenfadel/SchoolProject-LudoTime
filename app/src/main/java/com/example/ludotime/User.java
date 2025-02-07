package com.example.ludotime;

/**
 * @brief User describes a user
 */
public class User {

    private String name;
    private int score;
    private String icon;
    private String password;
    private String email;

    /**
     * @brief User() empty constructor
     */
    public User() {
    }

    /**
     * @brief User() constructor
     * @param email : user's email
     * @param password : user's password
     * @param name : user's username
     * @param score : user's score
     * @param icon : user's icon
     */
    public User(String email, String password, String name, int score, String icon) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.score = score;
        this.icon = icon;
    }

    /**
     * @brief getName() gives you user's name
     * @return : user's name
     */
    public String getName() {
        return name;
    }

    /**
     * @brief setName() sets user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @brief getScore() gives you user's score
     * @return : user's score
     */
    public int getScore() {
        return score;
    }

    /**
     * @brief setScore() sets user's score
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * @brief getIcon() gives you user's icon
     * @return : user's icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @brief setIcon() sets user's icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @brief getPassword() gives you user's password
     * @return : user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @brief setPassword() sets user's password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @brief getEmail() gives you user's email
     * @return : user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @brief setEmail() sets user's email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
