package com.leilao.leilaoapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFormDTO {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;

    @NotNull(message = "Preço inicial é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal startingPrice;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime startDate;

    @NotNull(message = "Data de encerramento é obrigatória")
    private LocalDateTime endDate;

    /** Múltiplas fotos — pode vir vazio ou null se nenhuma for enviada. */
    private List<MultipartFile> images;
}