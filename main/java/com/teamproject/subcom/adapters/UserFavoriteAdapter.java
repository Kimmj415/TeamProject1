package com.teamproject.subcom.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.teamproject.subcom.R;
import com.teamproject.subcom.SubBoardActivity;

import java.util.List;

public class UserFavoriteAdapter extends RecyclerView.Adapter<UserFavoriteAdapter.ViewHolder> {

    private List<String> favoriteBoards;
    private Activity activity;

    public UserFavoriteAdapter(List<String> favoriteBoards, Activity activity) {
        this.favoriteBoards = favoriteBoards;
        this.activity=activity;
    }

    public void setFavoriteBoards(List<String> favoriteBoards) {
        this.favoriteBoards = favoriteBoards;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String boardCode = favoriteBoards.get(position);
        holder.boardCodeTextView.setText(boardCode);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭한 아이템의 과목 코드를 SubBoardActivity로 전달
                Intent intent = new Intent(activity, SubBoardActivity.class);
                intent.putExtra("SUBJECT_CODE", boardCode);
                activity.startActivity(intent);
            }
        });

        Button removeFavoriteButton = holder.itemView.findViewById(R.id.favorite_remove_button);
        removeFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 여기에서 Firebase나 로컬 데이터베이스를 업데이트해야 할 수 있음
                showRemoveConfirmationDialog(boardCode);
            }
        });
    }

    private void showRemoveConfirmationDialog(String boardCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("삭제 확인")
                .setMessage(boardCode+" 게시판을 즐겨찾기에서 삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        favoriteBoards.remove(boardCode);
                        notifyDataSetChanged();
                        removeFromFavorites(boardCode);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    private void removeFromFavorites(String subCode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return favoriteBoards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView boardCodeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            boardCodeTextView = itemView.findViewById(R.id.favorite_code);
        }
    }
}

