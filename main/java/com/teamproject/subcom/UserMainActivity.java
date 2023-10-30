package com.teamproject.subcom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.teamproject.subcom.adapters.UserFavoriteAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserMainActivity extends AppCompatActivity {

    private Button Logout_button;
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();

    private EditText subCodeEditText;
    private Button codeSearchButton;
    private Button Lobby_button;

    private RecyclerView favoriteRecyclerView;
    private UserFavoriteAdapter favoriteAdapter;
    private List<String> favoriteBoards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        Logout_button=findViewById(R.id.logout);
        subCodeEditText = findViewById(R.id.sub_code);
        codeSearchButton = findViewById(R.id.code_search);
        Lobby_button=findViewById(R.id.lobbybutton);

        favoriteRecyclerView = findViewById(R.id.codelist);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteBoards = new ArrayList<>(); // 즐겨찾기 목록 초기화
        favoriteAdapter = new UserFavoriteAdapter(favoriteBoards, UserMainActivity.this);
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        Logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(UserMainActivity.this, LoginActivity.class));
            }
        });

        Lobby_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subCode = "스터디 모집";
                Intent intent = new Intent(UserMainActivity.this, SubBoardActivity.class);
                intent.putExtra("SUBJECT_CODE", subCode);
                startActivity(intent);
            }
        });

        codeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subCode = subCodeEditText.getText().toString();
                Intent intent = new Intent(UserMainActivity.this, SubBoardActivity.class);
                intent.putExtra("SUBJECT_CODE", subCode);
                startActivity(intent);
            }
        });

        fetchFavoriteBoards();
    }

    private void fetchFavoriteBoards() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("user").document(userId);

        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // "favoriteBoards" 필드에서 즐겨찾기 목록 가져오기
                            List<String> favorites = (List<String>) documentSnapshot.get("favoriteBoards");
                            if (favorites != null) {
                                favoriteBoards.clear();
                                favoriteBoards.addAll(favorites);
                                favoriteAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }
}