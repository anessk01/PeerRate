package com.dsproject.core;

import java.io.Serializable;
import java.util.ArrayList;

//message type D: sent from aggregator microservice to opinions microservice
public class MessageTypeD  implements Serializable {
    public ArrayList<String> allLikes;
    public ArrayList<String> allDislikes;
    public Boolean reaggregate;
    public String receiverEmail;

    public MessageTypeD( ArrayList<String> allLikes, ArrayList<String> allDislikes, Boolean reaggregate, String receiverEmail){
        this.allLikes = allLikes;
        this.allDislikes = allDislikes;
        this.reaggregate = reaggregate;
        this.receiverEmail = receiverEmail;
    }
}
