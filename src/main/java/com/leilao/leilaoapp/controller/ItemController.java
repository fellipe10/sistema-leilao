package com.leilao.leilaoapp.controller;

import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.Lances;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.repository.LancesRepository;
import com.leilao.leilaoapp.repository.PagamentoRepository;
import com.leilao.leilaoapp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final LancesRepository lancesRepository;
    private final PagamentoRepository pagamentoRepository;

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         @AuthenticationPrincipal User currentUser) {

        Item item = itemService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item não encontrado"));

        // Encerra automaticamente se o prazo já venceu
        item = itemService.encerrarSeVencido(item);

        List<Lances> bids = lancesRepository.findByItemOrderByTimestampDesc(item);

        // Lance penúltimo: índice 1 da lista (ordenada por mais recente)
        if (bids.size() >= 2) {
            model.addAttribute("penultimoLance", bids.get(1));
        }

        // Pagamento existente (para exibir status/botão)
        pagamentoRepository.findByItem(item).ifPresent(p ->
                model.addAttribute("pagamento", p));

        model.addAttribute("item", item);
        model.addAttribute("bids", bids);
        model.addAttribute("currentUser", currentUser);

        return "item/detail";
    }
}
