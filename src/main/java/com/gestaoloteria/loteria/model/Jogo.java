package com.gestaoloteria.loteria.model;

import java.time.LocalDate;
import java.util.List;

public class Jogo {
    private int id;
    private int loteriaId;
    private int concurso;
    private LocalDate data;
    private List<Integer> dezenas;

    public Jogo() {}

    public Jogo(int loteriaId, int concurso, LocalDate data, List<Integer> dezenas) {
        this.loteriaId = loteriaId;
        this.concurso = concurso;
        this.data = data;
        this.dezenas = dezenas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getLoteriaId() { return loteriaId; }
    public void setLoteriaId(int loteriaId) { this.loteriaId = loteriaId; }
    public int getConcurso() { return concurso; }
    public void setConcurso(int concurso) { this.concurso = concurso; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public List<Integer> getDezenas() { return dezenas; }
    public void setDezenas(List<Integer> dezenas) { this.dezenas = dezenas; }
}