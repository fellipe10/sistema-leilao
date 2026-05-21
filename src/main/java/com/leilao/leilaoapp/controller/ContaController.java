package com.leilao.leilaoapp.controller;

import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.repository.LancesRepository;
import com.leilao.leilaoapp.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/minha-conta")
@RequiredArgsConstructor
public class ContaController {

    private final LancesRepository lancesRepository;
    private final PagamentoRepository pagamentoRepository;

    @GetMapping
    public String minhaConta(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("currentUser", user);
        model.addAttribute("lances", lancesRepository.findByUserOrderByTimestampDesc(user));
        model.addAttribute("pagamentos", pagamentoRepository.findByUserOrderByCriadoEmDesc(user));
        return "conta/minha-conta";
    }
}
