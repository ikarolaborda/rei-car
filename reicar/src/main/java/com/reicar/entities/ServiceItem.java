package com.reicar.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "service_items")
public class ServiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;
    private String description;
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    private ServiceOrder serviceOrder;
}
