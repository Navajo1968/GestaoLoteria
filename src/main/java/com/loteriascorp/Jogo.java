package com.loteriascorp;

import java.util.List;
import java.util.stream.Collectors;

public class Jogo {
    private int numeroJogo;
    private List<Integer> numeros;
    private String dataInclusao;
    private String dataAposta;
    private int numConcurso;
    private int totAcertos;

    public Jogo(int numeroJogo, List<Integer> numeros) {
        this.numeroJogo = numeroJogo;
        this.numeros = numeros;
    }

    public Jogo(int numeroJogo, List<Integer> numeros, String dataInclusao) {
        this.numeroJogo = numeroJogo;
        this.numeros = numeros;
        this.dataInclusao = dataInclusao;
    }

    public Jogo(int numeroJogo, List<Integer> numeros, String dataInclusao, String dataAposta, int numConcurso, int totAcertos) {
        this.numeroJogo = numeroJogo;
        this.numeros = numeros;
        this.dataInclusao = dataInclusao;
        this.dataAposta = dataAposta;
        this.numConcurso = numConcurso;
        this.totAcertos = totAcertos;
    }
    
    public String getNumerosFormatados() {
        if (numeros == null) return "";
        return numeros.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    public int getNumeroJogo() {
        return numeroJogo;
    }

    public void setNumeroJogo(int numeroJogo) {
        this.numeroJogo = numeroJogo;
    }

    public List<Integer> getNumeros() {
        return numeros;
    }

    public void setNumeros(List<Integer> numeros) {
        this.numeros = numeros;
    }

    public String getDataInclusao() {
        return dataInclusao;
    }

    public void setDataInclusao(String dataInclusao) {
        this.dataInclusao = dataInclusao;
    }

    public String getDataAposta() {
        return dataAposta;
    }

    public void setDataAposta(String dataAposta) {
        this.dataAposta = dataAposta;
    }

    public int getNumConcurso() {
        return numConcurso;
    }

    public void setNumConcurso(int numConcurso) {
        this.numConcurso = numConcurso;
    }

    public int getTotAcertos() {
        return totAcertos;
    }

    public void setTotAcertos(int totAcertos) {
        this.totAcertos = totAcertos;
    }

    @Override
    public String toString() {
        return "Jogo{" +
                "numeroJogo=" + numeroJogo +
                ", numeros=" + numeros +
                ", dataInclusao='" + dataInclusao + '\'' +
                ", dataAposta='" + dataAposta + '\'' +
                ", numConcurso=" + numConcurso +
                ", totAcertos=" + totAcertos +
                '}';
    }
}