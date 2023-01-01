package com.dsproject.accounts.repository;

import com.dsproject.accounts.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findById(String email);
}
