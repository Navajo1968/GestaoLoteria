package com.loteriascorp;

import java.util.List;

public class JogoGerado {
    private int numeroJogo;
    private List<Integer> numeros;
    private int totAcertos;

    public JogoGerado(int numeroJogo, List<Integer> numeros, int totAcertos) {
        this.numeroJogo = numeroJogo;
        this.numeros = numeros;
        this.totAcertos = totAcertos;
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

    public int getTotAcertos() {
        return totAcertos;
    }

    public void setTotAcertos(int totAcertos) {
        this.totAcertos = totAcertos;
    }

    @Override
    public String toString() {
        return "JogoGerado{" +
                "numeroJogo=" + numeroJogo +
                ", numeros=" + numeros +
                ", totAcertos=" + totAcertos +
                '}';
    }
}