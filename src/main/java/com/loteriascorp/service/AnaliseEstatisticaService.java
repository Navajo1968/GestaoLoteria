package com.loteriascorp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import com.loteriascorp.Database;

public class AnaliseEstatisticaService {
    
    public void atualizarAnaliseEstatistica(int idLoteria) {
        try (Connection conn = Database.getConnection()) {
            // 1. Limpar análises antigas
            limparAnalisesAntigas(conn, idLoteria);
            
            // 2. Buscar histórico de resultados
            List<List<Integer>> historicoResultados = buscarHistoricoResultados(conn, idLoteria);
            
            // 3. Calcular métricas básicas
            Map<Integer, DescriptiveStatistics> estatisticasPorNumero = calcularEstatisticasBasicas(historicoResultados);
            
            // 4. Identificar padrões
            Map<String, Double> padroes = identificarPadroes(historicoResultados);
            
            // 5. Calcular métricas de qualidade
            Map<String, Double> metricas = calcularMetricasQualidade(historicoResultados);
            
            // 6. Gerar pesos para cada número
            Map<Integer, Double> pesosNumeros = calcularPesosNumeros(estatisticasPorNumero, padroes);
            
            // 7. Salvar todas as informações no banco
            salvarAnalises(conn, idLoteria, estatisticasPorNumero, padroes, metricas, pesosNumeros);
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar análise estatística", e);
        }
    }

    private void limparAnalisesAntigas(Connection conn, int idLoteria) throws SQLException {
        String[] tabelas = {
            "tb_analise_estatistica",
            "tb_metricas_qualidade",
            "tb_padroes_identificados",
            "tb_pesos_numeros"
        };
        
        for (String tabela : tabelas) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM " + tabela + " WHERE id_loterias = ?")) {
                stmt.setInt(1, idLoteria);
                stmt.executeUpdate();
            }
        }
    }

 // Adicione estes métodos à classe AnaliseEstatisticaService

    private void salvarAnaliseEstatistica(Connection conn, int idLoteria, 
            Map<Integer, DescriptiveStatistics> estatisticas) throws SQLException {
        String sql = """
            INSERT INTO tb_analise_estatistica 
            (id_loterias, numero, frequencia, media, desvio_padrao, dt_analise)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<Integer, DescriptiveStatistics> entry : estatisticas.entrySet()) {
                stmt.setInt(1, idLoteria);
                stmt.setInt(2, entry.getKey());
                stmt.setLong(3, entry.getValue().getN());
                stmt.setDouble(4, entry.getValue().getMean());
                stmt.setDouble(5, entry.getValue().getStandardDeviation());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void salvarPadroes(Connection conn, int idLoteria, 
            Map<String, Double> padroes) throws SQLException {
        String sql = """
            INSERT INTO tb_padroes_identificados 
            (id_loterias, nome_padrao, valor, dt_identificacao)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Double> entry : padroes.entrySet()) {
                stmt.setInt(1, idLoteria);
                stmt.setString(2, entry.getKey());
                stmt.setDouble(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void salvarMetricas(Connection conn, int idLoteria, 
            Map<String, Double> metricas) throws SQLException {
        String sql = """
            INSERT INTO tb_metricas_qualidade 
            (id_loterias, tipo_metrica, valor, dt_calculo)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Double> entry : metricas.entrySet()) {
                stmt.setInt(1, idLoteria);
                stmt.setString(2, entry.getKey());
                stmt.setDouble(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void salvarPesos(Connection conn, int idLoteria, 
            Map<Integer, Double> pesos) throws SQLException {
        String sql = """
            INSERT INTO tb_pesos_numeros 
            (id_loterias, numero, peso, dt_calculo)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<Integer, Double> entry : pesos.entrySet()) {
                stmt.setInt(1, idLoteria);
                stmt.setInt(2, entry.getKey());
                stmt.setDouble(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    
    private List<List<Integer>> buscarHistoricoResultados(Connection conn, int idLoteria) throws SQLException {
        List<List<Integer>> historico = new ArrayList<>();
        
        String sql = """
            SELECT num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, 
                   num11, num12, num13, num14, num15
            FROM tb_resultados
            WHERE id_loterias = ?
            ORDER BY num_concurso DESC
            LIMIT 100
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLoteria);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                List<Integer> numeros = new ArrayList<>();
                for (int i = 1; i <= 15; i++) {
                    numeros.add(rs.getInt("num" + i));
                }
                historico.add(numeros);
            }
        }
        
        return historico;
    }

    private Map<Integer, DescriptiveStatistics> calcularEstatisticasBasicas(List<List<Integer>> historico) {
        Map<Integer, DescriptiveStatistics> estatisticas = new HashMap<>();
        
        // Inicializar estatísticas para cada número possível (1-25 para Lotofácil)
        for (int i = 1; i <= 25; i++) {
            estatisticas.put(i, new DescriptiveStatistics());
        }
        
        // Calcular frequência e atraso para cada número
        for (int i = 0; i < historico.size(); i++) {
            List<Integer> resultado = historico.get(i);
            for (int numero : resultado) {
                estatisticas.get(numero).addValue(i); // i representa a recência
            }
        }
        
        return estatisticas;
    }

    private Map<String, Double> identificarPadroes(List<List<Integer>> historico) {
        Map<String, Double> padroes = new HashMap<>();
        
        // 1. Análise de paridade
        double mediaParesImparesRatio = calcularMediaParesImpares(historico);
        padroes.put("PARES_IMPARES_RATIO", mediaParesImparesRatio);
        
        // 2. Análise de sequências
        double mediaSequencias = calcularMediaSequencias(historico);
        padroes.put("MEDIA_SEQUENCIAS", mediaSequencias);
        
        // 3. Análise de distribuição por dezena
        Map<Integer, Double> distribuicaoDezenas = calcularDistribuicaoDezenas(historico);
        for (Map.Entry<Integer, Double> entry : distribuicaoDezenas.entrySet()) {
            padroes.put("DEZENA_" + entry.getKey(), entry.getValue());
        }
        
        // 4. Análise de soma total
        double mediaSoma = calcularMediaSoma(historico);
        padroes.put("MEDIA_SOMA", mediaSoma);
        
        return padroes;
    }

    private Map<String, Double> calcularMetricasQualidade(List<List<Integer>> historico) {
        Map<String, Double> metricas = new HashMap<>();
        
        // 1. Diversidade de números
        double diversidade = calcularDiversidade(historico);
        metricas.put("DIVERSIDADE", diversidade);
        
        // 2. Estabilidade de padrões
        double estabilidade = calcularEstabilidade(historico);
        metricas.put("ESTABILIDADE", estabilidade);
        
        // 3. Índice de dispersão
        double dispersao = calcularDispersao(historico);
        metricas.put("DISPERSAO", dispersao);
        
        return metricas;
    }

    private Map<Integer, Double> calcularPesosNumeros(
            Map<Integer, DescriptiveStatistics> estatisticas,
            Map<String, Double> padroes) {
        Map<Integer, Double> pesos = new HashMap<>();
        
        for (int numero = 1; numero <= 25; numero++) {
            DescriptiveStatistics stats = estatisticas.get(numero);
            
            // Combinar múltiplos fatores para gerar o peso final
            double frequencia = stats.getN() / 100.0; // Normalizado para últimos 100 jogos
            double recencia = 1.0 / (stats.getMin() + 1); // Quanto menor o último atraso, maior o peso
            double tendencia = calcularTendencia(stats);
            
            // Peso final é uma combinação ponderada dos fatores
            double pesoFinal = (0.4 * frequencia) + (0.3 * recencia) + (0.3 * tendencia);
            pesos.put(numero, pesoFinal);
        }
        
        return pesos;
    }

    private void salvarAnalises(
            Connection conn,
            int idLoteria,
            Map<Integer, DescriptiveStatistics> estatisticas,
            Map<String, Double> padroes,
            Map<String, Double> metricas,
            Map<Integer, Double> pesos) throws SQLException {
        
        conn.setAutoCommit(false);
        try {
            // 1. Salvar análise estatística
            salvarAnaliseEstatistica(conn, idLoteria, estatisticas);
            
            // 2. Salvar padrões identificados
            salvarPadroes(conn, idLoteria, padroes);
            
            // 3. Salvar métricas de qualidade
            salvarMetricas(conn, idLoteria, metricas);
            
            // 4. Salvar pesos dos números
            salvarPesos(conn, idLoteria, pesos);
            
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Métodos auxiliares de cálculo
    private double calcularMediaParesImpares(List<List<Integer>> historico) {
        double somaRatios = 0;
        for (List<Integer> resultado : historico) {
            long pares = resultado.stream().filter(n -> n % 2 == 0).count();
            somaRatios += (double) pares / resultado.size();
        }
        return somaRatios / historico.size();
    }

    private double calcularMediaSequencias(List<List<Integer>> historico) {
        double somaSequencias = 0;
        for (List<Integer> resultado : historico) {
            List<Integer> ordenado = new ArrayList<>(resultado);
            Collections.sort(ordenado);
            int sequencias = 0;
            for (int i = 1; i < ordenado.size(); i++) {
                if (ordenado.get(i) == ordenado.get(i-1) + 1) {
                    sequencias++;
                }
            }
            somaSequencias += sequencias;
        }
        return somaSequencias / historico.size();
    }

    private Map<Integer, Double> calcularDistribuicaoDezenas(List<List<Integer>> historico) {
        Map<Integer, Double> distribuicao = new HashMap<>();
        for (List<Integer> resultado : historico) {
            for (int numero : resultado) {
                int dezena = (numero - 1) / 10;
                distribuicao.merge(dezena, 1.0, Double::sum);
            }
        }
        
        // Normalizar
        int totalNumeros = historico.size() * 15;
        for (Map.Entry<Integer, Double> entry : distribuicao.entrySet()) {
            distribuicao.put(entry.getKey(), entry.getValue() / totalNumeros);
        }
        
        return distribuicao;
    }

    private double calcularMediaSoma(List<List<Integer>> historico) {
        return historico.stream()
            .mapToDouble(resultado -> resultado.stream().mapToInt(Integer::intValue).sum())
            .average()
            .orElse(0);
    }

    private double calcularDiversidade(List<List<Integer>> historico) {
        Set<Integer> numerosUnicos = new HashSet<>();
        historico.forEach(resultado -> numerosUnicos.addAll(resultado));
        return (double) numerosUnicos.size() / 25; // Normalizado para total de números possíveis
    }

    private double calcularEstabilidade(List<List<Integer>> historico) {
        // Calcula o desvio padrão das somas dos jogos
        DescriptiveStatistics stats = new DescriptiveStatistics();
        historico.forEach(resultado -> 
            stats.addValue(resultado.stream().mapToInt(Integer::intValue).sum())
        );
        return 1 - (stats.getStandardDeviation() / stats.getMean()); // Normalizado
    }

    private double calcularDispersao(List<List<Integer>> historico) {
        double somaDispersoes = 0;
        for (List<Integer> resultado : historico) {
            List<Integer> ordenado = new ArrayList<>(resultado);
            Collections.sort(ordenado);
            double dispersao = ordenado.get(ordenado.size()-1) - ordenado.get(0);
            somaDispersoes += dispersao;
        }
        return somaDispersoes / (historico.size() * 24); // Normalizado pelo range máximo possível
    }

    private double calcularTendencia(DescriptiveStatistics stats) {
        if (stats.getN() < 2) return 0;
        return 1 - (stats.getStandardDeviation() / stats.getMean());
    }
}