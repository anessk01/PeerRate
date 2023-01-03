package com.dsproject.aggregator.repository;

import com.dsproject.aggregator.entity.Aggregator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AggregatorRepository extends JpaRepository<Aggregator, String> {
    Optional<Aggregator> findById(String email);
}
