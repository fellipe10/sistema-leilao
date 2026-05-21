package com.leilao.leilaoapp.service;

import com.leilao.leilaoapp.dto.BidResponseDTO;
import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.Lances;
import com.leilao.leilaoapp.entity.enums.ItemStatus;
import com.leilao.leilaoapp.repository.ItemRepository;
import com.leilao.leilaoapp.repository.LancesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final ItemRepository itemRepository;
    private final LancesRepository lancesRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * A cada 30s: encerra leilões ATIVO cuja endDate já passou.
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void closeExpiredAuctions() {
        List<Item> expired = itemRepository
                .findByStatusAndEndDateBefore(ItemStatus.ATIVO, LocalDateTime.now());

        for (Item item : expired) {
            item.setStatus(ItemStatus.ENCERRADO);

            Optional<Lances> topBid = lancesRepository.findTopByItemOrderByAmountDesc(item);

            // Exibe código anônimo do vencedor (nunca o nome real)
            String codigoVencedor = "Sem lances";
            if (topBid.isPresent()) {
                item.setWinner(topBid.get().getUser());
                codigoVencedor = "#" + topBid.get().getUser().getCodigoBidder();
            }

            itemRepository.save(item);

            BidResponseDTO notification = BidResponseDTO.closed(codigoVencedor);
            messagingTemplate.convertAndSend("/topic/auction/" + item.getId(), notification);

            log.info("Leilão encerrado — item id={} título='{}' vencedor='{}'",
                    item.getId(), item.getTitle(), codigoVencedor);
        }
    }

    /**
     * A cada 60s: ativa leilões PENDENTE cuja startDate já chegou.
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void activatePendingAuctions() {
        List<Item> toActivate = itemRepository
                .findByStatusAndStartDateBefore(ItemStatus.PENDENTE, LocalDateTime.now());

        for (Item item : toActivate) {
            item.setStatus(ItemStatus.ATIVO);
            itemRepository.save(item);
            log.info("Leilão ativado — item id={} título='{}'", item.getId(), item.getTitle());
        }
    }
}
