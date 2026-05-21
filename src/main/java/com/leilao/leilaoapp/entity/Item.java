package com.leilao.leilaoapp.entity;

import com.leilao.leilaoapp.entity.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "titulo", nullable = false)
    private String title;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "url_imagem")
    private String imageUrl;
    
    @Column(name = "preco_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal startingPrice;
    
    @Column(name = "preco_atual", precision = 15, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "data_inicio")
    private LocalDateTime startDate;
    
    @Column(name = "data_encerramento")
    private LocalDateTime endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ItemStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vencedor_id", nullable = true)
    private User winner;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("posicao ASC")
    @Builder.Default
    private List<ItemImage> images = new ArrayList<>();
}