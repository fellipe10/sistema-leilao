package com.leilao.leilaoapp.service;

import com.leilao.leilaoapp.dto.ItemFormDTO;
import com.leilao.leilaoapp.entity.Item;
import com.leilao.leilaoapp.entity.ItemImage;
import com.leilao.leilaoapp.entity.Lances;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.entity.enums.ItemStatus;
import com.leilao.leilaoapp.exception.LancesException;
import com.leilao.leilaoapp.repository.ItemImageRepository;
import com.leilao.leilaoapp.repository.ItemRepository;
import com.leilao.leilaoapp.repository.LancesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final LancesRepository lancesRepository;
    private final ItemImageRepository itemImageRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ── Consultas ────────────────────────────────────────────────

    public List<Item> findAllActive() {
        return itemRepository.findByStatusOrderByEndDateAsc(ItemStatus.ATIVO);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    // ── Encerramento automático ──────────────────────────────────

    @Transactional
    public Item encerrarSeVencido(Item item) {
        if (item.getStatus() == ItemStatus.ATIVO
                && LocalDateTime.now().isAfter(item.getEndDate())) {

            item.setStatus(ItemStatus.ENCERRADO);

            List<Lances> lances = lancesRepository.findByItemOrderByTimestampDesc(item);
            if (!lances.isEmpty()) {
                item.setWinner(lances.get(0).getUser());
            }

            item = itemRepository.save(item);
        }
        return item;
    }

    // ── Validação de datas ───────────────────────────────────────

    private void validarDatas(ItemFormDTO dto) {
        if (dto.getEndDate() == null || dto.getStartDate() == null) return;
        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new LancesException("A data de encerramento deve ser posterior à data de início.");
        }
        if (dto.getEndDate().isBefore(LocalDateTime.now())) {
            throw new LancesException("A data de encerramento não pode estar no passado.");
        }
    }

    // ── Criação ──────────────────────────────────────────────────

    @Transactional
    public Item create(ItemFormDTO dto, User admin) {
        validarDatas(dto);

        ItemStatus status = dto.getStartDate().isBefore(LocalDateTime.now()) ||
                            dto.getStartDate().isEqual(LocalDateTime.now())
                            ? ItemStatus.ATIVO
                            : ItemStatus.PENDENTE;

        Item item = Item.builder()
                        .title(dto.getTitle())
                        .description(dto.getDescription())
                        .startingPrice(dto.getStartingPrice())
                        .currentPrice(dto.getStartingPrice())
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .status(status)
                        .createdBy(admin)
                        .build();

        item = itemRepository.save(item);

        // Salva as imagens enviadas
        List<ItemImage> salvas = salvarImagens(dto.getImages(), item);
        if (!salvas.isEmpty()) {
            item.setImageUrl(salvas.get(0).getUrl()); // primeira imagem como capa
            item = itemRepository.save(item);
        }

        return item;
    }

    // ── Atualização ──────────────────────────────────────────────

    @Transactional
    public Item update(Long id, ItemFormDTO dto) {
        validarDatas(dto);
        Item item = itemRepository.findById(id)
                                  .orElseThrow(() -> new LancesException("Item não encontrado: " + id));

        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setStartingPrice(dto.getStartingPrice());
        item.setStartDate(dto.getStartDate());
        item.setEndDate(dto.getEndDate());

        item = itemRepository.save(item);

        // Acrescenta novas imagens (sem remover as existentes)
        List<ItemImage> novas = salvarImagens(dto.getImages(), item);

        // Garante que imageUrl aponta para a primeira imagem disponível
        List<ItemImage> todas = itemImageRepository.findByItemOrderByPosicaoAsc(item);
        if (!todas.isEmpty()) {
            item.setImageUrl(todas.get(0).getUrl());
        } else if (!novas.isEmpty()) {
            item.setImageUrl(novas.get(0).getUrl());
        }

        return itemRepository.save(item);
    }

    // ── Remoção de imagem individual ─────────────────────────────

    @Transactional
    public void removeImage(Long itemId, Long imageId) {
        ItemImage img = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new LancesException("Imagem não encontrada: " + imageId));

        if (!img.getItem().getId().equals(itemId)) {
            throw new LancesException("Imagem não pertence a este lote.");
        }

        itemImageRepository.delete(img);

        // Atualiza imageUrl do item para a nova primeira imagem
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new LancesException("Item não encontrado: " + itemId));
        List<ItemImage> restantes = itemImageRepository.findByItemOrderByPosicaoAsc(item);
        item.setImageUrl(restantes.isEmpty() ? null : restantes.get(0).getUrl());
        itemRepository.save(item);
    }

    // ── Remoção completa do lote ─────────────────────────────────

    public void delete(Long id) {
        Item item = itemRepository.findById(id)
                                  .orElseThrow(() -> new LancesException("Item não encontrado: " + id));
        itemRepository.delete(item);
    }

    // ── Utilitários privados ─────────────────────────────────────

    /**
     * Persiste uma lista de MultipartFiles e retorna as entidades ItemImage criadas.
     * Ignora entradas nulas / vazias.
     */
    private List<ItemImage> salvarImagens(List<MultipartFile> files, Item item) {
        if (files == null || files.isEmpty()) return Collections.emptyList();

        // Descobre a maior posição já existente para este item
        List<ItemImage> existentes = itemImageRepository.findByItemOrderByPosicaoAsc(item);
        int proximaPosicao = existentes.isEmpty() ? 0 : existentes.get(existentes.size() - 1).getPosicao() + 1;

        List<ItemImage> salvas = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String url = saveImage(file);
            ItemImage img = ItemImage.builder()
                                     .item(item)
                                     .url(url)
                                     .posicao(proximaPosicao++)
                                     .build();
            salvas.add(itemImageRepository.save(img));
        }
        return salvas;
    }

    private String saveImage(MultipartFile file) {
        String contentType = file.getContentType();
        List<String> allowed = List.of("image/jpeg", "image/png", "image/webp", "image/avif");
        if (contentType == null || !allowed.contains(contentType)) {
            throw new LancesException("Formato de imagem inválido. Use JPG, PNG, WebP ou AVIF.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                           ? originalFilename.substring(originalFilename.lastIndexOf("."))
                           : ".jpg";

        String filename = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(filename),
                       StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Erro ao salvar imagem: {}", e.getMessage());
            throw new LancesException("Erro ao salvar imagem: " + e.getMessage());
        }

        return "/uploads/" + filename;
    }
}
