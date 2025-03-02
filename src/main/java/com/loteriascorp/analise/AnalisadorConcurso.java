package com.loteriascorp.analise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.loteriascorp.Database;

public class AnalisadorConcurso {
    private static final Logger logger = LogManager.getLogger(AnalisadorConcurso.class);
    
    private final int idLoteria;
    private final int numeroConcurso;
    private List<Integer> numerosSorteados;
    private Map<String, Double> metricas;
    
    public AnalisadorConcurso(int idLoteria, int numeroConcurso) {
        this.idLoteria = idLoteria;
        this.numeroConcurso = numeroConcurso;
        this.numerosSorteados = new ArrayList<>();
        this.metricas = new HashMap<>();
        carregarConcurso();
    }
    
       
    private void salvarMetrica(String tipo, double valor) {
        String sql = """
            INSERT INTO tb_analise_concursos 
            (id_loterias, nr_concurso, tipo_metrica, valor)
            VALUES (?, ?, ?, ?)
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, numeroConcurso);
            stmt.setString(3, tipo);
            stmt.setDouble(4, valor);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("Erro ao salvar métrica {}: ", tipo, e);
        }
    }
    
    
    private void carregarConcurso() {
        String sql = """
            SELECT num1, num2, num3, num4, num5, num6, num7, num8, num9, 
                   num10, num11, num12, num13, num14, num15
            FROM tb_historico_jogos 
            WHERE id_loterias = ? AND num_concurso = ?
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, numeroConcurso);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (int i = 1; i <= 15; i++) {
                        numerosSorteados.add(rs.getInt("num" + i));
                    }
                    Collections.sort(numerosSorteados);
                    logger.info("Concurso {} carregado com sucesso", numeroConcurso);
                } else {
                    logger.error("Concurso {} não encontrado", numeroConcurso);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao carregar concurso: ", e);
        }
    }
    
    public void analisarConcurso() {
        if (numerosSorteados.isEmpty()) {
            logger.error("Não há números para analisar");
            return;
        }
        
     // Nova métrica: análise de números consecutivos
        int consecutivos = 0;
        for (int i = 1; i < numerosSorteados.size(); i++) {
            if (numerosSorteados.get(i) - numerosSorteados.get(i - 1) == 1) {
                consecutivos++;
            }
        }
        salvarMetrica("NUMEROS_CONSECUTIVOS", consecutivos);
        
        // Nova métrica: análise de desvio padrão
        double media = numerosSorteados.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double desvioPadrao = Math.sqrt(numerosSorteados.stream().mapToDouble(n -> Math.pow(n - media, 2)).average().orElse(0.0));
        salvarMetrica("DESVIO_PADRAO", desvioPadrao);
        
        // Análise de paridade
        long pares = numerosSorteados.stream().filter(n -> n % 2 == 0).count();
        salvarMetrica("PARES", pares);
        salvarMetrica("IMPARES", 15 - pares);
        
        // Distribuição por dezenas
        Map<Integer, Long> distribuicao = numerosSorteados.stream()
            .collect(Collectors.groupingBy(
                n -> (n - 1) / 10,
                Collectors.counting()
            ));
        
        salvarMetrica("DEZENA_1_10", distribuicao.getOrDefault(0, 0L));
        salvarMetrica("DEZENA_11_20", distribuicao.getOrDefault(1, 0L));
        salvarMetrica("DEZENA_21_25", distribuicao.getOrDefault(2, 0L));
        
        // Soma e média
        int soma = numerosSorteados.stream().mapToInt(Integer::intValue).sum();
        salvarMetrica("SOMA_TOTAL", soma);
        salvarMetrica("MEDIA", soma / 15.0);
        
        // Intervalos
        List<Integer> ordenados = new ArrayList<>(numerosSorteados);
        Collections.sort(ordenados);
        
        double mediaIntervalos = 0;
        int totalIntervalos = 0;
        for (int i = 1; i < ordenados.size(); i++) {
            int intervalo = ordenados.get(i) - ordenados.get(i-1);
            mediaIntervalos += intervalo;
            totalIntervalos++;
        }
        mediaIntervalos /= totalIntervalos;
        
        salvarMetrica("MEDIA_INTERVALOS", mediaIntervalos);
        
        // Análise de clusters
        int clusters = 1;
        int maiorCluster = 1;
        int clusterAtual = 1;
        
        for (int i = 1; i < ordenados.size(); i++) {
            if (ordenados.get(i) - ordenados.get(i-1) <= 2) {
                clusterAtual++;
                maiorCluster = Math.max(maiorCluster, clusterAtual);
            } else {
                if (clusterAtual > 1) clusters++;
                clusterAtual = 1;
            }
        }
        
        salvarMetrica("QTD_CLUSTERS", clusters);
        salvarMetrica("MAIOR_CLUSTER", maiorCluster);
        
        // Atualiza análise estatística
        new AnaliseEstatistica(idLoteria).analisarNumeros();
    }
    
    public Map<String, Double> getMetricas() {
        return new HashMap<>(metricas);
    }
    
    public List<Integer> getNumerosSorteados() {
        return new ArrayList<>(numerosSorteados);
    }
}