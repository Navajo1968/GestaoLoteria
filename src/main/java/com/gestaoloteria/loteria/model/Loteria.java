package com.gestaoloteria.loteria.model;

import java.util.ArrayList;
import java.util.List;

public class Loteria {
    private Integer id;
    private String nome;
    private String descricao;
    // Remover: private String tipo;
    private int qtdMin;
    private int qtdMax;
    private int qtdSorteados;
    private List<FaixaPremiacao> faixas = new ArrayList<>();

    public Loteria() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    // Remova getter e setter de tipo

    public int getQtdMin() { return qtdMin; }
    public void setQtdMin(int qtdMin) { this.qtdMin = qtdMin; }

    public int getQtdMax() { return qtdMax; }
    public void setQtdMax(int qtdMax) { this.qtdMax = qtdMax; }

    public int getQtdSorteados() { return qtdSorteados; }
    public void setQtdSorteados(int qtdSorteados) { this.qtdSorteados = qtdSorteados; }

    public List<FaixaPremiacao> getFaixas() { return faixas; }
    public void setFaixas(List<FaixaPremiacao> faixas) {
        this.faixas = (faixas != null) ? faixas : new ArrayList<>();
    }
}