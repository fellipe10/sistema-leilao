package com.leilao.leilaoapp.repository;

import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
    List<ItemImage> findByItemOrderByPosicaoAsc(Item item);
}
