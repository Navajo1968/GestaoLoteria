package com.loteriascorp.service;

import com.loteriascorp.Jogo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JogoService {

    public JogoService() {
        // Construtor vazio ou outras inicializações, se necessário
    }

    public List<Jogo> gerarJogos(int numeroConcurso, int quantidadeJogos) {
        List<Integer> numerosMaisFrequentes = calcularNumerosMaisFrequentes();
        List<Integer> numerosPrevistos = preverNumeros(numerosMaisFrequentes);
        return criarJogos(numeroConcurso, quantidadeJogos, numerosPrevistos);
    }
    
    private List<Integer> calcularNumerosMaisFrequentes() {
        // Implementar lógica para calcular números mais frequentes
        return new ArrayList<>(); // Retornar lista de números mais frequentes
    }
    
    private List<Integer> preverNumeros(List<Integer> numerosMaisFrequentes) {
        // Implementar algoritmo de machine learning para prever números
        return new ArrayList<>(); // Retornar lista de números previstos
    }
    
    private List<Jogo> criarJogos(int numeroConcurso, int quantidadeJogos, List<Integer> numerosPrevistos) {
        List<Jogo> jogos = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < quantidadeJogos; i++) {
            Jogo jogo = new Jogo(i + 1, new ArrayList<>());
            jogo.setNumConcurso(numeroConcurso);
            List<Integer> numerosDoJogo = new ArrayList<>(numerosPrevistos);
            
            // Embaralhar e selecionar os números para o jogo
            while (numerosDoJogo.size() > 15) {
                numerosDoJogo.remove(random.nextInt(numerosDoJogo.size()));
            }
            
            jogo.setNumeros(numerosDoJogo);
            jogos.add(jogo);
        }
        
        return jogos;
    }
}