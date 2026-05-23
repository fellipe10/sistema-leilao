package com.leilao.leilaoapp.controller.admin;

import com.leilao.leilaoapp.dto.ItemFormDTO;
import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.entity.enums.ItemStatus;
import com.leilao.leilaoapp.entity.enums.PagamentoStatus;
import com.leilao.leilaoapp.exception.LancesException;
import com.leilao.leilaoapp.repository.LancesRepository;
import com.leilao.leilaoapp.repository.PagamentoRepository;
import com.leilao.leilaoapp.repository.UserRepository;
import com.leilao.leilaoapp.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemService itemService;
    private final LancesRepository lancesRepository;
    private final PagamentoRepository pagamentoRepository;
    private final UserRepository userRepository;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        List<Item> todos = itemService.findAll();
        long ativos    = todos.stream().filter(i -> i.getStatus() == ItemStatus.ATIVO).count();
        long pendentes = todos.stream().filter(i -> i.getStatus() == ItemStatus.PENDENTE).count();
        long encerrados= todos.stream().filter(i -> i.getStatus() == ItemStatus.ENCERRADO).count();
        long totalLances = lancesRepository.count();
        long totalUsuarios = userRepository.count();
        BigDecimal receitaTotal = pagamentoRepository.findAll().stream()
                .filter(p -> p.getStatus() == PagamentoStatus.PAGO)
                .map(p -> p.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalItens",    todos.size());
        model.addAttribute("ativos",        ativos);
        model.addAttribute("pendentes",     pendentes);
        model.addAttribute("encerrados",    encerrados);
        model.addAttribute("totalLances",   totalLances);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("receitaTotal",  receitaTotal);
        model.addAttribute("ultimosItens",  todos.stream().sorted(
                (a, b) -> b.getId().compareTo(a.getId())).limit(5).toList());
        return "admin/dashboard";
    }
    
    @GetMapping("/admin/items")
    public String listItems(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String de,
            @RequestParam(required = false) String ate,
            Model model) {

        List<Item> items = itemService.findAll();

        // Filtro por título
        if (titulo != null && !titulo.isBlank()) {
            String q = titulo.toLowerCase();
            items = items.stream()
                    .filter(i -> i.getTitle().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        // Filtro por status
        if (status != null && !status.isBlank()) {
            try {
                ItemStatus s = ItemStatus.valueOf(status);
                items = items.stream()
                        .filter(i -> i.getStatus() == s)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {}
        }

        // Filtro por período (endDate)
        if (de != null && !de.isBlank()) {
            LocalDateTime inicio = LocalDate.parse(de).atStartOfDay();
            items = items.stream()
                    .filter(i -> !i.getEndDate().isBefore(inicio))
                    .collect(Collectors.toList());
        }
        if (ate != null && !ate.isBlank()) {
            LocalDateTime fim = LocalDate.parse(ate).atTime(23, 59, 59);
            items = items.stream()
                    .filter(i -> !i.getEndDate().isAfter(fim))
                    .collect(Collectors.toList());
        }

        model.addAttribute("items",  items);
        model.addAttribute("titulo", titulo);
        model.addAttribute("status", status);
        model.addAttribute("de",     de);
        model.addAttribute("ate",    ate);
        return "admin/item-list";
    }
    
    @GetMapping("/admin/items/new")
    public String newItemForm(Model model) {
        model.addAttribute("itemFormDTO", new ItemFormDTO());
        model.addAttribute("editando", false);
        return "admin/item-form";
    }
    
    @PostMapping("/admin/items")
    public String createItem(@Valid @ModelAttribute("itemFormDTO") ItemFormDTO dto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal User admin,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editando", false);
            return "admin/item-form";
        }
        
        try {
            itemService.create(dto, admin);
            redirectAttributes.addFlashAttribute("sucesso", "Lote criado com sucesso!");
        } catch (LancesException e) {
            model.addAttribute("erroGlobal", e.getMessage());
            model.addAttribute("editando", false);
            return "admin/item-form";
        }
        
        return "redirect:/admin/items";
    }
    
    @GetMapping("/admin/items/{id}/edit")
    public String editItemForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return itemService.findById(id).map(item -> {
            ItemFormDTO dto = ItemFormDTO.builder()
                                         .title(item.getTitle())
                                         .description(item.getDescription())
                                         .startingPrice(item.getStartingPrice())
                                         .startDate(item.getStartDate())
                                         .endDate(item.getEndDate())
                                         .build();
            model.addAttribute("itemFormDTO", dto);
            model.addAttribute("item", item);
            model.addAttribute("editando", true);
            return "admin/item-form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("erro", "Lote não encontrado.");
            return "redirect:/admin/items";
        });
    }
    
    @PostMapping("/admin/items/{id}")
    public String updateItem(@PathVariable Long id,
                             @Valid @ModelAttribute("itemFormDTO") ItemFormDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            itemService.findById(id).ifPresent(i -> model.addAttribute("item", i));
            model.addAttribute("editando", true);
            return "admin/item-form";
        }
        
        try {
            itemService.update(id, dto);
            redirectAttributes.addFlashAttribute("sucesso", "Lote atualizado com sucesso!");
        } catch (LancesException e) {
            model.addAttribute("erroGlobal", e.getMessage());
            itemService.findById(id).ifPresent(i -> model.addAttribute("item", i));
            model.addAttribute("editando", true);
            return "admin/item-form";
        }
        
        return "redirect:/admin/items";
    }
    
    @PostMapping("/admin/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            itemService.delete(id);
            redirectAttributes.addFlashAttribute("sucesso", "Lote removido com sucesso!");
        } catch (LancesException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/items";
    }

    @PostMapping("/admin/items/{id}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long id,
                              @PathVariable Long imageId,
                              RedirectAttributes redirectAttributes) {
        try {
            itemService.removeImage(id, imageId);
            redirectAttributes.addFlashAttribute("sucesso", "Foto removida com sucesso!");
        } catch (LancesException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/items/" + id + "/edit";
    }

    @PostMapping("/admin/items/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String novoStatus,
                               @RequestParam(required = false, defaultValue = "admin") String origem,
                               RedirectAttributes redirectAttributes) {
        try {
            ItemStatus status = ItemStatus.valueOf(novoStatus);
            itemService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("sucesso", "Status atualizado para " + novoStatus + "!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", "Status inválido: " + novoStatus);
        } catch (LancesException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "detalhe".equals(origem)
                ? "redirect:/items/" + id
                : "redirect:/admin/items/" + id + "/edit";
    }

    @PostMapping("/admin/items/{id}/prorrogar")
    public String prorrogar(@PathVariable Long id,
                            @RequestParam int segundos,
                            @RequestParam(required = false, defaultValue = "admin") String origem,
                            RedirectAttributes redirectAttributes) {
        try {
            itemService.prorrogar(id, segundos);
            redirectAttributes.addFlashAttribute("sucesso", "Lote prorrogado por " + segundos + " segundos!");
        } catch (LancesException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "detalhe".equals(origem)
                ? "redirect:/items/" + id
                : "redirect:/admin/items/" + id + "/edit";
    }
}