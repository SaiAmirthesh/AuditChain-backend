package com.SaiAmirthesh.AuditChain.service;

import com.SaiAmirthesh.AuditChain.entity.Alert;
import com.SaiAmirthesh.AuditChain.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AlertService {
    @Autowired
    private AlertRepository alertRepository;

    public List<Alert> getAlerts() {
        return alertRepository.findAll();
    }
}
