package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Transaction;
import com.pecacm.backend.enums.EventRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByUserId(Integer userId, PageRequest pageRequest);
    Page<Transaction> findByUserIdAndRole(Integer userId, EventRole role, PageRequest pageRequest);
}
