package com.nackademin.foureverhh.jogtracker180417;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editEmail,editPassword;
    private Button btn_logIn;
    private Button btn_signUp;
    private FirebaseAuth myAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editEmail = findViewById(R.id.logInEmail);
        editPassword = findViewById(R.id.logInPassword);
        btn_logIn = findViewById(R.id.logIn);
        btn_signUp = findViewById(R.id.signUp);
        myAuth = FirebaseAuth.getInstance();
        btn_logIn.setOnClickListener(this);
        btn_signUp.setOnClickListener(this);
/*
        btn_logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editEmail.getError() == null){
                    signUp();
                }
            }
        });
        */
    }

    public void login(){
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editEmail.setError("Input a valid email");
            editEmail.requestFocus();
        }
        if(password.length() == 0){
            editPassword.setError("Password is requested");
            editPassword.requestFocus();
        }
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(),"Email or password is empty",Toast.LENGTH_SHORT).show();
        }else{
            myAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                 if(task.isSuccessful()){
                     finish();
                    startActivity(new Intent(MainActivity.this,Training.class));
                 }else{
                  Toast.makeText(getApplicationContext(),"Wrong email or password",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void signUp(){
        startActivity(new Intent(MainActivity.this,SignUp.class));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.logIn :
                login();
                break;
           case R.id.signUp :
                signUp();
                break;
        }

    }

}
