package com.gestaoloteria.loteria.model;

import java.time.LocalDate;

public class Concurso {
    private int id;
    private int loteriaId;
    private int numeroConcurso;
    private LocalDate dataConcurso;

    public Concurso() {}

    public Concurso(int loteriaId, int numeroConcurso, LocalDate dataConcurso) {
        this.loteriaId = loteriaId;
        this.numeroConcurso = numeroConcurso;
        this.dataConcurso = dataConcurso;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getLoteriaId() { return loteriaId; }
    public void setLoteriaId(int loteriaId) { this.loteriaId = loteriaId; }
    public int getNumeroConcurso() { return numeroConcurso; }
    public void setNumeroConcurso(int numeroConcurso) { this.numeroConcurso = numeroConcurso; }
    public LocalDate getDataConcurso() { return dataConcurso; }
    public void setDataConcurso(LocalDate dataConcurso) { this.dataConcurso = dataConcurso; }
}