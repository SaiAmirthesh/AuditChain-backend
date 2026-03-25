package com.SaiAmirthesh.AuditChain.controller;

import com.SaiAmirthesh.AuditChain.entity.Account;
import com.SaiAmirthesh.AuditChain.entity.Transaction;
import com.SaiAmirthesh.AuditChain.repository.AccountRepository;
import com.SaiAmirthesh.AuditChain.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<Transaction> getTransactions() {
        return transactionRepository.findAll();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalAccounts", accountRepository.count());
        dashboard.put("totalTransactions", transactionRepository.count());
        
        List<Transaction> recentTransactions = transactionRepository.findAll();
        dashboard.put("recentTransactions", 
            recentTransactions.subList(0, Math.min(recentTransactions.size(), 10)));
        
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
    public List<com.SaiAmirthesh.AuditChain.entity.AuditLog> getLogs() {
        return auditRepository.findAllByOrderByIdAsc();
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
}
