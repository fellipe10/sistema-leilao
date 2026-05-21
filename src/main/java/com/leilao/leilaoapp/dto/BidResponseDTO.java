package com.leilao.leilaoapp.dto;

import java.math.BigDecimal;

public class BidResponseDTO {

    private String bidderName;
    private BigDecimal amount;
    private BigDecimal newCurrentPrice;
    private String timestamp;
    private boolean success;
    private String errorMessage;
    private boolean closed;
    private String winnerName;

    public BidResponseDTO() {}

    // Construtor para lance bem-sucedido
    public BidResponseDTO(String bidderName, BigDecimal amount, BigDecimal newCurrentPrice,
                          String timestamp, boolean success) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.newCurrentPrice = newCurrentPrice;
        this.timestamp = timestamp;
        this.success = success;
    }

    // Construtor para erro
    public static BidResponseDTO error(String errorMessage) {
        BidResponseDTO dto = new BidResponseDTO();
        dto.success = false;
        dto.errorMessage = errorMessage;
        return dto;
    }

    // Construtor para leilão encerrado
    public static BidResponseDTO closed(String winnerName) {
        BidResponseDTO dto = new BidResponseDTO();
        dto.success = true;
        dto.closed = true;
        dto.winnerName = winnerName;
        return dto;
    }

    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getNewCurrentPrice() { return newCurrentPrice; }
    public void setNewCurrentPrice(BigDecimal newCurrentPrice) { this.newCurrentPrice = newCurrentPrice; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }

    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
}
