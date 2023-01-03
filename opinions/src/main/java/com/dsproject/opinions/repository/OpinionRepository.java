package com.dsproject.opinions.repository;

import com.dsproject.opinions.entity.Opinion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, String> {
    @Query("SELECT o FROM Opinion o where o.receiverEmail = :email") 
    ArrayList<Opinion> findOpinionByReceiverEmail(String email);

    @Query("SELECT o FROM Opinion o where o.receiverEmail = :email AND viewed = :viewed") 
    ArrayList<Opinion> findOpinionByReceiverEmailViewed(String email, boolean viewed);

    @Query("SELECT o.likes FROM Opinion o where o.receiverEmail = :email AND viewed = :viewed") 
    Optional<ArrayList<String>> findViewedLikesByReceiverEmail(String email, boolean viewed);

    @Query("SELECT o.dislikes FROM Opinion o where o.receiverEmail = :email AND viewed = :viewed") 
    Optional<ArrayList<String>> findViewedDislikesByReceiverEmail(String email, boolean viewed);

    @Query("SELECT o FROM Opinion o where o.senderEmail = :email") 
    ArrayList<Opinion> findOpinionBySenderEmail(String email);

    @Query("SELECT o FROM Opinion o where o.receiverEmail = :receiverEmail AND o.senderEmail = :senderEmail") 
    ArrayList<Opinion> checkIfReviewed(String receiverEmail, String senderEmail);

    Optional<Opinion> findByTimestamp(LocalDateTime timestamp);
}
