package com.teamproject.subcom.models;

import java.util.Date;

public class Comment {
    private String text; // 댓글 내용
    private String userId; // 댓글 작성자의 사용자 ID
    private String timestamp; // 댓글 작성 시간

    public Comment() {
    }

    public Comment(String text, String userId, String timestamp) {
        this.text = text;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getUserId() {
        return userId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
