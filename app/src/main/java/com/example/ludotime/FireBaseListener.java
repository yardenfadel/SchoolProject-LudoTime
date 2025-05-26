package com.example.ludotime;

import java.util.ArrayList;

/**
 * FireBaseListener interface is used to handle asynchronous database operations and callbacks.
 *
 * <p>This interface provides a mechanism to deal with Firebase database delays and asynchronous
 * operations by implementing callback methods that are invoked when database operations complete.
 * It follows the observer pattern to notify listeners about the completion of various Firebase
 * operations related to user management and authentication.</p>
 *
 */
public interface FireBaseListener {

    /**
     * Callback method invoked when a single user retrieval operation completes.
     *
     * <p>This method is called when a Firebase database query for a single user
     * has finished executing. The user parameter contains the retrieved user data,
     * or null if no user was found or an error occurred during the operation.</p>
     *
     * @param u The User object retrieved from the database, or null if not found
     *          or if an error occurred during the retrieval process
     */
    void onCallbackUser(User u);

    /**
     * Callback method invoked when a multiple users retrieval operation completes.
     *
     * <p>This method is called when a Firebase database query for multiple users
     * has finished executing. The users parameter contains a list of all retrieved
     * user objects. The list may be empty if no users were found, but will not be null.</p>
     *
     * @param users ArrayList containing all User objects retrieved from the database.
     *              The list is guaranteed to be non-null but may be empty if no users
     *              were found or if an error occurred during the retrieval process
     */
    void onCallbackUsers(ArrayList<User> users);

    /**
     * Callback method invoked when a login or signup operation completes.
     *
     * <p>This method is called when Firebase authentication operations (either login
     * or user registration) have finished executing. It serves as a notification
     * that the authentication process has completed, regardless of whether it was
     * successful or failed. The implementing class should check the authentication
     * state or error conditions separately to determine the actual result.</p>
     *
     * <p><strong>Note:</strong> This callback does not provide success/failure information
     * directly. Implementers should use Firebase authentication state listeners or
     * check authentication status separately to determine the outcome.</p>
     */
    void onCallbackFromLoginOrSignup();
}