package com.example.ludotime;

import java.util.ArrayList;
/**
 * @brief FireBaseListener is used to deal with the database delay
 */
public interface FireBaseListener {
    void onCallbackUser(User u);
    void onCallbackUsers(ArrayList<User> users);
}
