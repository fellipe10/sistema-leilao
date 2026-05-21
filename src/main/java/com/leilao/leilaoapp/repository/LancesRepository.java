package com.leilao.leilaoapp.repository;

import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.Lances;
import com.leilao.leilaoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LancesRepository extends JpaRepository<Lances, Long> {
    
    List<Lances> findByItemOrderByTimestampDesc(Item item);
    
    Optional<Lances> findTopByItemOrderByAmountDesc(Item item);
    
    List<Lances> findByUserOrderByTimestampDesc(User user);

    List<Lances> findTop2ByItemOrderByTimestampDesc(Item item);
}