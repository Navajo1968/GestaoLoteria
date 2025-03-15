package com.loteriascorp.service;

import java.util.List;

public class SimuladorApostas {

    public void simularApostas(int idLoteria, int quantidadeJogos) {
        GeradorJogosOptimizado gerador = new GeradorJogosOptimizado();
        List<List<Integer>> jogos = gerador.gerarJogos(idLoteria, quantidadeJogos);

        for (List<Integer> jogo : jogos) {
            // Simular a aposta com o jogo gerado
            // Aqui você pode adicionar lógica para verificar os resultados
            System.out.println("Jogo gerado: " + jogo);
        }
    }

    public static void main(String[] args) {
        SimuladorApostas simulador = new SimuladorApostas();
        int idLoteria = 1; // ID da loteria (exemplo)
        int quantidadeJogos = 10; // Quantidade de jogos a serem gerados e simulados

        simulador.simularApostas(idLoteria, quantidadeJogos);
    }
}