package com.teamproject.subcom.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.Context;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.teamproject.subcom.R;
import com.teamproject.subcom.models.Comment;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>{
    private List<Comment> comments;
    private Activity activity;
    // 생성자에서 댓글 목록을 받아 초기화
    public CommentAdapter(List<Comment> comments, Activity activity) {
        this.comments = comments;
        this.activity=activity;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 뷰 홀더 생성 및 레이아웃 연결
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        // 댓글 데이터를 뷰 홀더에 바인딩
        Comment comment = comments.get(position);
        holder.commentTextView.setText(comment.getText());
        holder.authorTextView.setText("익명");
        holder.timestampTextView.setText(comment.getTimestamp());

        holder.reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDialog(activity, comment);
            }
        });
    }

    private void showReportDialog(Activity context, Comment comment) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("댓글 신고");
        alertDialogBuilder.setMessage("해당 댓글을 신고하시겠습니까?");
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 댓글을 신고하는 코드를 여기에 추가
                String reportedText = comment.getText();
                String reportedTimestamp = comment.getTimestamp();
                String reportedAuthor = comment.getUserId();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("reportedText", reportedText);
                reportData.put("reportedTimestamp", reportedTimestamp);
                reportData.put("reportedAuthor", reportedAuthor);

                db.collection("reportedComments").add(reportData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(context, "댓글이 신고되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "댓글 신고에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        alertDialogBuilder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView commentTextView;
        public TextView authorTextView;
        public TextView timestampTextView;
        public TextView reportButton;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comment_text_view);
            authorTextView = itemView.findViewById(R.id.author_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            reportButton = itemView.findViewById(R.id.report_button);
        }
    }
}

