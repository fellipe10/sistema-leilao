package com.leilao.leilaoapp.repository;

import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.Pagamento;
import com.leilao.leilaoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByItem(Item item);

    List<Pagamento> findByUserOrderByCriadoEmDesc(User user);
}
