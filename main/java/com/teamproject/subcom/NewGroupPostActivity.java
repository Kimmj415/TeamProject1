package com.teamproject.subcom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.teamproject.subcom.models.Post;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewGroupPostActivity extends AppCompatActivity {

    private String subjectCode;
    private EditText editTextTitle;
    private EditText editTextContent;
    private EditText editTextTopic;
    private EditText editTextIntro;
    private EditText editTextPrefer;
    private EditText editTextUrl;
    private Button submitButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_post);
        Intent intent = getIntent();
        if (intent != null) {
            subjectCode = intent.getStringExtra("SUBJECT_CODE");
        }

        editTextTitle = findViewById(R.id.title_et);
        editTextContent = findViewById(R.id.content_et);
        submitButton = findViewById(R.id.reg_button);
        editTextTopic=findViewById(R.id.topic_et);
        editTextIntro=findViewById(R.id.intro_et);
        editTextPrefer=findViewById(R.id.prefer_et);
        editTextUrl=findViewById(R.id.url_et);

        db = FirebaseFirestore.getInstance();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = filterProfanity("["+editTextTopic.getText().toString().trim()+"]"+editTextTitle.getText().toString().trim());
                String content = filterProfanity(editTextIntro.getText().toString().trim()+"\n"+editTextContent.getText().toString().trim())+"\n"+editTextPrefer.getText().toString().trim()+"\n"+editTextUrl.getText().toString().trim();
                String author = mAuth.getCurrentUser().getUid();
                String timestamp = new Date().toString();

                savePostToFirestore(subjectCode, title, content, author, timestamp);
            }
        });
    }

    private void savePostToFirestore(String subjectCode, String title, String content, String author, String timestamp) {

        String collectionPath = "게시판/" + subjectCode + "/게시글";

        Post post = new Post(author, title, content, timestamp,subjectCode);

        db.collection(collectionPath)
                .add(post)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            String documentId = task.getResult().getId();
                            Map<String, Object> data = new HashMap<>();
                            data.put("postId", documentId);
                            db.collection(collectionPath).document(documentId)
                                    .set(data, SetOptions.merge()) // 문서를 업데이트 또는 생성 (merge 옵션을 사용하여 중복 방지)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // 데이터 추가 성공
                                                Intent next = new Intent(new Intent(NewGroupPostActivity.this, SubBoardActivity.class));
                                                String subjectCode = getIntent().getStringExtra("SUBJECT_CODE");
                                                next.putExtra("SUBJECT_CODE", subjectCode);
                                                startActivity(next);
                                                finish();
                                            } else {
                                                Toast.makeText(NewGroupPostActivity.this, "오류 발생.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(NewGroupPostActivity.this, "오류 발생.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public String filterProfanity(String input) {
        String replacement = "*";
        String[] profanityList = {"시발","씨발","ㅅㅂ","시바","ㅆㅂ","씨바","개같다","개같네", "병신", "좆까", "존나", "미친", "개새끼","찐따","새끼","지랄","ㅈ같네","ㅈ같","좆같","ㅂㅅ","ㅄ"};

        for (String profanity : profanityList) {
            if (input.contains(profanity)) {
                String replacementString = new String(new char[profanity.length()]).replace("\0", replacement);
                input = input.replaceAll(profanity, replacementString);
            }
        }

        return input;
    }
}