package com.dsproject.aggregator.entity;

import java.util.LinkedHashMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class Aggregator {
    @Id
    private String email;
    private Boolean reaggregate;
    @Lob
    private LinkedHashMap<String, Double> results;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getReaggregate(){
        return reaggregate;
    }

    public void setReaggregate(Boolean reaggregate){
        this.reaggregate = reaggregate;
    }
}
