package com.SaiAmirthesh.AuditChain.controller;

import com.SaiAmirthesh.AuditChain.entity.Account;
import com.SaiAmirthesh.AuditChain.entity.Transaction;
import com.SaiAmirthesh.AuditChain.repository.AccountRepository;
import com.SaiAmirthesh.AuditChain.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.SaiAmirthesh.AuditChain.service.AnalyticsService;
import com.SaiAmirthesh.AuditChain.service.AIAgentService;
import com.SaiAmirthesh.AuditChain.dto.AdminAnalyticsDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private com.SaiAmirthesh.AuditChain.repository.AuditRepository auditRepository;

    @Autowired
    private com.SaiAmirthesh.AuditChain.repository.AlertRepository alertRepository;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private AIAgentService aiAgentService;

    @org.springframework.web.bind.annotation.DeleteMapping("/reset-all")
    public String resetAll() {
        auditRepository.deleteAll();
        alertRepository.deleteAll();
        return "SUCCESS: All AuditLogs and Alerts have been purged. Database state is clean.";
    }

    @GetMapping("/accounts")
    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    @GetMapping("/transactions")
    public Page<Transaction> getTransactions(@RequestParam(defaultValue = "0") int page, 
                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findAllByOrderByTimestampDesc(pageable);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalAccounts", accountRepository.count());
        dashboard.put("totalTransactions", transactionRepository.count());
        
        Page<Transaction> recentTransactions = transactionRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 10));
        dashboard.put("recentTransactions", recentTransactions.getContent());
        
        return dashboard;
    }

    @org.springframework.web.bind.annotation.PostMapping("/tamper")
    public String tamperDatabase() {
        List<com.SaiAmirthesh.AuditChain.entity.AuditLog> logs = auditRepository.findAllByOrderByIdAsc();
        if (!logs.isEmpty()) {
            com.SaiAmirthesh.AuditChain.entity.AuditLog lastLog = logs.get(logs.size() - 1);
            lastLog.setRowHash("tampered_malicious_hash_detected");
            auditRepository.save(lastLog);
            return "SUCCESS: Database successfully tampered!";
        }
        return "ERROR: No logs exist to tamper";
    }

    @GetMapping("/logs")
    public Page<com.SaiAmirthesh.AuditChain.entity.AuditLog> getLogs(@RequestParam(defaultValue = "0") int page, 
                                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditRepository.findAllByOrderByIdAsc(pageable);
    }

    @org.springframework.web.bind.annotation.PutMapping("/tamper/{id}")
    public com.SaiAmirthesh.AuditChain.entity.AuditLog tamperSpecificLog(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody Map<String, String> payload) {
        com.SaiAmirthesh.AuditChain.entity.AuditLog log = auditRepository.findById(id).orElseThrow(() -> new RuntimeException("Log not found"));
        // Maliciously change data without updating the hash chain
        if (payload.containsKey("newData")) {
            log.setNewData(payload.get("newData"));
        }
        return auditRepository.save(log);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/tamper/{id}")
    public String deleteSpecificLog(@org.springframework.web.bind.annotation.PathVariable Long id) {
        auditRepository.deleteById(id);
        return "SUCCESS: Log maliciously deleted";
    }

    @GetMapping("/ai-intelligence")
    public String getAiIntelligence() {
        AdminAnalyticsDTO data = analyticsService.getAdminAnalytics();
        return aiAgentService.getAdminIntelligence(data);
    }

    @PostMapping("/maintenance")
    public String runMaintenance() {
        analyticsService.runSystemMaintenance();
        return "SUCCESS: System-wide anomaly detection and risk scoring recalibrated.";
    }
}
