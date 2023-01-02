package com.dsproject.accounts.helpers;

import java.util.LinkedList;


public class NotificationManager {
    private static final int MAXNOTIFCOUNT = 10;
    
    /** 
     * add to notifications
     */
    public static LinkedList<String> addNotification(String newNotification, LinkedList<String> notifications){
        // if the number of notifications has exceeded MAXNOTIFCOUNT, remove the oldest notification so that a new one is added
        if(notifications == null){
            notifications = new LinkedList<String>();
        }
        
        if(notifications.size() == MAXNOTIFCOUNT){
            notifications.remove();
        }
        notifications.add(newNotification);
        return notifications;
    }
}
