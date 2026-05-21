package com.leilao.leilaoapp.controller;

import com.leilao.leilaoapp.dto.BidMessageDTO;
import com.leilao.leilaoapp.dto.BidResponseDTO;
import com.leilao.leilaoapp.exception.LancesException;
import com.leilao.leilaoapp.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/bid")
    public void handleBid(BidMessageDTO message, Principal principal) {

        // Rejeita se não há usuário autenticado
        if (principal == null) {
            if (message.getItemId() != null) {
                messagingTemplate.convertAndSend(
                        "/topic/auction/" + message.getItemId(),
                        BidResponseDTO.error("Você precisa estar logado para dar um lance."));
            }
            return;
        }

        // Rejeita se o payload estiver malformado
        if (message.getItemId() == null || message.getAmount() == null) {
            messagingTemplate.convertAndSend(
                    "/topic/auction/" + message.getItemId(),
                    BidResponseDTO.error("Dados do lance inválidos."));
            return;
        }

        try {
            // principal.getName() retorna o e-mail do usuário (username do Spring Security)
            BidResponseDTO response = bidService.placeBid(message, principal.getName());
            messagingTemplate.convertAndSend(
                    "/topic/auction/" + message.getItemId(), response);

        } catch (LancesException e) {
            messagingTemplate.convertAndSend(
                    "/topic/auction/" + message.getItemId(),
                    BidResponseDTO.error(e.getMessage()));
        }
    }
}
