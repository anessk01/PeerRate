package com.dsproject.opinions.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OpinionPK implements Serializable{
    private LocalDateTime timestamp;
    private String senderEmail;

    public OpinionPK(LocalDateTime timestamp, String senderEmail){
        this.timestamp = timestamp;
        this.senderEmail = senderEmail;
    }
}
