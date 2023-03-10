package com.dsproject.opinions.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Opinion {
    @Id
    private LocalDateTime timestamp;
    private String senderEmail;
    private String receiverEmail;
    @Column(length = 7000)
    private String likes;
    @Column(length = 7000)
    private String dislikes;
    private Boolean viewed;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getDislikes() {
        return dislikes;
    }

    public void setDislikes(String dislikes) {
        this.dislikes = dislikes;
    }

    public Boolean getViewed(){
        return viewed;
    }

    public void setViewed(Boolean viewed){
        this.viewed = viewed;
    }
}
