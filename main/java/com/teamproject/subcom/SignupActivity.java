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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private EditText mEmailText, mPasswordText, re_mPasswordText;
    private Button signup_button;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore= FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mEmailText=findViewById(R.id.signup_email);
        mPasswordText=findViewById(R.id.signup_password);
        re_mPasswordText=findViewById(R.id.re_signup_password);

        signup_button=findViewById(R.id.signup_button);

        mAuth = FirebaseAuth.getInstance();

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPasswordText.getText().toString().isEmpty() || re_mPasswordText.getText().toString().isEmpty() || mEmailText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "입력칸을 모두 채워주세요!",Toast.LENGTH_SHORT).show();
                }
                else if(!mPasswordText.getText().toString().equals(re_mPasswordText.getText().toString())){
                    Toast.makeText(getApplicationContext(), "비밀번호가 서로 일치하지 않습니다!",Toast.LENGTH_SHORT).show();
                }
                else if(!(mEmailText.getText().toString().length()>10 &&mEmailText.getText().toString().endsWith("@mju.ac.kr"))){
                    Toast.makeText(getApplicationContext(), "올바른 형식의 이메일이 아닙니다!",Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.createUserWithEmailAndPassword(mEmailText.getText().toString(), mPasswordText.getText().toString())
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(SignupActivity.this, "인증 이메일을 발송했습니다.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                        if (user != null) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put(FirebaseID.documentId, user.getUid());
                                            userMap.put(FirebaseID.email, mEmailText.getText().toString());
                                            userMap.put(FirebaseID.password, mPasswordText.getText().toString());
                                            List<String> initialFavorites = new ArrayList<>();
                                            userMap.put("favoriteBoards", initialFavorites);
                                            mStore.collection(FirebaseID.user).document(user.getUid()).set(userMap, SetOptions.merge());
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(SignupActivity.this, "회원가입 오류. 조건을 모두 충족해주세요.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    // [END create_user_with_email]
                }
            }
        });
    }

}