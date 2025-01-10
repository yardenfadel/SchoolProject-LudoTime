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

public class FireBaseController {
    private static FirebaseAuth mAuth;
    private static FirebaseDatabase database;
    private static DatabaseReference ref;

    public static FirebaseAuth getAuth(){
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }
    public static FirebaseDatabase getDatabase(){
        if (database == null)
            database = FirebaseDatabase.getInstance();
        return database;
    }
    public static DatabaseReference getReference(){
        if (ref == null)
            ref = getDatabase().getReference("Users");
        return ref;
    }

    public static void logOut(){
        getAuth().signOut();
    }


    public void createUser(User user){
        getAuth().createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "createUserWithEmail:success");
                            getReference().child(task.getResult().getUser().getUid()).setValue(user);

                        } else {
                            Log.d("TAG", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }
    public void loginUser(String email, String password){
        getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithEmail:success");
                        } else {
                            Log.d("TAG", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

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
}
