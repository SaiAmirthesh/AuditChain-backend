package com.SaiAmirthesh.AuditChain.service;

import com.SaiAmirthesh.AuditChain.dto.AdminAnalyticsDTO;
import com.SaiAmirthesh.AuditChain.dto.UserAnalyticsDTO;
import com.SaiAmirthesh.AuditChain.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private TransactionRepository transactionRepository;

    public UserAnalyticsDTO getUserAnalytics(String accountNo) {
        List<Object[]> categoryResults = transactionRepository.getUserCategorySpending(accountNo);
        List<UserAnalyticsDTO.CategorySpending> categorySpending = categoryResults.stream()
                .map(res -> new UserAnalyticsDTO.CategorySpending(
                        (String) res[0],
                        ((Number) res[1]).doubleValue(),
                        ((Number) res[2]).longValue()
                ))
                .collect(Collectors.toList());

        Map<String, Object> incomeExpense = transactionRepository.getUserIncomeExpense(accountNo);
        double income = 0;
        double expense = 0;
        
        if (incomeExpense != null) {
            income = ((Number) incomeExpense.getOrDefault("income", 0)).doubleValue();
            expense = ((Number) incomeExpense.getOrDefault("expense", 0)).doubleValue();
        }

        return new UserAnalyticsDTO(categorySpending, income, expense);
    }

    public AdminAnalyticsDTO getAdminAnalytics() {
        Map<String, Object> metrics = transactionRepository.getAdminSystemMetrics();
        long totalCount = ((Number) metrics.getOrDefault("total_count", 0)).longValue();
        double totalVolume = ((Number) metrics.getOrDefault("total_volume", 0)).doubleValue();
        double avgVal = ((Number) metrics.getOrDefault("avg_val", 0)).doubleValue();

        List<Object[]> statusResults = transactionRepository.getAdminStatusDistribution();
        List<AdminAnalyticsDTO.StatusCount> statusDistribution = statusResults.stream()
                .map(res -> new AdminAnalyticsDTO.StatusCount((String) res[0], ((Number) res[1]).longValue()))
                .collect(Collectors.toList());

        List<Object[]> channelResults = transactionRepository.getAdminChannelUsage();
        List<AdminAnalyticsDTO.ChannelCount> channelUsage = channelResults.stream()
                .map(res -> new AdminAnalyticsDTO.ChannelCount((String) res[0], ((Number) res[1]).longValue()))
                .collect(Collectors.toList());

        return new AdminAnalyticsDTO(totalCount, totalVolume, avgVal, statusDistribution, channelUsage);
    }

    public void runSystemMaintenance() {
        transactionRepository.callDetectAnomalies();
        transactionRepository.callUpdateAccountRiskScores();
    }
}
