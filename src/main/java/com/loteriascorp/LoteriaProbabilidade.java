package com.loteriascorp;

public class LoteriaProbabilidade {
    private int idLoterias;
    private int idLoteriasPreco;
    private int idLoteriasProbabilidade;
    private int qtNumerosAcertos;
    private int qtNumerosJogados;
    private int qtProbabilidade;
    private double vlrFatorPremiacao;
    private String nomeLoteria;
    private String nomeLoteriaPreco;

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

    public int getIdLoteriasProbabilidade() {
        return idLoteriasProbabilidade;
    }

    public void setIdLoteriasProbabilidade(int idLoteriasProbabilidade) {
        this.idLoteriasProbabilidade = idLoteriasProbabilidade;
    }

    public int getQtNumerosAcertos() {
        return qtNumerosAcertos;
    }

    public void setQtNumerosAcertos(int qtNumerosAcertos) {
        this.qtNumerosAcertos = qtNumerosAcertos;
    }

    public int getQtNumerosJogados() {
        return qtNumerosJogados;
    }

    public void setQtNumerosJogados(int qtNumerosJogados) {
        this.qtNumerosJogados = qtNumerosJogados;
    }

    public int getQtProbabilidade() {
        return qtProbabilidade;
    }

    public void setQtProbabilidade(int qtProbabilidade) {
        this.qtProbabilidade = qtProbabilidade;
    }

    public double getVlrFatorPremiacao() {
        return vlrFatorPremiacao;
    }

    public void setVlrFatorPremiacao(double vlrFatorPremiacao) {
        this.vlrFatorPremiacao = vlrFatorPremiacao;
    }

    public String getNomeLoteria() {
        return nomeLoteria;
    }

    public void setNomeLoteria(String nomeLoteria) {
        this.nomeLoteria = nomeLoteria;
    }

    public String getNomeLoteriaPreco() {
        return nomeLoteriaPreco;
    }

    public void setNomeLoteriaPreco(String nomeLoteriaPreco) {
        this.nomeLoteriaPreco = nomeLoteriaPreco;
    }
}