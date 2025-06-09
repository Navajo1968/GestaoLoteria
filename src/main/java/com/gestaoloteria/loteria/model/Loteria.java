package com.gestaoloteria.loteria.model;

import java.util.List;

public class Loteria {
    private Integer id;
    private String nome;
    private String descricao;
    private Integer qtdMin;
    private Integer qtdMax;
    private Integer qtdSorteados;
    private List<FaixaPremiacao> faixas;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getQtdMin() { return qtdMin; }
    public void setQtdMin(Integer qtdMin) { this.qtdMin = qtdMin; }
    public Integer getQtdMax() { return qtdMax; }
    public void setQtdMax(Integer qtdMax) { this.qtdMax = qtdMax; }
    public Integer getQtdSorteados() { return qtdSorteados; }
    public void setQtdSorteados(Integer qtdSorteados) { this.qtdSorteados = qtdSorteados; }
    public List<FaixaPremiacao> getFaixas() { return faixas; }
    public void setFaixas(List<FaixaPremiacao> faixas) { this.faixas = faixas; }

    @Override
    public String toString() {
        return nome;
    }
}