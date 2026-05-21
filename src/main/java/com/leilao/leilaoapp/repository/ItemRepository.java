package com.leilao.leilaoapp.repository;

import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    
    List<Item> findByStatus(ItemStatus status);
    
    List<Item> findByStatusAndEndDateBefore(ItemStatus status, LocalDateTime dateTime);
    
    List<Item> findByStatusOrderByEndDateAsc(ItemStatus status);

    List<Item> findByStatusAndStartDateBefore(ItemStatus status, LocalDateTime dateTime);
}