package com.teamproject.subcom.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.Context;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.teamproject.subcom.FirebaseID;
import com.teamproject.subcom.GroupPostDetailActivity;
import com.teamproject.subcom.NewPostActivity;
import com.teamproject.subcom.PostDetailActivity;
import com.teamproject.subcom.R;
import com.teamproject.subcom.models.Comment;
import com.teamproject.subcom.models.Post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Post> posts;
    private Activity activity;

    public RecyclerViewAdapter(List<Post> posts) {
        this.posts = posts;
    }

    public RecyclerViewAdapter(List<Post> posts, Activity activity) {
        this.posts = posts;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        if(post.getSubjectCode().equals("스터디 모집")){
            holder.titleTextView.setText(post.getTitle());
            String[] parts=post.getContents().split("\n");
            if (parts[1].length()>15){
                holder.contentTextView.setText(parts[1].substring(0,15)+"...");
            }
            else{
                holder.contentTextView.setText(parts[1]);
            }
            holder.timeTextView.setText(post.getTimestamp().toString());
        }
        else{
            holder.titleTextView.setText(post.getTitle());
            if (post.getContents().length()>15){
                holder.contentTextView.setText(post.getContents().substring(0,15)+"...");
            }
            else{
                holder.contentTextView.setText(post.getContents());
            }
            holder.timeTextView.setText(post.getTimestamp().toString());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(post.getSubjectCode().equals("스터디 모집")) {
                    Intent intent = new Intent(activity, GroupPostDetailActivity.class);
                    intent.putExtra("DOC_ID", post.getDocumentId());
                    intent.putExtra("DATE", post.getTimestamp());
                    intent.putExtra("SUBCODE", post.getSubjectCode());
                    intent.putExtra("POST_ID", post.getPostId());
                    activity.startActivity(intent);
                }
                else{
                    Intent intent = new Intent(activity, PostDetailActivity.class);
                    intent.putExtra("DOC_ID", post.getDocumentId());
                    intent.putExtra("DATE", post.getTimestamp());
                    intent.putExtra("SUBCODE", post.getSubjectCode());
                    intent.putExtra("POST_ID", post.getPostId());
                    activity.startActivity(intent);
                }
            }
        });

        holder.reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDialog(activity, post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView contentTextView;
        public TextView timeTextView;
        public TextView reportButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_post_title);
            contentTextView = itemView.findViewById(R.id.item_post_contents);
            timeTextView=itemView.findViewById(R.id.item_post_time);
            reportButton = itemView.findViewById(R.id.reportpost_button);
        }
    }

    private void showReportDialog(Activity context, Post post) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("댓글 신고");
        alertDialogBuilder.setMessage("해당 게시글을 신고하시겠습니까?");
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 댓글을 신고하는 코드를 여기에 추가
                String reportedComments = post.getContents();
                String reportedTitle=post.getTitle();
                String reportedPostId=post.getPostId();
                String postTimestamp = post.getTimestamp();
                String reportedAuthor = post.getDocumentId();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("reportedTitle", reportedTitle);
                reportData.put("reportedContents", reportedComments);
                reportData.put("reportedAuthor", reportedAuthor);
                reportData.put("reportedPostId", reportedPostId);
                reportData.put("postTimestamp", postTimestamp);

                db.collection("reportedPosts").add(reportData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(context, "게시글이 신고되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@org.checkerframework.checker.nullness.qual.NonNull Exception e) {
                                Toast.makeText(context, "게시글 신고에 실패했습니다.", Toast.LENGTH_SHORT).show();
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
}
