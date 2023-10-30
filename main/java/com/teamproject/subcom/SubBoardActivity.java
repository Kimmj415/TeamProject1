package com.teamproject.subcom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.teamproject.subcom.adapters.RecyclerViewAdapter;
import com.teamproject.subcom.models.Post;
import com.teamproject.subcom.search.BM25Similarity;
import org.apache.commons.text.similarity.JaccardSimilarity;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SubBoardActivity extends AppCompatActivity {
    private TextView subBoardTitle;
    private FloatingActionButton board_add_button;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private List<Post> posts = new ArrayList<>();
    private FirebaseFirestore db;
    private FloatingActionButton favoriteButton;
    private boolean isFavorite = false;
    private String subCode;
    private ImageButton back_button;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_board);

        subBoardTitle = findViewById(R.id.sub_board_title);
        board_add_button = findViewById(R.id.board_add_button);
        recyclerView = findViewById(R.id.postRecyclerView);
        searchView = findViewById(R.id.searchView);
        favoriteButton = findViewById(R.id.favoriteButton);
        back_button=findViewById(R.id.backButton);

        Intent intent = getIntent();
        if (intent != null) {
            subCode = intent.getStringExtra("SUBJECT_CODE");
            if (subCode != null) {
                String boardTitle =subCode+" 게시판";
                subBoardTitle.setText(boardTitle);
                fetchPostsFromFirestore(subCode);
            }
        }
        checkIfFavorite(subCode);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SubBoardActivity.this, UserMainActivity.class ));
            }
        });
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorite) {
                    // 사용자의 즐겨찾기 목록에서 제거
                    removeFromFavorites(subCode);
                } else {
                    // 사용자의 즐겨찾기 목록에 추가
                    addToFavorites(subCode);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Post> filteredPosts = new ArrayList<>();
                for (Post post : posts) {

                    if (post.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        filteredPosts.add(post);
                    }
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(SubBoardActivity.this));
                adapter = new RecyclerViewAdapter(filteredPosts,SubBoardActivity.this);
                recyclerView.setAdapter(adapter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Post> filteredPosts = new ArrayList<>();
                JaccardSimilarity similarity = new JaccardSimilarity();
                for (Post post : posts) {
                    if(similarity.apply(post.getTitle().toLowerCase(),newText.toLowerCase())>0.4||similarity.apply(post.getContents().toLowerCase(),newText.toLowerCase())>0.4){
                        filteredPosts.add(post);
                    }
                    else {
                        if (post.getTitle().toLowerCase().contains(newText.toLowerCase()) || post.getContents().toLowerCase().contains(newText.toLowerCase())) {
                            filteredPosts.add(post);
                        }
                    }
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(SubBoardActivity.this));
                adapter = new RecyclerViewAdapter(filteredPosts,SubBoardActivity.this);
                recyclerView.setAdapter(adapter);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recyclerView.setLayoutManager(new LinearLayoutManager(SubBoardActivity.this));
                adapter = new RecyclerViewAdapter(posts,SubBoardActivity.this);
                recyclerView.setAdapter(adapter);
                return false;
            }
        });

        board_add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subjectCode = getIntent().getStringExtra("SUBJECT_CODE");
                if(subjectCode.equals("스터디 모집")){
                    Intent newPostIntent = new Intent(SubBoardActivity.this, NewGroupPostActivity.class);
                    newPostIntent.putExtra("SUBJECT_CODE", subjectCode);
                    startActivity(newPostIntent);
                }
                else{
                    Intent newPostIntent = new Intent(SubBoardActivity.this, NewPostActivity.class);
                    newPostIntent.putExtra("SUBJECT_CODE", subjectCode);
                    startActivity(newPostIntent);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(posts,SubBoardActivity.this);
        recyclerView.setAdapter(adapter);
    }
    private void fetchPostsFromFirestore(String subjectCode) {
        db = FirebaseFirestore.getInstance();
        String collectionPath = "게시판/" + subjectCode + "/게시글";
        Query query = db.collection(collectionPath)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Post post = documentSnapshot.toObject(Post.class);
                            posts.add(post);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void removeFromFavorites(String subCode) {
        db.collection("user").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> favoriteBoards = (List<String>) documentSnapshot.get("favoriteBoards");
                            if (favoriteBoards != null) {
                                favoriteBoards.remove(subCode);
                                // Firestore에 업데이트된 favoriteBoards 리스트 저장
                                db.collection("user").document(userId)
                                        .update("favoriteBoards", favoriteBoards)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // 즐겨찾기 목록에서 제거한 경우 아이콘 변경
                                                favoriteButton.setImageResource(R.drawable.blank_star);
                                                isFavorite = false;
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void addToFavorites(String subCode) {
        db.collection("user").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> favoriteBoards = (List<String>) documentSnapshot.get("favoriteBoards");
                            if (favoriteBoards == null) {
                                favoriteBoards = new ArrayList<>();
                            }
                            favoriteBoards.add(subCode);
                            // Firestore에 업데이트된 favoriteBoards 리스트 저장
                            db.collection("user").document(userId)
                                    .update("favoriteBoards", favoriteBoards)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // 즐겨찾기 목록에 추가한 경우 아이콘 변경
                                            favoriteButton.setImageResource(R.drawable.filled_star);
                                            isFavorite = true;
                                        }
                                    });
                        }
                    }
                });
    }

    private void checkIfFavorite(String subCode) {
        db.collection("user").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> favoriteBoards = (List<String>) documentSnapshot.get("favoriteBoards");
                            if (favoriteBoards != null && favoriteBoards.contains(subCode)) {
                                // 해당 게시판이 즐겨찾기 목록에 있는 경우
                                favoriteButton.setImageResource(R.drawable.filled_star);
                                isFavorite = true;
                            } else {
                                // 해당 게시판이 즐겨찾기 목록에 없는 경우
                                favoriteButton.setImageResource(R.drawable.blank_star);
                                isFavorite = false;
                            }
                        }
                    }
                });
    }
}