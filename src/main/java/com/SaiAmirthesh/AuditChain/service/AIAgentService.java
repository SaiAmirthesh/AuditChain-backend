package com.SaiAmirthesh.AuditChain.service;

import com.SaiAmirthesh.AuditChain.dto.AdminAnalyticsDTO;
import com.SaiAmirthesh.AuditChain.dto.UserAnalyticsDTO;
import com.SaiAmirthesh.AuditChain.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIAgentService {

    @Autowired
    private GeminiService geminiService;

    public String getUserInsights(UserAnalyticsDTO data) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a friendly Personal Financial Advisor AI. Analyze the following user data and provide a single, neat, easily understandable paragraph of advice. ")
          .append("IMPORTANT: Do NOT use any markdown formatting, bolding with stars (**), bullet points, or brackets []. ")
          .append("Just output plain text in one cohesive paragraph. Be encouraging but firm on savings. Keep it under 100 words.\\n\\n");
        sb.append("Current Income: ₹").append(data.getTotalIncome()).append("\\n");
        sb.append("Current Expenses: ₹").append(data.getTotalExpense()).append("\\n");
        sb.append("Spending by Category:\\n");
        for (UserAnalyticsDTO.CategorySpending cs : data.getCategorySpending()) {
            sb.append("- ").append(cs.getCategory()).append(": ₹").append(cs.getAmount()).append("\\n");
        }
        
        return geminiService.generateContent(sb.toString());
    }

    public String getAdminIntelligence(AdminAnalyticsDTO data) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a System Intelligence AI for a Banking Platform. Analyze these system-wide metrics and provide a single, professional paragraph of health report. ")
          .append("IMPORTANT: Do NOT use any markdown formatting, bolding with stars (**), bullet points, or brackets []. ")
          .append("Just output plain text in one cohesive paragraph. Identify any trends or bottlenecks. Keep it professional.\\n\\n");
        sb.append("Total Transaction Volume: ₹").append(data.getTotalVolume()).append("\\n");
        sb.append("Total Transaction Count: ").append(data.getTotalCount()).append("\\n");
        sb.append("Average Transaction Value: ₹").append(data.getAvgTxValue()).append("\\n");
        
        sb.append("Status Distribution:\\n");
        for (AdminAnalyticsDTO.StatusCount sc : data.getStatusDistribution()) {
            sb.append("- ").append(sc.getStatus()).append(": ").append(sc.getCount()).append("\\n");
        }

        sb.append("Channel Usage:\\n");
        for (AdminAnalyticsDTO.ChannelCount cc : data.getChannelUsage()) {
            sb.append("- ").append(cc.getChannel()).append(": ").append(cc.getCount()).append("\\n");
        }

        return geminiService.generateContent(sb.toString());
    }

    public String getAuditorVerdict(List<Transaction> flaggedTxs) {
        if (flaggedTxs.isEmpty()) {
            return "No suspicious activity detected in the recent block sequence. System integrity is 100%.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("You are a Senior Forensic Auditor AI. Analyze these FLAGGED suspicious transactions and provide a single, authoritative paragraph giving your forensic verdict. ")
          .append("IMPORTANT: Do NOT use any markdown formatting, bolding with stars (**), bullet points, or brackets []. ")
          .append("Just output plain text in one cohesive paragraph. Focus on the risk factors. Be authoritative.\\n\\n");
        
        for (Transaction tx : flaggedTxs) {
            sb.append("- ID: TX-").append(tx.getId())
              .append(" | Reason: ").append(tx.getFlagReason())
              .append(" | Amount: ₹").append(tx.getAmount())
              .append(" | From: ").append(tx.getFromAccount())
              .append(" | Risk Score: ").append(tx.getRiskScore()).append("\\n");
        }

        return geminiService.generateContent(sb.toString());
    }
}
