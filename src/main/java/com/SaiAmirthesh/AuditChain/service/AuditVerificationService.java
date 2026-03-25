package com.SaiAmirthesh.AuditChain.service;

import com.SaiAmirthesh.AuditChain.entity.Alert;
import com.SaiAmirthesh.AuditChain.entity.AuditLog;
import com.SaiAmirthesh.AuditChain.repository.AlertRepository;
import com.SaiAmirthesh.AuditChain.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.SaiAmirthesh.AuditChain.util.HashUtil;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuditVerificationService {
    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AlertRepository alertRepository;

    public List<String> verifyChain() {

        List<AuditLog> logs = auditRepository.findAllByOrderByIdAsc();

        String runningHash = "0";
        List<String> result = new ArrayList<>();
        
        long expectedNextId = -1;

        for (AuditLog log : logs) {
            
            // Check for missing rows
            if (expectedNextId != -1 && log.getId() != expectedNextId) {
                createAlert("ROW DELETED: missing audit log between " + (expectedNextId - 1) + " and " + log.getId());
                result.add("ROW DELETED before id " + log.getId());
            }
            expectedNextId = log.getId() + 1;


            String balanceStr = "";
            if (log.getNewData() != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(log.getNewData());
                    if (node.has("balance")) {
                        balanceStr = node.get("balance").asText();
                    }
                } catch (Exception e) {}
            }
            
            if (balanceStr.isEmpty()) {
                balanceStr = log.getNewData() != null ? log.getNewData() : "";
            }

            String tableName = log.getTableName() != null ? log.getTableName() : "";
            String operation = log.getOperation() != null ? log.getOperation() : "";
            String recordIdStr = log.getRecordId() != null ? String.valueOf(log.getRecordId()) : "";
            String oldData = log.getOldData() != null ? log.getOldData() : "";
            String newData = log.getNewData() != null ? log.getNewData() : "";
            String changedBy = log.getChangedBy() != null ? log.getChangedBy() : "";
            String changedAtStr = log.getChangedAt() != null ? log.getChangedAt().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString() : "";
            String prevHashStr = (log.getPrevHash() == null || log.getPrevHash().equalsIgnoreCase("null")) ? "" : log.getPrevHash();

            String dataToHash = tableName + operation + recordIdStr + oldData + newData + changedBy + changedAtStr + prevHashStr;
            String computedHash = HashUtil.sha256(dataToHash);

            System.out.println("--- AUDIT ID " + log.getId() + " ---");
            System.out.println("Data String: [" + dataToHash + "]");
            System.out.println("Computed Hash: " + computedHash);
            System.out.println("Stored Hash:   " + log.getRowHash());

            if (!computedHash.equals(log.getRowHash())) {
                createAlert("ROW MODIFIED: hash mismatch at audit id " + log.getId());
                result.add("ROW MODIFIED at id " + log.getId());
            }

            if (log.getPrevHash() != null && !log.getPrevHash().equalsIgnoreCase("null") && !log.getPrevHash().equals(runningHash)) {
                createAlert("CHAIN BROKEN: prev_hash mismatch at audit id " + log.getId());
                result.add("CHAIN BROKEN at id " + log.getId());
            }

            runningHash = log.getRowHash();
            if (result.isEmpty() || result.get(result.size() - 1).contains("intact")) {
               result.add("row " + log.getId() + " intact");
            }
        }

        if (result.isEmpty() || result.stream().allMatch(s -> s.contains("intact"))) {
            result.clear();
            result.add("CHAIN INTACT: All rows verified successfully");
        }
        return result;
    }
    
    private void createAlert(String message) {
        Alert alert = new Alert();
        alert.setAlertMessage(message);
        alert.setStatus("open");
        alert.setVisibleTo("auditor");
        alertRepository.save(alert);
    }
}
