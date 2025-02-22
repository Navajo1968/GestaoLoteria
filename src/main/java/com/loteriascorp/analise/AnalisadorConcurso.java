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
    
    
    private void analisarFrequenciaHistorica() {
        String sql = """
            SELECT num, COUNT(*) as frequencia
            FROM (
                SELECT num1 as num FROM tb_historico_jogos WHERE id_loterias = ? AND num_concurso < ?
                UNION ALL SELECT num2 UNION ALL SELECT num3 
                UNION ALL SELECT num4 UNION ALL SELECT num5
                UNION ALL SELECT num6 UNION ALL SELECT num7
                UNION ALL SELECT num8 UNION ALL SELECT num9
                UNION ALL SELECT num10 UNION ALL SELECT num11
                UNION ALL SELECT num12 UNION ALL SELECT num13
                UNION ALL SELECT num14 UNION ALL SELECT num15
            ) nums
            GROUP BY num
            ORDER BY num
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, numeroConcurso);
            
            Map<Integer, Integer> frequencias = new HashMap<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    frequencias.put(rs.getInt("num"), rs.getInt("frequencia"));
                }
            }
            
            // Calcula a frequência média dos números do concurso atual
            double freqMedia = numerosSorteados.stream()
                .mapToDouble(num -> frequencias.getOrDefault(num, 0))
                .average()
                .orElse(0.0);
            
            metricas.put("frequencia_media", freqMedia);
            
            // Identifica números quentes (alta frequência) e frios (baixa frequência)
            long numerosQuentes = numerosSorteados.stream()
                .filter(num -> frequencias.getOrDefault(num, 0) > freqMedia)
                .count();
                
            metricas.put("numeros_quentes", (double) numerosQuentes);
            metricas.put("numeros_frios", (double) (15 - numerosQuentes));
        } catch (SQLException e) {
            logger.error("Erro ao analisar frequências históricas: ", e);
        }
    }
    
    private void analisarPadroesIntervalo() {
        // Calcula intervalos entre números consecutivos
        List<Integer> intervalos = new ArrayList<>();
        for (int i = 1; i < numerosSorteados.size(); i++) {
            intervalos.add(numerosSorteados.get(i) - numerosSorteados.get(i-1));
        }
        
        // Média dos intervalos
        double mediaIntervalos = intervalos.stream()
            .mapToDouble(Integer::doubleValue)
            .average()
            .orElse(0.0);
            
        // Desvio padrão dos intervalos
        double desvioPadrao = Math.sqrt(
            intervalos.stream()
                .mapToDouble(i -> Math.pow(i - mediaIntervalos, 2))
                .average()
                .orElse(0.0)
        );
        
        metricas.put("media_intervalos", mediaIntervalos);
        metricas.put("desvio_padrao_intervalos", desvioPadrao);
    }
    
    private void analisarClusters() {
        // Identifica grupos de números próximos
        List<Integer> ordenados = new ArrayList<>(numerosSorteados);
        Collections.sort(ordenados);
        
        int clusters = 1;
        int maiorCluster = 1;
        int clusterAtual = 1;
        
        for (int i = 1; i < ordenados.size(); i++) {
            if (ordenados.get(i) - ordenados.get(i-1) <= 2) {
                clusterAtual++;
                if (clusterAtual > maiorCluster) {
                    maiorCluster = clusterAtual;
                }
            } else {
                if (clusterAtual > 1) {
                    clusters++;
                }
                clusterAtual = 1;
            }
        }
        
        metricas.put("quantidade_clusters", (double) clusters);
        metricas.put("maior_cluster", (double) maiorCluster);
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
        
        // Análise básica
        analisarParidade();
        analisarDistribuicaoDezenas();
        analisarSequencias();
        analisarSoma();
        analisarMedianaEModa();
        
        // Novas análises
        analisarFrequenciaHistorica();
        analisarPadroesIntervalo();
        analisarClusters();
        
        // Análise comparativa com histórico
        analisarComparativoHistorico();
        
        // Salvar análise
        salvarAnalise();
    }
    
    private void analisarParidade() {
        long pares = numerosSorteados.stream()
            .filter(n -> n % 2 == 0)
            .count();
        
        metricas.put("quantidade_pares", (double) pares);
        metricas.put("quantidade_impares", (double) (15 - pares));
        metricas.put("ratio_paridade", pares / 15.0);
    }
    
    private void analisarDistribuicaoDezenas() {
        Map<Integer, Long> distribuicao = numerosSorteados.stream()
            .collect(Collectors.groupingBy(
                n -> (n - 1) / 10,
                Collectors.counting()
            ));
        
        metricas.put("qtd_primeira_dezena", (double) distribuicao.getOrDefault(0, 0L));
        metricas.put("qtd_segunda_dezena", (double) distribuicao.getOrDefault(1, 0L));
        metricas.put("qtd_terceira_dezena", (double) distribuicao.getOrDefault(2, 0L));
    }
    
    private void analisarSequencias() {
        int sequencias = 0;
        int maiorSequencia = 1;
        int sequenciaAtual = 1;
        
        for (int i = 1; i < numerosSorteados.size(); i++) {
            if (numerosSorteados.get(i) == numerosSorteados.get(i-1) + 1) {
                sequenciaAtual++;
                if (sequenciaAtual > maiorSequencia) {
                    maiorSequencia = sequenciaAtual;
                }
            } else {
                if (sequenciaAtual > 1) {
                    sequencias++;
                }
                sequenciaAtual = 1;
            }
        }
        
        metricas.put("quantidade_sequencias", (double) sequencias);
        metricas.put("maior_sequencia", (double) maiorSequencia);
    }
    
    private void analisarSoma() {
        int soma = numerosSorteados.stream()
            .mapToInt(Integer::intValue)
            .sum();
        
        double media = numerosSorteados.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
            
        metricas.put("soma_total", (double) soma);
        metricas.put("media_numeros", media);
    }
    
    private void analisarMedianaEModa() {
        // Mediana
        List<Integer> ordenados = new ArrayList<>(numerosSorteados);
        Collections.sort(ordenados);
        double mediana = (ordenados.get(7) + ordenados.get(8)) / 2.0;
        
        // Moda
        Map<Integer, Long> frequencias = numerosSorteados.stream()
            .collect(Collectors.groupingBy(
                n -> n,
                Collectors.counting()
            ));
        
        OptionalInt moda = frequencias.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> e.getKey())
            .stream()
            .mapToInt(Integer::intValue)
            .findFirst();
            
        metricas.put("mediana", mediana);
        metricas.put("moda", moda.isPresent() ? (double) moda.getAsInt() : 0.0);
    }
    
    private void analisarComparativoHistorico() {
        // Busca os últimos 10 concursos para comparação
        List<List<Integer>> ultimosConcursos = buscarUltimosConcursos(10);
        
        if (!ultimosConcursos.isEmpty()) {
            // Calcula métricas comparativas
            double mediaAcertos = calcularMediaAcertos(ultimosConcursos);
            double similaridadeMedia = calcularSimilaridadeMedia(ultimosConcursos);
            
            metricas.put("media_acertos_anteriores", mediaAcertos);
            metricas.put("similaridade_media", similaridadeMedia);
        }
    }
    
    private List<List<Integer>> buscarUltimosConcursos(int quantidade) {
        List<List<Integer>> concursos = new ArrayList<>();
        String sql = """
            SELECT num1, num2, num3, num4, num5, num6, num7, num8, num9, 
                   num10, num11, num12, num13, num14, num15
            FROM tb_historico_jogos 
            WHERE id_loterias = ? AND num_concurso < ?
            ORDER BY num_concurso DESC
            LIMIT ?
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, numeroConcurso);
            stmt.setInt(3, quantidade);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    List<Integer> numeros = new ArrayList<>();
                    for (int i = 1; i <= 15; i++) {
                        numeros.add(rs.getInt("num" + i));
                    }
                    concursos.add(numeros);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar concursos anteriores: ", e);
        }
        
        return concursos;
    }
    
    private double calcularMediaAcertos(List<List<Integer>> concursosAnteriores) {
        return concursosAnteriores.stream()
            .mapToDouble(concurso -> 
                numerosSorteados.stream()
                    .filter(concurso::contains)
                    .count())
            .average()
            .orElse(0.0);
    }
    
    private double calcularSimilaridadeMedia(List<List<Integer>> concursosAnteriores) {
        return concursosAnteriores.stream()
            .mapToDouble(concurso -> calcularSimilaridade(concurso))
            .average()
            .orElse(0.0);
    }
    
    private double calcularSimilaridade(List<Integer> outroConcurso) {
        Set<Integer> conjunto1 = new HashSet<>(numerosSorteados);
        Set<Integer> conjunto2 = new HashSet<>(outroConcurso);
        
        Set<Integer> intersecao = new HashSet<>(conjunto1);
        intersecao.retainAll(conjunto2);
        
        Set<Integer> uniao = new HashSet<>(conjunto1);
        uniao.addAll(conjunto2);
        
        return (double) intersecao.size() / uniao.size();
    }
    
    private void salvarAnalise() {
        String sql = """
            INSERT INTO tb_analise_concursos 
            (id_loterias, nr_concurso, tipo_metrica, valor, dt_analise)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Map.Entry<String, Double> metrica : metricas.entrySet()) {
                stmt.setInt(1, idLoteria);
                stmt.setInt(2, numeroConcurso);
                stmt.setString(3, metrica.getKey());
                stmt.setDouble(4, metrica.getValue());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            logger.info("Análise do concurso {} salva com sucesso", numeroConcurso);
        } catch (SQLException e) {
            logger.error("Erro ao salvar análise: ", e);
        }
    }
    
    public Map<String, Double> getMetricas() {
        return new HashMap<>(metricas);
    }
    
    public List<Integer> getNumerosSorteados() {
        return new ArrayList<>(numerosSorteados);
    }
}