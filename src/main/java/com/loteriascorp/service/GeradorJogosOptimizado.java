package com.loteriascorp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.loteriascorp.Database;

public class GeradorJogosOptimizado {
    private static final Logger logger = LogManager.getLogger(GeradorJogosOptimizado.class);
    private static final int TAMANHO_JOGO = 15;
    private static final int NUMERO_MAXIMO = 25;
    private static final double PESO_RECENCIA = 0.3;
    private static final double PESO_FREQUENCIA = 0.4;
    private static final double PESO_PADRAO = 0.3;
    private static final int MAX_SEQUENCIAS_CONSECUTIVAS = 4;
    private static final int MAX_SIMILARIDADE = 11; // Máximo de números iguais permitidos entre dois jogos
    
    // Record para armazenar resultado da análise
    public record ResultadoAnalise(
        int qtPares,
        int qtImpares,
        double media,
        double desvioPadrao,
        Map<String, Integer> distribuicaoDezenas,
        double scoreQualidade
    ) {}
    
    public GeradorJogosOptimizado() {
    }

    /**
     * Valida um jogo de acordo com critérios avançados
     * @param jogo Lista de números do jogo
     * @return true se o jogo é válido, false caso contrário
     */
    public boolean validarJogoAvancado(List<Integer> jogo) {
        try {
            // 1. Verifica distribuição de pares e ímpares
            long pares = jogo.stream().filter(n -> n % 2 == 0).count();
            if (pares < 6 || pares > 9) {
                logger.debug("Jogo inválido: distribuição de pares/ímpares fora do padrão");
                return false;
            }
            
            // 2. Verifica distribuição por dezenas
            Map<Integer, Long> distribuicaoDezenas = jogo.stream()
                .collect(Collectors.groupingBy(n -> (n-1)/10, Collectors.counting()));
            
            if (distribuicaoDezenas.values().stream().anyMatch(v -> v > 6)) {
                logger.debug("Jogo inválido: muitos números em uma única dezena");
                return false;
            }
            
            // 3. Verifica sequências consecutivas
            List<Integer> ordenado = new ArrayList<>(jogo);
            Collections.sort(ordenado);
            int sequencias = 0;
            for (int i = 1; i < ordenado.size(); i++) {
                if (ordenado.get(i) == ordenado.get(i-1) + 1) {
                    sequencias++;
                    if (sequencias > MAX_SEQUENCIAS_CONSECUTIVAS) {
                        logger.debug("Jogo inválido: muitas sequências consecutivas");
                        return false;
                    }
                } else {
                    sequencias = 0;
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Erro ao validar jogo: ", e);
            return false;
        }
    }
    
    /**
     * Gera números prováveis baseado em análise estatística
     */
    public List<Integer> gerarNumerosProvaveis(int idLoteria) {
        String sql = """
            WITH metricas_recentes AS (
                SELECT 
                    tipo_metrica,
                    AVG(valor) as media_valor
                FROM tb_analise_concursos
                WHERE id_loterias = ?
                AND nr_concurso >= (SELECT MAX(nr_concurso) - 10 FROM tb_analise_concursos WHERE id_loterias = ?)
                GROUP BY tipo_metrica
            ),
            numeros_estatistica AS (
                SELECT 
                    num_analisado,
                    frequencia * ? + -- PESO_FREQUENCIA
                    (1.0 / NULLIF(recencia, 0)) * ? + -- PESO_RECENCIA
                    media_posicao * ? as score -- PESO_PADRAO
                FROM tb_analise_estatistica
                WHERE id_loterias = ?
                AND dt_analise = (
                    SELECT MAX(dt_analise) 
                    FROM tb_analise_estatistica 
                    WHERE id_loterias = ?
                )
            )
            SELECT 
                num_analisado,
                score
            FROM numeros_estatistica
            ORDER BY score DESC
        """;
        
        List<Integer> numerosProvaveis = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, idLoteria);
            stmt.setDouble(3, PESO_FREQUENCIA);
            stmt.setDouble(4, PESO_RECENCIA);
            stmt.setDouble(5, PESO_PADRAO);
            stmt.setInt(6, idLoteria);
            stmt.setInt(7, idLoteria);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next() && numerosProvaveis.size() < TAMANHO_JOGO) {
                numerosProvaveis.add(rs.getInt("num_analisado"));
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao gerar números prováveis: ", e);
        }
        
        return numerosProvaveis;
    }
    
    /**
     * Busca jogos históricos para comparação
     */
    private List<List<Integer>> buscarJogosHistoricos(int idLoteria, int limite) {
        List<List<Integer>> historico = new ArrayList<>();
        String sql = """
            SELECT num1, num2, num3, num4, num5, num6, num7, num8, num9, 
                   num10, num11, num12, num13, num14, num15
            FROM tb_historico_jogos
            WHERE id_loterias = ?
            ORDER BY nr_concurso DESC
            LIMIT ?
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, limite);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                List<Integer> jogo = new ArrayList<>();
                for (int i = 1; i <= 15; i++) {
                    jogo.add(rs.getInt(i));
                }
                historico.add(jogo);
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar jogos históricos: ", e);
        }
        
        return historico;
    }
    
    /**
     * Gera uma lista de jogos baseada em análise estatística
     */
    public List<List<Integer>> gerarJogos(int idLoteria, int quantidade) {
        List<List<Integer>> jogos = new ArrayList<>();
        List<Integer> numerosBase = gerarNumerosProvaveis(idLoteria);
        List<List<Integer>> historico = buscarJogosHistoricos(idLoteria, 100); // Busca últimos 100 jogos
        
        int tentativas = 0;
        while (jogos.size() < quantidade && tentativas < quantidade * 3) {
            List<Integer> jogo = new ArrayList<>(numerosBase);
            aplicarVariacoes(jogo);
            
            if (validarJogoAvancado(jogo) && validarSimilaridade(jogo, jogos, historico)) {
                jogos.add(jogo);
            }
            
            tentativas++;
        }
        
        if (jogos.size() < quantidade) {
            logger.warn("Não foi possível gerar todos os {} jogos solicitados. Gerados: {}", 
                       quantidade, jogos.size());
        }
        
        return jogos;
    }

    /**
     * Valida se um jogo não é muito similar a outros
     */
    private boolean validarSimilaridade(List<Integer> novoJogo, 
                                      List<List<Integer>> jogosGerados,
                                      List<List<Integer>> historico) {
        // Verifica similaridade com jogos já gerados
        for (List<Integer> jogoGerado : jogosGerados) {
            if (calcularSimilaridade(novoJogo, jogoGerado) > MAX_SIMILARIDADE) {
                logger.debug("Jogo muito similar a outro já gerado");
                return false;
            }
        }
        
        // Verifica similaridade com histórico
        for (List<Integer> jogoHistorico : historico) {
            if (calcularSimilaridade(novoJogo, jogoHistorico) > MAX_SIMILARIDADE) {
                logger.debug("Jogo muito similar a um jogo histórico");
                return false;
            }
        }
        
        return true;
    }

    /**
     * Aplica variações aleatórias a um jogo
     */
    private void aplicarVariacoes(List<Integer> jogo) {
        Random random = new Random();
        int substituicoes = random.nextInt(3) + 1;
        
        for (int i = 0; i < substituicoes; i++) {
            int posicaoRemover = random.nextInt(jogo.size());
            jogo.remove(posicaoRemover);
            
            int novoNumero;
            do {
                novoNumero = random.nextInt(NUMERO_MAXIMO) + 1;
            } while (jogo.contains(novoNumero));
            
            jogo.add(novoNumero);
        }
        
        Collections.sort(jogo);
    }

    /**
     * Calcula a similaridade entre dois jogos (quantidade de números em comum)
     */
    private int calcularSimilaridade(List<Integer> jogo1, List<Integer> jogo2) {
        return (int) jogo1.stream().filter(jogo2::contains).count();
    }

    /**
     * Analisa um jogo e retorna suas métricas
     */
    private ResultadoAnalise analisarJogo(List<Integer> jogo) {
        int pares = (int) jogo.stream().filter(n -> n % 2 == 0).count();
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        jogo.forEach(stats::addValue);
        
        Map<String, Integer> distribuicao = new HashMap<>();
        for (int num : jogo) {
            if (num <= 10) distribuicao.merge("1-10", 1, Integer::sum);
            else if (num <= 20) distribuicao.merge("11-20", 1, Integer::sum);
            else distribuicao.merge("21-25", 1, Integer::sum);
        }
        
        double scoreQualidade = calcularScoreQualidade(pares, distribuicao);
        
        return new ResultadoAnalise(
            pares,
            TAMANHO_JOGO - pares,
            stats.getMean(),
            stats.getStandardDeviation(),
            distribuicao,
            scoreQualidade
        );
    }

    /**
     * Calcula o score de qualidade de um jogo
     */
    private double calcularScoreQualidade(int pares, Map<String, Integer> distribuicao) {
        double score = 0.0;
        
        // Avaliação da paridade (ideal: 7-8 ou 8-7)
        score += (pares >= 7 && pares <= 8) ? 1.0 : 0.0;
        
        // Avaliação da distribuição por dezenas
        int d1 = distribuicao.getOrDefault("1-10", 0);
        int d2 = distribuicao.getOrDefault("11-20", 0);
        int d3 = distribuicao.getOrDefault("21-25", 0);
        
        // Distribuição ideal aproximada: 6-6-3
        score += (d1 >= 5 && d1 <= 7) ? 1.0 : 0.0;
        score += (d2 >= 5 && d2 <= 7) ? 1.0 : 0.0;
        score += (d3 >= 2 && d3 <= 4) ? 1.0 : 0.0;
        
        return score / 4.0; // Normaliza para 0-1
    }

    /**
     * Salva os jogos gerados no banco de dados
     */
    public void salvarJogosGerados(int idLoterias, List<List<Integer>> jogos) {
        String sql = """
            INSERT INTO tb_jogos_gerados 
            (id_loterias, dt_inclusao, dt_aposta, num1, num2, num3, num4, num5, 
             num6, num7, num8, num9, num10, num11, num12, num13, num14, num15)
            VALUES (?, CURRENT_TIMESTAMP, CURRENT_DATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (List<Integer> jogo : jogos) {
                stmt.setInt(1, idLoterias);
                for (int i = 0; i < jogo.size(); i++) {
                    stmt.setInt(i + 2, jogo.get(i));
                }
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            logger.info("Salvos {} jogos gerados no banco de dados", jogos.size());
        } catch (Exception e) {
            logger.error("Erro ao salvar jogos gerados: ", e);
        }
    }

    /**
     * Retorna as métricas de qualidade de um jogo
     */
    public Map<String, Double> getMetricasJogo(List<Integer> jogo) {
        Map<String, Double> metricas = new HashMap<>();
        ResultadoAnalise analise = analisarJogo(jogo);
        
        metricas.put("scoreQualidade", analise.scoreQualidade());
        metricas.put("percentualPares", (double) analise.qtPares() / TAMANHO_JOGO);
        metricas.put("mediaNumeros", analise.media());
        metricas.put("desvioPadrao", analise.desvioPadrao());
        
        return metricas;
    }
}