package com.loteriascorp;

public class LoteriaPreco {
    private int idLoterias;
    private int idLoteriasPreco;
    private int qtNumerosJogados;
    private double vlrAposta;
    private String nomeLoteria;

    // Getters e setters
    public int getIdLoterias() {
        return idLoterias;
    }

    public void setIdLoterias(int idLoterias) {
        this.idLoterias = idLoterias;
    }

    public int getIdLoteriasPreco() {
        return idLoteriasPreco;
    }

    public void setIdLoteriasPreco(int idLoteriasPreco) {
        this.idLoteriasPreco = idLoteriasPreco;
    }

    public int getQtNumerosJogados() {
        return qtNumerosJogados;
    }

    public void setQtNumerosJogados(int qtNumerosJogados) {
        this.qtNumerosJogados = qtNumerosJogados;
    }

    public double getVlrAposta() {
        return vlrAposta;
    }

    public void setVlrAposta(double vlrAposta) {
        this.vlrAposta = vlrAposta;
    }

    public String getNomeLoteria() {
        return nomeLoteria;
    }

    public void setNomeLoteria(String nomeLoteria) {
        this.nomeLoteria = nomeLoteria;
    }
}