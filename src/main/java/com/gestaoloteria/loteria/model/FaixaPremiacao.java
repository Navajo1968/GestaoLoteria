package com.gestaoloteria.loteria.model;

public class FaixaPremiacao {
    private Integer id;
    private Integer loteriaId;
    private String nome;
    private Integer acertos;
    private Integer ordem;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getLoteriaId() { return loteriaId; }
    public void setLoteriaId(Integer loteriaId) { this.loteriaId = loteriaId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getAcertos() { return acertos; }
    public void setAcertos(Integer acertos) { this.acertos = acertos; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
}