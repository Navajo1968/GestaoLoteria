package com.gestaoloteria.loteria.model;

public class FaixaPremiacao {
    private Integer id;
    private String nome;
    private String descricao;
    private int ordem;
    private int acertos;

    public FaixaPremiacao() {}

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getOrdem() {
        return ordem;
    }
    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public int getAcertos() {
        return acertos;
    }
    public void setAcertos(int acertos) {
        this.acertos = acertos;
    }
}