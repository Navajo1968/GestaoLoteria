package com.loteriascorp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
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
    private static final int MAX_SIMILARIDADE = 11;
    
   
    /**
     * Busca o último concurso realizado para uma loteria
     */
    private int buscarUltimoConcurso(int idLoteria) {
        String sql = "SELECT MAX(num_concurso) as ultimo FROM tb_historico_jogos WHERE id_loterias = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("ultimo");
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar último concurso: ", e);
        }
        return 0;
    }
    
    /**
     * Gera números prováveis baseado em análise estatística do último concurso
     */
    public List<Integer> gerarNumerosProvaveis(int idLoteria) {
        int ultimoConcurso = buscarUltimoConcurso(idLoteria);
        if (ultimoConcurso == 0) {
            logger.error("Nenhum concurso encontrado para a loteria {}", idLoteria);
            return new ArrayList<>();
        }
        
        String sql = """
            WITH numeros_analisados AS (
                SELECT 
                    num,
                    COUNT(*) as frequencia,
                    MAX(num_concurso) - MIN(num_concurso) as recencia,
                    AVG(posicao) as media_posicao
                FROM (
                    SELECT num_concurso,
                           UNNEST(ARRAY[num1, num2, num3, num4, num5, num6, num7, num8,
                                      num9, num10, num11, num12, num13, num14, num15]) as num,
                           UNNEST(ARRAY[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]) as posicao
                    FROM tb_historico_jogos
                    WHERE id_loterias = ?
                    AND num_concurso >= ? - 10
                ) t
                GROUP BY num
            )
            SELECT 
                num,
                frequencia,
                recencia,
                media_posicao,
                (frequencia * ? + 
                 (1.0 / NULLIF(recencia, 0)) * ? + 
                 media_posicao * ?) as score
            FROM numeros_analisados
            ORDER BY score DESC
            LIMIT ?
        """;
        
        List<Integer> numerosProvaveis = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, ultimoConcurso);
            stmt.setDouble(3, PESO_FREQUENCIA);
            stmt.setDouble(4, PESO_RECENCIA);
            stmt.setDouble(5, PESO_PADRAO);
            stmt.setInt(6, TAMANHO_JOGO);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                numerosProvaveis.add(rs.getInt("num"));
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao gerar números prováveis: ", e);
        }
        
        return numerosProvaveis;
    }
    
    /**
     * Gera uma lista de jogos baseada em análise estatística
     */
    public List<List<Integer>> gerarJogos(int idLoteria, int quantidade) {
        List<List<Integer>> jogos = new ArrayList<>();
        List<Integer> numerosBase = gerarNumerosProvaveis(idLoteria);
        
        if (numerosBase.isEmpty()) {
            logger.warn("Não foi possível gerar números base para a loteria {}", idLoteria);
            return jogos;
        }
        
        List<List<Integer>> historico = buscarJogosHistoricos(idLoteria, 100);
        
        int tentativas = 0;
        int maxTentativas = quantidade * 3;
        
        while (jogos.size() < quantidade && tentativas < maxTentativas) {
            List<Integer> jogo = new ArrayList<>(numerosBase);
            aplicarVariacoes(jogo);
            
            if (validarJogoAvancado(jogo) && validarSimilaridade(jogo, jogos, historico)) {
                jogos.add(new ArrayList<>(jogo));
            }
            
            tentativas++;
        }
        
        if (jogos.size() < quantidade) {
            logger.warn("Gerados apenas {} de {} jogos solicitados", jogos.size(), quantidade);
        }
        
        return jogos;
    }
    
    /**
     * Valida se um jogo atende aos critérios básicos
     */
    public boolean validarJogoAvancado(List<Integer> jogo) {
        try {
            // Verifica tamanho do jogo
            if (jogo.size() != TAMANHO_JOGO) {
                return false;
            }
            
            // Verifica números válidos
            if (!jogo.stream().allMatch(n -> n >= 1 && n <= NUMERO_MAXIMO)) {
                return false;
            }
            
            // Verifica distribuição de pares e ímpares
            long pares = jogo.stream().filter(n -> n % 2 == 0).count();
            if (pares < 6 || pares > 9) {
                return false;
            }
            
            // Verifica distribuição por dezenas
            Map<Integer, Long> distribuicaoDezenas = jogo.stream()
                .collect(Collectors.groupingBy(n -> (n-1)/10, Collectors.counting()));
            
            if (distribuicaoDezenas.values().stream().anyMatch(v -> v > 6)) {
                return false;
            }
            
            // Verifica sequências consecutivas
            List<Integer> ordenado = new ArrayList<>(jogo);
            Collections.sort(ordenado);
            int sequencias = 0;
            for (int i = 1; i < ordenado.size(); i++) {
                if (ordenado.get(i) == ordenado.get(i-1) + 1) {
                    sequencias++;
                    if (sequencias > MAX_SEQUENCIAS_CONSECUTIVAS) {
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
     * Busca jogos históricos para comparação
     */
    private List<List<Integer>> buscarJogosHistoricos(int idLoteria, int limite) {
        List<List<Integer>> historico = new ArrayList<>();
        String sql = """
            SELECT num1, num2, num3, num4, num5, num6, num7, num8, num9, 
                   num10, num11, num12, num13, num14, num15
            FROM tb_historico_jogos
            WHERE id_loterias = ?
            ORDER BY num_concurso DESC
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
                Collections.sort(jogo);
                historico.add(jogo);
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar jogos históricos: ", e);
        }
        
        return historico;
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
     * Valida se um jogo não é muito similar a outros
     */
    private boolean validarSimilaridade(List<Integer> novoJogo, 
                                      List<List<Integer>> jogosGerados,
                                      List<List<Integer>> historico) {
        // Verifica similaridade com jogos já gerados
        for (List<Integer> jogoGerado : jogosGerados) {
            if (calcularSimilaridade(novoJogo, jogoGerado) > MAX_SIMILARIDADE) {
                return false;
            }
        }
        
        // Verifica similaridade com histórico
        for (List<Integer> jogoHistorico : historico) {
            if (calcularSimilaridade(novoJogo, jogoHistorico) > MAX_SIMILARIDADE) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calcula a quantidade de números em comum entre dois jogos
     */
    private int calcularSimilaridade(List<Integer> jogo1, List<Integer> jogo2) {
        return (int) jogo1.stream().filter(jogo2::contains).count();
    }
}