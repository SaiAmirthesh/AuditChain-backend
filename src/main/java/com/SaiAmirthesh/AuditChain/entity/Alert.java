package com.SaiAmirthesh.AuditChain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "alerts")
@Data
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String alertMessage;
    private String status;
    private String visibleTo;
}
