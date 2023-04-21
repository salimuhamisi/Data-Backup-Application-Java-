package com.example.autobackup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        Button signup = findViewById(R.id.signup);

        auth=FirebaseAuth.getInstance();

        signup.setOnClickListener(v -> {
            String txt_email=email.getText().toString();
            String txt_password=password.getText().toString();

            if(TextUtils.isEmpty(txt_email)|| TextUtils.isEmpty(txt_password)) {
                Toast.makeText(StartActivity.this, "Empty credentials", Toast.LENGTH_SHORT).show();
            } else if (txt_password.length()<6) {
                Toast.makeText(StartActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
            }else{
                registerUser(txt_email, txt_password);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();

                loginUser(txt_email, txt_password);
            }
        });
        
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(StartActivity.this, "login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(StartActivity.this, task -> {
            if(task.isSuccessful()){
                Toast.makeText(StartActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }else{
                Toast.makeText(StartActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
            }

        });
    }
}