package com.SaiAmirthesh.AuditChain.repository;

import com.SaiAmirthesh.AuditChain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Page<Transaction> findByFromAccountOrToAccountOrderByTimestampDesc(String fromAccount, String toAccount, Pageable pageable);

    Page<Transaction> findAllByOrderByTimestampDesc(Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "call detect_anomalies()", nativeQuery = true)
    void callDetectAnomalies();

    @Transactional
    @Modifying
    @Query(value = "call update_account_risk_scores()", nativeQuery = true)
    void callUpdateAccountRiskScores();

    @Query(value = "select category, sum(amount) as amount, count(*) as count from transactions where from_account = :acc group by category", nativeQuery = true)
    List<Object[]> getUserCategorySpending(@Param("acc") String acc);

    @Query(value = "select sum(case when to_account = :acc then amount else 0 end) as income, sum(case when from_account = :acc then amount else 0 end) as expense from transactions where from_account = :acc or to_account = :acc", nativeQuery = true)
    Map<String, Object> getUserIncomeExpense(@Param("acc") String acc);

    @Query(value = "select count(*) as total_count, sum(amount) as total_volume, avg(amount) as avg_val from transactions", nativeQuery = true)
    Map<String, Object> getAdminSystemMetrics();

    @Query(value = "select status, count(*) as count from transactions group by status", nativeQuery = true)
    List<Object[]> getAdminStatusDistribution();

    @Query(value = "select channel, count(*) as count from transactions group by channel", nativeQuery = true)
    List<Object[]> getAdminChannelUsage();
}
