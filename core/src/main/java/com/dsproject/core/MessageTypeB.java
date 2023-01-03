package com.dsproject.core;

import java.io.Serializable;
import java.util.LinkedList;

//message type B: sent from accounts microservice to opinions microservice
public class MessageTypeB implements Serializable {
    public Boolean success;
    public Boolean allowed;
    public LinkedList<String> notifications;
    public Integer credits;
    
    public MessageTypeB(Boolean success, Boolean allowed, LinkedList<String> notifications, Integer credits){
        this.success = success;
        this.allowed = allowed;
        this.notifications = notifications;
        this.credits = credits;
    }
}
