package com.leilao.leilaoapp.service;

import com.leilao.leilaoapp.dto.BidMessageDTO;
import com.leilao.leilaoapp.dto.BidResponseDTO;
import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.Lances;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.entity.enums.ItemStatus;
import com.leilao.leilaoapp.exception.LancesException;
import com.leilao.leilaoapp.repository.ItemRepository;
import com.leilao.leilaoapp.repository.LancesRepository;
import com.leilao.leilaoapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BidService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final LancesRepository lancesRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BidService(LancesRepository lancesRepository,
                      ItemRepository itemRepository,
                      UserRepository userRepository) {
        this.lancesRepository = lancesRepository;
        this.itemRepository   = itemRepository;
        this.userRepository   = userRepository;
    }

    /** Incremento mínimo obrigatório por lance (R$ 1,00) */
    private static final java.math.BigDecimal INCREMENTO_MINIMO =
            new java.math.BigDecimal("1.00");

    @Transactional
    public BidResponseDTO placeBid(BidMessageDTO message, String email) {

        // Carrega a entidade gerenciada pelo e-mail recebido do Principal do WebSocket
        User managedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new LancesException("Usuário não encontrado."));

        // 1. Item existe?
        Item item = itemRepository.findById(message.getItemId())
                .orElseThrow(() -> new LancesException("Item não encontrado."));

        // 2. Item está ATIVO?
        if (item.getStatus() != ItemStatus.ATIVO) {
            throw new LancesException("Este leilão não está ativo.");
        }

        // 3. Data de encerramento ainda não passou?
        if (LocalDateTime.now().isAfter(item.getEndDate())) {
            throw new LancesException("O prazo deste leilão já foi encerrado.");
        }

        // 4. Valor do lance deve superar o preço atual pelo incremento mínimo
        java.math.BigDecimal minimoAceito = item.getCurrentPrice().add(INCREMENTO_MINIMO);
        if (message.getAmount().compareTo(minimoAceito) < 0) {
            throw new LancesException(
                    "O lance mínimo é R$ " + minimoAceito.toPlainString() +
                    " (incremento mínimo de R$ " + INCREMENTO_MINIMO.toPlainString() + ").");
        }

        // Lance válido: salvar e atualizar preço atual
        Lances lance = Lances.builder()
                .amount(message.getAmount())
                .user(managedUser)
                .item(item)
                .build();

        lancesRepository.save(lance);

        item.setCurrentPrice(message.getAmount());
        itemRepository.save(item);

        // Montar e retornar o DTO de resposta
        // bidderName exibe o código anônimo do licitante em vez do nome real
        String codigoExibido = "#" + managedUser.getCodigoBidder();
        BidResponseDTO response = new BidResponseDTO(
                codigoExibido,
                message.getAmount(),
                item.getCurrentPrice(),
                LocalDateTime.now().format(FORMATTER),
                true
        );
        response.setClosed(false);

        return response;
    }
}
