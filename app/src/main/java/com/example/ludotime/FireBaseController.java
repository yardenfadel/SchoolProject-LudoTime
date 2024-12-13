package com.example.ludotime;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FireBaseController {
    private static FirebaseAuth mAuth;

    public static FirebaseAuth getAuth(){
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }


    public void createUser(User user){
        getAuth().createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "createUserWithEmail:success");
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
}
