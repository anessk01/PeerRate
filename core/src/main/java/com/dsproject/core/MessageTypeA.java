package com.dsproject.core;

import java.io.Serializable;

//message type A: sent from opinions microservice to accounts microservice
public class MessageTypeA implements Serializable {
    public String senderEmail;
    public String receiverEmail;
    public String type;  //should be either increment, decrement or fetch

    public MessageTypeA(String senderEmail, String receiverEmail, String type){
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.type = type;
    }
}
