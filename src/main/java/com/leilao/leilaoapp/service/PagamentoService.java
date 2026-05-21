package com.leilao.leilaoapp.service;

import com.leilao.leilaoapp.dto.PagamentoFormDTO;
import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.Pagamento;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.entity.enums.PagamentoMetodo;
import com.leilao.leilaoapp.entity.enums.PagamentoStatus;
import com.leilao.leilaoapp.exception.LancesException;
import com.leilao.leilaoapp.repository.ItemRepository;
import com.leilao.leilaoapp.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final ItemRepository itemRepository;

    /**
     * Busca ou cria um pagamento pendente para o item.
     * Apenas o vencedor pode iniciar o pagamento.
     */
    @Transactional
    public Pagamento buscarOuCriarPagamento(Long itemId, User user) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new LancesException("Item não encontrado."));

        if (item.getWinner() == null || !item.getWinner().getId().equals(user.getId())) {
            throw new LancesException("Apenas o vencedor pode realizar o pagamento.");
        }

        Optional<Pagamento> existente = pagamentoRepository.findByItem(item);
        if (existente.isPresent()) {
            return existente.get();
        }

        Pagamento pagamento = Pagamento.builder()
                .item(item)
                .user(user)
                .valor(item.getCurrentPrice())
                .status(PagamentoStatus.PENDENTE)
                .build();

        return pagamentoRepository.save(pagamento);
    }

    /**
     * Processa (simula) o pagamento e marca como PAGO.
     */
    @Transactional
    public Pagamento processarPagamento(Long pagamentoId, PagamentoFormDTO form, User user) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new LancesException("Pagamento não encontrado."));

        if (!pagamento.getUser().getId().equals(user.getId())) {
            throw new LancesException("Acesso negado.");
        }

        if (pagamento.getStatus() == PagamentoStatus.PAGO) {
            throw new LancesException("Este pagamento já foi realizado.");
        }

        PagamentoMetodo metodo = PagamentoMetodo.valueOf(form.getMetodo());

        // Validação básica para cartão
        if (metodo == PagamentoMetodo.CARTAO) {
            if (form.getNumeroCartao() == null || form.getNumeroCartao().replaceAll("\\s", "").length() < 16) {
                throw new LancesException("Número do cartão inválido.");
            }
            if (form.getCvv() == null || form.getCvv().trim().length() < 3) {
                throw new LancesException("CVV inválido.");
            }
            if (form.getValidadeCartao() == null || form.getValidadeCartao().trim().isEmpty()) {
                throw new LancesException("Data de validade inválida.");
            }
        }

        // Simula aprovação
        pagamento.setMetodo(metodo);
        pagamento.setStatus(PagamentoStatus.PAGO);
        pagamento.setPagoEm(LocalDateTime.now());

        return pagamentoRepository.save(pagamento);
    }
}
