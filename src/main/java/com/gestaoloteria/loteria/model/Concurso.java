package com.gestaoloteria.loteria.model;

import java.time.LocalDate;

public class Concurso {
    private Integer id;
    private Integer loteriaId;
    private Integer numero;
    private LocalDate data;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getLoteriaId() { return loteriaId; }
    public void setLoteriaId(Integer loteriaId) { this.loteriaId = loteriaId; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
}