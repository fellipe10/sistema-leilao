package com.leilao.leilaoapp.dto;

public class PagamentoFormDTO {

    // "PIX" ou "CARTAO"
    private String metodo;

    // Campos de cartão (opcionais para PIX)
    private String nomeCartao;
    private String numeroCartao;
    private String validadeCartao;
    private String cvv;

    public PagamentoFormDTO() {}

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public String getNomeCartao() { return nomeCartao; }
    public void setNomeCartao(String nomeCartao) { this.nomeCartao = nomeCartao; }

    public String getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(String numeroCartao) { this.numeroCartao = numeroCartao; }

    public String getValidadeCartao() { return validadeCartao; }
    public void setValidadeCartao(String validadeCartao) { this.validadeCartao = validadeCartao; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}
