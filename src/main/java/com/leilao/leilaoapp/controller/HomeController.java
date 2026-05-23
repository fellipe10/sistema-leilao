package com.leilao.leilaoapp.controller;

import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.Lances;
import com.leilao.leilaoapp.repository.LancesRepository;
import com.leilao.leilaoapp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ItemService itemService;
    private final LancesRepository lancesRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Busca ativos e pendentes; encerra no banco qualquer ATIVO com prazo vencido
        List<Item> items = itemService.findAllActiveAndPending().stream()
                .map(item -> {
                    if (item.getStatus() == com.leilao.leilaoapp.entity.enums.ItemStatus.ATIVO) {
                        return itemService.encerrarSeVencido(item);
                    }
                    return item;
                })
                // Remove os que acabaram de ser encerrados (podem ter virado ENCERRADO)
                .filter(item -> item.getStatus() != com.leilao.leilaoapp.entity.enums.ItemStatus.ENCERRADO)
                .collect(java.util.stream.Collectors.toList());

        // Para cada item ATIVO, busca os 2 lances mais recentes e extrai o penúltimo
        Map<Long, BigDecimal> penultimosPorItem = new HashMap<>();
        for (Item item : items) {
            List<Lances> top2 = lancesRepository.findTop2ByItemOrderByTimestampDesc(item);
            if (top2.size() == 2) {
                penultimosPorItem.put(item.getId(), top2.get(1).getAmount());
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("penultimosPorItem", penultimosPorItem);
        return "home";
    }
}
