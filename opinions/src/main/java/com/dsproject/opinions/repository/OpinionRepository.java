package com.dsproject.opinions.repository;

import com.dsproject.opinions.entity.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, String> {
    @Query("SELECT a.name FROM Account a where a.lastCompanyWorked = :company") 
    ArrayList<String> findIdByCompany(String company);

    @Query("SELECT a.password FROM Account a where a.email = :email") 
    String findPasswordByEmail(String email);

    // @Query("SELECT a.email FROM Account a where a.email = :email") 
    Optional<Opinion> findById(String email);
}
