package com.SaiAmirthesh.AuditChain.controller;

import com.SaiAmirthesh.AuditChain.entity.Alert;
import com.SaiAmirthesh.AuditChain.entity.AuditLog;
import com.SaiAmirthesh.AuditChain.entity.Transaction;
import com.SaiAmirthesh.AuditChain.repository.AuditRepository;
import com.SaiAmirthesh.AuditChain.repository.TransactionRepository;
import com.SaiAmirthesh.AuditChain.service.AIAgentService;
import com.SaiAmirthesh.AuditChain.service.AlertService;
import com.SaiAmirthesh.AuditChain.service.AuditVerificationService;
import com.SaiAmirthesh.AuditChain.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auditor")
public class AuditorController {
    
    @Autowired
    private AuditVerificationService auditVerificationService;

    @Autowired
    private AlertService alertService;
    
    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AIAgentService aiAgentService;

    @GetMapping("/logs")
    public Page<AuditLog> getLogs(@RequestParam(defaultValue = "0") int page, 
                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditRepository.findAllByOrderByIdAsc(pageable);
    }

    @GetMapping("/transactions")
    public Page<Transaction> getTransactions(@RequestParam(defaultValue = "0") int page, 
                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findAllByOrderByTimestampDesc(pageable);
    }

    @GetMapping("/verify")
    public List<String> verify() {
        return auditVerificationService.verifyChain();
    }

    @GetMapping("/alerts")
    public List<Alert> alerts() {
        return alertService.getAlerts();
    }

    @GetMapping("/ai-summary")
    public String getAiSummary() {
        return geminiService.generateSummary();
    }

    @GetMapping("/ai-verdict")
    public String getAiVerdict() {
        // Fetch flagged transactions for AI analysis
        List<Transaction> flaggedTxs = transactionRepository.findAll().stream()
                .filter(tx -> tx.getFlagged() != null && tx.getFlagged())
                .limit(20) 
                .collect(Collectors.toList());
        
        return aiAgentService.getAuditorVerdict(flaggedTxs);
    }
}
