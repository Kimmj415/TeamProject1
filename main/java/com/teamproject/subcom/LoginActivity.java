package com.teamproject.subcom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private Button Login_button, Signup_button;
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()){
            Toast.makeText(LoginActivity.this, "자동 로그인, 환영합니다"+ currentUser.getUid(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, UserMainActivity.class));
            reload();
        }
    }
    private void reload() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmail=findViewById(R.id.login_email);
        mPassword=findViewById(R.id.login_password);

        Signup_button=findViewById(R.id.login_signup);
        Login_button=findViewById(R.id.login_success);

        Login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(user.isEmailVerified()) {
                                        if (user != null) {
                                            Toast.makeText(LoginActivity.this, "로그인 성공." + user.getUid(), Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LoginActivity.this, UserMainActivity.class));
                                        }
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "이메일 인증을 완료하세요.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "로그인 오류.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        Signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }


}