package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Transaction;
import com.pecacm.backend.enums.EventRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByUserId(Integer userId, PageRequest pageRequest);

    Page<Transaction> findByUserIdAndRole(Integer userId, EventRole role, PageRequest pageRequest);

    @Query("SELECT t from Transaction t WHERE t.event.id = :eventId AND t.role IN :roles")
    List<Transaction> findListByEventIdAndRoles(Integer eventId, List<EventRole> roles);

    Page<Transaction> findByEventIdAndRole(Integer eventId, EventRole eventRole, PageRequest pageRequest);
}