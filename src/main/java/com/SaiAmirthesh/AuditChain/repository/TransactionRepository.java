package com.SaiAmirthesh.AuditChain.repository;

import com.SaiAmirthesh.AuditChain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountOrToAccountOrderByTimestampDesc(String fromAccount, String toAccount);
    List<Transaction> findTop10ByFromAccountOrToAccountOrderByTimestampDesc(String fromAccount, String toAccount);
}
