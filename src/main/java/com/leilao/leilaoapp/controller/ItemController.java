package com.leilao.leilaoapp.controller;

import com.leilao.leilaoapp.dto.BidMessageDTO;
import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.Lances;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.exception.LancesException;
import com.leilao.leilaoapp.repository.LancesRepository;
import com.leilao.leilaoapp.repository.PagamentoRepository;
import com.leilao.leilaoapp.service.BidService;
import com.leilao.leilaoapp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final BidService bidService;
    private final SimpMessagingTemplate messagingTemplate;
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

    /**
     * Fallback HTTP para dar lance — garante funcionamento em mobile
     * e em qualquer navegador mesmo sem WebSocket.
     */
    @PostMapping("/{id}/bid")
    public String placeBid(@PathVariable Long id,
                           @RequestParam BigDecimal amount,
                           @AuthenticationPrincipal User currentUser,
                           RedirectAttributes ra) {
        if (currentUser == null) {
            ra.addFlashAttribute("bidErro", "Você precisa estar logado para dar um lance.");
            return "redirect:/items/" + id;
        }

        try {
            BidMessageDTO msg = new BidMessageDTO(id, amount);
            var response = bidService.placeBid(msg, currentUser.getEmail());

            // Notifica os outros via WebSocket (atualiza tela de quem está assistindo)
            messagingTemplate.convertAndSend("/topic/auction/" + id, response);

            ra.addFlashAttribute("bidSucesso",
                    "Lance de R$ " + amount.toPlainString().replace(".", ",") + " registrado!");
        } catch (LancesException e) {
            ra.addFlashAttribute("bidErro", e.getMessage());
        }

        return "redirect:/items/" + id;
    }
}
