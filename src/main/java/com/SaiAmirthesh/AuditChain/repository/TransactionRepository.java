package com.SaiAmirthesh.AuditChain.repository;

import com.SaiAmirthesh.AuditChain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Transaction t WHERE t.fromAccount = :user OR t.toAccount = :user ORDER BY t.timestamp DESC")
    java.util.List<Transaction> findUserTransactions(@org.springframework.data.repository.query.Param("user") String user);

    List<Transaction> findByFromAccountOrToAccountOrderByTimestampDesc(String fromAccount, String toAccount);
    List<Transaction> findTop10ByFromAccountOrToAccountOrderByTimestampDesc(String fromAccount, String toAccount);
}
