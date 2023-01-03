package com.dsproject.accounts.repository;

import com.dsproject.accounts.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    @Query("SELECT a.name, a.email FROM Account a where a.lastCompanyWorked = :company AND a.email != :currentUser") 
    List<ArrayList<String>> findIdByCompany(String company, String currentUser);

    @Query("SELECT a FROM Account a where a.lastCompanyWorked = :company") 
    List<Account> returnAllInCompany(String company);

    @Query("SELECT a.password FROM Account a where a.email = :email") 
    String findPasswordByEmail(String email);

    // @Query("SELECT a.notifications FROM Account a where a.email = :email") 
    // List<String> getNotificationsByEmail(String email);

    Optional<Account> findById(String email);
}
