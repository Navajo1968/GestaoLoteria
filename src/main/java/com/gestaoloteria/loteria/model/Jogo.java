package com.gestaoloteria.loteria.model;

public class Jogo {
    private Integer id;
    private Integer loteriaId;
    private Integer concursoId;
    private String numeros;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getLoteriaId() {
        return loteriaId;
    }
    public void setLoteriaId(Integer loteriaId) {
        this.loteriaId = loteriaId;
    }
    public Integer getConcursoId() {
        return concursoId;
    }
    public void setConcursoId(Integer concursoId) {
        this.concursoId = concursoId;
    }
    public String getNumeros() {
        return numeros;
    }
    public void setNumeros(String numeros) {
        this.numeros = numeros;
    }
}