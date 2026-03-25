package com.SaiAmirthesh.AuditChain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "audit_log")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tableName;
    private String operation;
    private Long recordId;
    private String changedBy;
    private java.time.LocalDateTime changedAt;
    private String prevHash;
    private String rowHash;

    @Column(columnDefinition = "json")
    private String oldData;

    @Column(columnDefinition = "json")
    private String newData;

}
