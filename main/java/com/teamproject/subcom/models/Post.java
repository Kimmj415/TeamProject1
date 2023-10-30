package com.teamproject.subcom.models;

import java.util.Date;

public class Post {
    private String documentId;
    private String title;

    private String contents;

    private String subjectCode;
    private String timestamp;

    private String postId;

    public Post() {
    }

    public Post(String documentId, String title, String contents, String timestamp, String subjectCode) {
        this.documentId = documentId;
        this.title = title;
        this.contents = contents;
        this.timestamp = timestamp;
        this.subjectCode = subjectCode;
    }

    public Post(String documentId, String title, String contents, String timestamp, String subjectCode, String postId) {
        this.documentId = documentId;
        this.title = title;
        this.contents = contents;
        this.timestamp = timestamp;
        this.subjectCode = subjectCode;
        this.postId=postId;
    }

    public Post(String documentId, String title, String contents) {
        this.documentId = documentId;
        this.title = title;
        this.contents = contents;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String content) {
        this.contents = content;
    }

    @Override
    public String toString() {
        return "Post{" +
                "subcode='" + subjectCode + '\'' +
                "time='" + String.valueOf(timestamp) + '\'' +
                "documentId='" + documentId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + contents + '\'' +
                '}';
    }
    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
