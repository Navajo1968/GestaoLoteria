package com.gestaoloteria.loteria.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Concurso {
    private Integer id;
    private Integer loteriaId;
    private Integer numero;
    private LocalDate data;

    private BigDecimal arrecadacaoTotal;
    private Boolean acumulado;
    private BigDecimal valorAcumulado;
    private BigDecimal estimativaPremio;
    private BigDecimal acumuladoEspecial;
    private String observacao;
    private String timeCoracao;

    // Não precisa de dezenasSorteadas ou métodos get/set relacionados

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getLoteriaId() { return loteriaId; }
    public void setLoteriaId(Integer loteriaId) { this.loteriaId = loteriaId; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public BigDecimal getArrecadacaoTotal() { return arrecadacaoTotal; }
    public void setArrecadacaoTotal(BigDecimal arrecadacaoTotal) { this.arrecadacaoTotal = arrecadacaoTotal; }

    public Boolean getAcumulado() { return acumulado; }
    public void setAcumulado(Boolean acumulado) { this.acumulado = acumulado; }

    public BigDecimal getValorAcumulado() { return valorAcumulado; }
    public void setValorAcumulado(BigDecimal valorAcumulado) { this.valorAcumulado = valorAcumulado; }

    public BigDecimal getEstimativaPremio() { return estimativaPremio; }
    public void setEstimativaPremio(BigDecimal estimativaPremio) { this.estimativaPremio = estimativaPremio; }

    public BigDecimal getAcumuladoEspecial() { return acumuladoEspecial; }
    public void setAcumuladoEspecial(BigDecimal acumuladoEspecial) { this.acumuladoEspecial = acumuladoEspecial; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public String getTimeCoracao() { return timeCoracao; }
    public void setTimeCoracao(String timeCoracao) { this.timeCoracao = timeCoracao; }
}