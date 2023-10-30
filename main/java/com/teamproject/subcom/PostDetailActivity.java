package com.teamproject.subcom;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.teamproject.subcom.adapters.CommentAdapter;
import com.teamproject.subcom.models.Comment;
import com.teamproject.subcom.models.Post;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView dateTextView;
    private TextView contentTextView;
    private EditText commentEditText;
    private Button registerButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        RecyclerView commentRecyclerView = findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        titleTextView = findViewById(R.id.title_tv);
        dateTextView = findViewById(R.id.date_tv);
        contentTextView = findViewById(R.id.content_tv);
        commentEditText = findViewById(R.id.comment_et);
        registerButton = findViewById(R.id.reg_button);
        db = FirebaseFirestore.getInstance();

        String subCode = getIntent().getStringExtra("SUBCODE");
        String docId = getIntent().getStringExtra("DOC_ID");
        String date = getIntent().getStringExtra("DATE");
        String postId = getIntent().getStringExtra("POST_ID");

        db.collection("게시판")
                .document(subCode)
                .collection("게시글")
                .document(postId)
                .collection("댓글")
                .orderBy("timestamp", Query.Direction.DESCENDING) // 타임스탬프 내림차순 정렬
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // 오류 처리
                            return;
                        }

                        // 댓글 데이터를 리스트로 변환
                        List<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Comment comment = document.toObject(Comment.class);
                            comments.add(comment);
                        }

                        // CommentAdapter에 댓글 데이터 설정
                        CommentAdapter commentAdapter = new CommentAdapter(comments,PostDetailActivity.this);
                        commentRecyclerView.setAdapter(commentAdapter);
                    }
                });

        registerButton.setOnClickListener(view -> {
            String comment = filterProfanity(commentEditText.getText().toString().trim());
            if (!comment.isEmpty()) {
                Date timestamp = new Date();
                Comment newcomment = new Comment(comment, mAuth.getCurrentUser().getUid(), timestamp.toString());
                db.collection("게시판")
                        .document(subCode)
                        .collection("게시글")
                        .document(postId)
                        .collection("댓글")
                        .add(newcomment)
                        .addOnSuccessListener(documentReference -> {
                            // 댓글 추가 성공 시 처리
                            Toast.makeText(PostDetailActivity.this, "댓글이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            commentEditText.setText("");
                            Intent intent = new Intent(PostDetailActivity.this, PostDetailActivity.class);
                            intent.putExtra("DOC_ID", docId);
                            intent.putExtra("DATE", date);
                            intent.putExtra("SUBCODE", subCode);
                            intent.putExtra("POST_ID", postId);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            // 댓글 추가 실패 시 처리
                            Toast.makeText(PostDetailActivity.this, "댓글 추가 실패.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(PostDetailActivity.this, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        db.collection("게시판")
                .document(subCode)
                .collection("게시글")
                .whereEqualTo("postId", postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // 일치하는 문서가 하나 이상 있다면, 첫 번째 문서를 사용합니다.
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                // DocumentSnapshot으로부터 Post 객체 가져오기
                                Post post = document.toObject(Post.class);
                                if (post != null) {
                                    // Post 객체에서 데이터를 가져와 TextView에 설정
                                    titleTextView.setText(post.getTitle().toString());
                                    dateTextView.setText(post.getTimestamp().toString());
                                    contentTextView.setText(post.getContents().toString());
                                }
                            } else {
                                Toast.makeText(PostDetailActivity.this, "일치하는 게시물 없음", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(PostDetailActivity.this, "데이터 로딩 실패.", Toast.LENGTH_SHORT).show();
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