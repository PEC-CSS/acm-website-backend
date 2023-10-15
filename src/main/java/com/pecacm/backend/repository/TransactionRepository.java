package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("select t from Transaction t where t.user.email = ?1 order by t.date DESC")
    List<Transaction> findByUser_EmailOrderByDateDesc(String email);

}
