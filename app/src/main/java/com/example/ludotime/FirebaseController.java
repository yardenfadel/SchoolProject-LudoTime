/**
 * FirebaseController.java
 *
 * Controller class that manages Firebase authentication and database operations.
 * Provides methods for user registration, login, logout, and data retrieval.
 * Uses singleton pattern for Firebase instances to ensure consistent access.
 */
package com.example.ludotime;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseController {
    /** Firebase Authentication instance */
    private static FirebaseAuth mAuth;

    /** Firebase Realtime Database instance */
    private static FirebaseDatabase database;

    /** Database reference pointing to the "Users" node */
    private static DatabaseReference ref;

    /**
     * Get the Firebase Authentication instance using singleton pattern
     * @return FirebaseAuth instance for authentication operations
     */
    public static FirebaseAuth getAuth(){
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }

    /**
     * Get the Firebase Database instance using singleton pattern
     * @return FirebaseDatabase instance for database operations
     */
    public static FirebaseDatabase getDatabase(){
        if (database == null)
            database = FirebaseDatabase.getInstance();
        return database;
    }

    /**
     * Get the database reference to the "Users" node using singleton pattern
     * @return DatabaseReference pointing to the Users collection
     */
    public static DatabaseReference getReference(){
        if (ref == null)
            ref = getDatabase().getReference("Users");
        return ref;
    }

    /**
     * Check if a user is currently authenticated
     * @return true if a user is logged in, false otherwise
     */
    public boolean isConnected(){
        return getAuth().getCurrentUser()!=null;
    }

    /**
     * Sign out the current user from Firebase Authentication
     */
    public static void logOut(){
        getAuth().signOut();
    }

    /**
     * Create a new user account with email and password
     * After successful authentication, stores the user data in the database
     *
     * @param user The User object containing registration information
     * @param fBL FireBaseListener callback for handling completion events
     */
    public void createUser(User user, FireBaseListener fBL){
        getAuth().createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "createUserWithEmail:success");
                            // Wait for setValue to complete before calling callback
                            System.out.println("user created 1");
                            getReference().child(task.getResult().getUser().getUid()).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                System.out.println("callback called 2");
                                                fBL.onCallbackFromLoginOrSignup();
                                            }
                                        }
                                    });
                        } else {
                            Log.d("TAG", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    /**
     * Authenticate user with email and password
     *
     * @param email The user's email address
     * @param password The user's password
     * @param fBL FireBaseListener callback for handling authentication result
     */
    public void loginUser(String email, String password, FireBaseListener fBL){
        getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithEmail:success");
                            fBL.onCallbackFromLoginOrSignup();
                        } else {
                            Log.d("TAG", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    /**
     * Read the current user's data from the database
     * Sets up a real-time listener that will trigger whenever the user's data changes
     *
     * @param fBL FireBaseListener callback that receives the User object
     */
    public void readUser(FireBaseListener fBL){
        getReference().child(getAuth().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                fBL.onCallbackUser(user);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Read all users from the database
     * Sets up a real-time listener that will trigger whenever any user data changes
     *
     * @param fBL FireBaseListener callback that receives the ArrayList of User objects
     */
    public void readUsersList(FireBaseListener fBL){
        getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> users = new ArrayList<>();
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    User u = data.getValue(User.class);
                    users.add(u);
                }
                fBL.onCallbackUsers(users);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });
    }
}