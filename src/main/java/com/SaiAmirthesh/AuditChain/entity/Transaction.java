package com.SaiAmirthesh.AuditChain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromAccount;
    private String toAccount;
    private Double amount;

    @Column(name = "transaction_type")
    private String transactionType = "transfer";

    private String status = "completed";

    private String description;

    private String category = "general";

    private String channel = "web";

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_info")
    private String deviceInfo;

    private String location;

    @Column(name = "risk_score")
    private Double riskScore = 0.0;

    private Boolean flagged = false;

    @Column(name = "flag_reason")
    private String flagReason;

    private LocalDateTime timestamp;
}
