package com.nackademin.foureverhh.jogtracker180417;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {
    private EditText editSignUpEmail,editSignUpPassword;
    private FirebaseAuth myAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editSignUpEmail = findViewById(R.id.signUpEmail);
        editSignUpPassword = findViewById(R.id.signUpPassword);

        ((Button)findViewById(R.id.signUpButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpAccount();
            }
        });
        myAuth = FirebaseAuth.getInstance();
    }

    public void signUpAccount(){
        String email = editSignUpEmail.getText().toString().trim();
        String password = editSignUpPassword.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editSignUpEmail.setError("Input a valid email");
            editSignUpEmail.requestFocus();
        }
        if(password.length() == 0 || password.length() <6){
            editSignUpPassword.setError("Password should more than 6 digits");
            editSignUpPassword.requestFocus();
        }

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(),"Email or password is empty",Toast.LENGTH_SHORT).show();
        }else{
            myAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        finish();
                        startActivity(new Intent(SignUp.this,MainActivity.class));
                    }else{
                        Log.w("signUp fail", "createUserWithEmail:failure", task.getException());
                        if(task.getException() instanceof FirebaseAuthUserCollisionException){
                            Toast.makeText(getApplicationContext(),"You have registered",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
}
