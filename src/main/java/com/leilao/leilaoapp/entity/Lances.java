package com.leilao.leilaoapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lances {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "data_hora")
    private LocalDateTime timestamp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Item item;
    
    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}