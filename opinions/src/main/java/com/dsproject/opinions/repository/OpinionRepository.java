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
    Optional<ArrayList<Opinion>> findOpinionByReceiverEmail(String email);

    @Query("SELECT o FROM Opinion o where o.senderEmail = :email") 
    Optional<ArrayList<Opinion>> findOpinionBySenderEmail(String email);

    @Query("SELECT o FROM Opinion o where o.receiverEmail = :receiverEmail AND o.senderEmail = :senderEmail") 
    String checkIfReviewed(String receiverEmail, String senderEmail);

    Optional<Opinion> findByTimestamp(LocalDateTime timestamp);
}
