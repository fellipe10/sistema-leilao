package com.leilao.leilaoapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    private Item item;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "posicao")
    private Integer posicao;
}
