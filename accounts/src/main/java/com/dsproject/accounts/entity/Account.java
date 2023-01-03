package com.dsproject.accounts.entity;

import java.util.LinkedList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Account {
    @Id
    private String email;
    private String password;
    private String name;
    private String lastCompanyWorked;
    private int credits;
    @Column(name="NOTIFICATIONS",columnDefinition="longblob")
    private LinkedList<String> notifications; 

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastCompanyWorked() {
        return lastCompanyWorked;
    }

    public void setLastCompanyWorked(String lastCompanyWorked) {
        this.lastCompanyWorked = lastCompanyWorked;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public LinkedList<String>  getNotifications(){
        return notifications;
    }

    public void setNotifications(LinkedList<String>  notifications){
        this.notifications = notifications;
    }
}
