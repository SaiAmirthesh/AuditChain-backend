package com.SaiAmirthesh.AuditChain.dto;

import lombok.Data;

@Data
public class TransferRequest {
    private String fromAccount;
    private String toAccount;
    private Double amount;
}
