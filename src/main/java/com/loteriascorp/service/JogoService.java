package com.loteriascorp.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.loteriascorp.Jogo;
import com.loteriascorp.database.DatabaseHelper;

public class JogoService {

    private DatabaseHelper databaseHelper;

    public JogoService() {
        this.databaseHelper = new DatabaseHelper();
    }

    public List<Jogo> gerarJogos(int numeroConcurso, int quantidadeJogos) {
        List<Integer> numerosMaisFrequentes = calcularNumerosMaisFrequentes();
        return criarJogos(numeroConcurso, quantidadeJogos, numerosMaisFrequentes);
    }

    private List<Integer> calcularNumerosMaisFrequentes() {
        Map<Integer, Long> frequenciaNumeros = databaseHelper.obterHistoricoJogos()
            .stream()
            .flatMap(jogo -> jogo.getNumeros().stream())
            .collect(Collectors.groupingBy(numero -> numero, Collectors.counting()));

        return frequenciaNumeros.entrySet()
            .stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private List<Jogo> criarJogos(int numeroConcurso, int quantidadeJogos, List<Integer> numerosMaisFrequentes) {
        // Implementação da lógica para criar jogos baseados nos números mais frequentes
        // ...
        return null; // Retorna a lista de jogos gerados
    }
}