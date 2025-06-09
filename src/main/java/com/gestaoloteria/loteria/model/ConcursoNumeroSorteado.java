package com.gestaoloteria.loteria.model;

public class ConcursoNumeroSorteado {
    private int id;
    private int concursoId;
    private int numero;
    private int ordem;

    public ConcursoNumeroSorteado() {}

    public ConcursoNumeroSorteado(int concursoId, int numero, int ordem) {
        this.concursoId = concursoId;
        this.numero = numero;
        this.ordem = ordem;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getConcursoId() { return concursoId; }
    public void setConcursoId(int concursoId) { this.concursoId = concursoId; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
}