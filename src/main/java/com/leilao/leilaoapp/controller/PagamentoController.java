package com.leilao.leilaoapp.controller;

import com.leilao.leilaoapp.dto.PagamentoFormDTO;
import com.leilao.leilaoapp.entity.Pagamento;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.exception.LancesException;
import com.leilao.leilaoapp.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pagamento")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    /** Exibe a página de checkout para o vencedor */
    @GetMapping("/{itemId}")
    public String checkout(@PathVariable Long itemId,
                           @AuthenticationPrincipal User user,
                           Model model) {
        try {
            Pagamento pagamento = pagamentoService.buscarOuCriarPagamento(itemId, user);
            model.addAttribute("pagamento", pagamento);
            model.addAttribute("form", new PagamentoFormDTO());
            return "pagamento/checkout";
        } catch (LancesException e) {
            return "redirect:/items/" + itemId;
        }
    }

    /** Processa o pagamento */
    @PostMapping("/{itemId}")
    public String processar(@PathVariable Long itemId,
                            @ModelAttribute("form") PagamentoFormDTO form,
                            @AuthenticationPrincipal User user,
                            RedirectAttributes redirectAttrs) {
        try {
            Pagamento pagamento = pagamentoService.buscarOuCriarPagamento(itemId, user);
            pagamentoService.processarPagamento(pagamento.getId(), form, user);
            redirectAttrs.addFlashAttribute("sucessoPagamento",
                    "✅ Pagamento confirmado com sucesso!");
            return "redirect:/items/" + itemId;
        } catch (LancesException e) {
            redirectAttrs.addFlashAttribute("erroPagamento", e.getMessage());
            return "redirect:/pagamento/" + itemId;
        }
    }
}
