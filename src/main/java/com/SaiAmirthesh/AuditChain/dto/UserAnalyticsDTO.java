package com.SaiAmirthesh.AuditChain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsDTO {
    private List<CategorySpending> categorySpending;
    private double totalIncome;
    private double totalExpense;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategorySpending {
        private String category;
        private double amount;
        private long count;
    }
}
