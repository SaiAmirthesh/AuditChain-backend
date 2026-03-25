package com.SaiAmirthesh.AuditChain.controller;

import com.SaiAmirthesh.AuditChain.entity.Alert;
import com.SaiAmirthesh.AuditChain.entity.AuditLog;
import com.SaiAmirthesh.AuditChain.repository.AuditRepository;
import com.SaiAmirthesh.AuditChain.service.AlertService;
import com.SaiAmirthesh.AuditChain.service.AuditVerificationService;
import com.SaiAmirthesh.AuditChain.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/logs")
    public List<AuditLog> getLogs() {
        return auditRepository.findAllByOrderByIdAsc();
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
}
