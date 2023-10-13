package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Transaction;
import com.pecacm.backend.enums.EventRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByUserId(Integer userId);
    List<Transaction> findByUserIdAndRole(Integer userId, EventRole role);
}
