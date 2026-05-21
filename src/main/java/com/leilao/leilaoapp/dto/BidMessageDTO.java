package com.leilao.leilaoapp.dto;

import java.math.BigDecimal;

public class BidMessageDTO {

    private Long itemId;
    private BigDecimal amount;

    public BidMessageDTO() {}

    public BidMessageDTO(Long itemId, BigDecimal amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
