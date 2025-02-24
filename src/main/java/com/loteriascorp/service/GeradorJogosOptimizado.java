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
import java.util.stream.IntStream;

public class GeradorJogosOptimizado {
    private static final Logger logger = LogManager.getLogger(GeradorJogosOptimizado.class);
    private static final int TAMANHO_JOGO = 15;
    private static final int NUMERO_MAXIMO = 25;
    private static final double PESO_RECENCIA = 0.4;
    private static final double PESO_FREQUENCIA = 0.4;
    private static final double PESO_PADRAO = 0.2;
    private static final int MAX_SEQUENCIAS_CONSECUTIVAS = 4;
    private static final int MAX_SIMILARIDADE = 12;
    
    /**
     * Gera números baseados em análise de frequência
     */
    private List<Integer> gerarNumerosPorFrequencia(int idLoteria) {
        String sql = """
            WITH ultimos_sorteios AS (
                SELECT 
                    num_concurso,
                    UNNEST(ARRAY[num1, num2, num3, num4, num5, num6, num7, num8,
                               num9, num10, num11, num12, num13, num14, num15]) as numero
                FROM tb_historico_jogos
                WHERE id_loterias = ?
                AND num_concurso >= (
                    SELECT MAX(num_concurso) - 50 
                    FROM tb_historico_jogos 
                    WHERE id_loterias = ?
                )
            )
            SELECT 
                numero,
                COUNT(*) as frequencia,
                MAX(num_concurso) - MIN(num_concurso) as periodo,
                AVG(num_concurso) as media_ocorrencia
            FROM ultimos_sorteios
            GROUP BY numero
            ORDER BY 
                frequencia DESC,
                periodo ASC,
                media_ocorrencia DESC
            LIMIT ?
        """;
        
        List<Integer> numerosFrequentes = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, idLoteria);
            stmt.setInt(3, TAMANHO_JOGO * 2); // Busca o dobro para ter mais opções
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                numerosFrequentes.add(rs.getInt("numero"));
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao gerar números por frequência: ", e);
        }
        
        return numerosFrequentes;
    }
    
    private List<Integer> analisarJogosPremiados(int idLoteria) {
        String sql = """
            WITH jogos_pontuados AS (
                SELECT 
                    hj.num1, hj.num2, hj.num3, hj.num4, hj.num5, hj.num6, hj.num7, hj.num8,
                    hj.num9, hj.num10, hj.num11, hj.num12, hj.num13, hj.num14, hj.num15,
                    ap.tot_acertos,
                    CASE 
                        WHEN ap.tot_acertos >= 13 THEN 3
                        WHEN ap.tot_acertos >= 12 THEN 2
                        WHEN ap.tot_acertos >= 11 THEN 1
                        ELSE 0
                    END as peso_acertos
                FROM tb_historico_jogos hj
                JOIN tb_jogos_gerados ap ON hj.num_concurso = ap.num_concurso
                WHERE hj.id_loterias = ?
                AND ap.tot_acertos >= 11  -- Considerar jogos com 11 ou mais acertos
                ORDER BY hj.num_concurso DESC
                LIMIT 50  -- Aumentar o limite para ter mais dados para análise
            )
            SELECT 
                num,
                SUM(peso_acertos) as score_numero
            FROM (
                SELECT UNNEST(ARRAY[num1, num2, num3, num4, num5, num6, num7, num8,
                                  num9, num10, num11, num12, num13, num14, num15]) as num,
                      peso_acertos
                FROM jogos_pontuados
            ) t
            GROUP BY num
            HAVING SUM(peso_acertos) > 0
            ORDER BY score_numero DESC, num ASC
            LIMIT 15
        """;
        
        List<Integer> numerosPremiados = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                numerosPremiados.add(rs.getInt("num"));
            }
            logger.info("Números premiados encontrados: {}", numerosPremiados);
        } catch (SQLException e) {
            logger.error("Erro ao analisar jogos premiados: ", e);
        }
        return numerosPremiados;
    }
    
    /**
     * Gera números prováveis baseado em análise estatística
     */
    public List<Integer> gerarNumerosProvaveis(int idLoteria) {
        List<Integer> numerosFrequentes = gerarNumerosPorFrequencia(idLoteria);
        List<Integer> numerosPremiados = analisarJogosPremiados(idLoteria);
        
        // Combinar as duas análises
        Map<Integer, Double> scoresFinal = new HashMap<>();
        
        // Se não temos muitos números premiados, aumentar peso da frequência
        double pesoPremiados = numerosPremiados.isEmpty() ? 0.2 : PESO_PADRAO;
        double pesoFrequencia = numerosPremiados.isEmpty() ? 0.8 : PESO_FREQUENCIA + PESO_RECENCIA;
        
        numerosFrequentes.forEach(n -> 
            scoresFinal.put(n, scoresFinal.getOrDefault(n, 0.0) + pesoFrequencia));
        
        numerosPremiados.forEach(n -> 
            scoresFinal.put(n, scoresFinal.getOrDefault(n, 0.0) + pesoPremiados));
        
        // Adicionar log para debug
        logger.info("Números frequentes: {}", numerosFrequentes);
        logger.info("Números premiados: {}", numerosPremiados);
        
        List<Integer> numerosSelecionados = scoresFinal.entrySet().stream()
            .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
            .limit(TAMANHO_JOGO)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        logger.info("Números selecionados para o jogo: {}", numerosSelecionados);
        
        return numerosSelecionados;
    }

    private void aplicarVariacoesInteligentes(List<Integer> jogo, List<List<Integer>> historico) {
        Random random = new Random();
        Map<Integer, Double> scoreNumeros = calcularScoreNumeros(historico);
        
        // Quantidade de números a variar baseada no histórico de sucesso
        int qtdVariacoes = random.nextInt(3) + 2; // 2 a 4 variações
        
        for (int i = 0; i < qtdVariacoes; i++) {
            // Remove números com menor score
            jogo.sort((a, b) -> Double.compare(scoreNumeros.getOrDefault(a, 0.0), 
                                             scoreNumeros.getOrDefault(b, 0.0)));
            jogo.remove(0); // Remove o número com menor score
            
            // Adiciona números com maior probabilidade de sucesso
            List<Integer> candidatos = IntStream.rangeClosed(1, 25)
                .filter(n -> !jogo.contains(n))
                .boxed()
                .sorted((a, b) -> Double.compare(scoreNumeros.getOrDefault(b, 0.0),
                                               scoreNumeros.getOrDefault(a, 0.0)))
                .collect(Collectors.toList());
            
            if (!candidatos.isEmpty()) {
                // Escolhe aleatoriamente entre os top 5 melhores candidatos
                int indice = random.nextInt(Math.min(5, candidatos.size()));
                jogo.add(candidatos.get(indice));
            }
        }
        
        Collections.sort(jogo);
    }

    private Map<Integer, Double> calcularScoreNumeros(List<List<Integer>> historico) {
        Map<Integer, Double> scores = new HashMap<>();
        Map<Integer, Integer> frequencia = new HashMap<>();
        Map<Integer, Integer> recencia = new HashMap<>();
        
        for (int i = 0; i < historico.size(); i++) {
            List<Integer> jogo = historico.get(i);
            for (int numero : jogo) {
                frequencia.merge(numero, 1, Integer::sum);
                recencia.putIfAbsent(numero, i); // Guarda a primeira ocorrência (mais recente)
            }
        }
        
        int maxFreq = frequencia.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int maxRec = historico.size();
        
        for (int numero = 1; numero <= 25; numero++) {
            double scoreFreq = (double) frequencia.getOrDefault(numero, 0) / maxFreq;
            double scoreRec = 1.0 - ((double) recencia.getOrDefault(numero, maxRec) / maxRec);
            scores.put(numero, (scoreFreq * 0.6) + (scoreRec * 0.4));
        }
        
        return scores;
    }
    
    private double avaliarProbabilidadeAcertos(List<Integer> jogo, List<List<Integer>> historico) {
        String sql = """
            WITH ultimos_jogos AS (
                SELECT 
                    h.num_concurso,
                    ARRAY[h.num1,h.num2,h.num3,h.num4,h.num5,h.num6,h.num7,h.num8,
                          h.num9,h.num10,h.num11,h.num12,h.num13,h.num14,h.num15] as numeros,
                    g.tot_acertos
                FROM tb_historico_jogos h
                JOIN tb_jogos_gerados g ON h.num_concurso = g.num_concurso
                WHERE h.id_loterias = 1
                ORDER BY h.num_concurso DESC
                LIMIT 200
            )
            SELECT 
                COUNT(*) as total_jogos,
                SUM(CASE WHEN tot_acertos >= 11 THEN 1 ELSE 0 END) as jogos_premiados
            FROM ultimos_jogos u
            WHERE (
                -- Verifica similaridade na distribuição par/ímpar
                ABS((SELECT COUNT(*) FROM UNNEST(u.numeros) n WHERE n % 2 = 0) - 
                    (SELECT COUNT(*) FROM UNNEST(?) n WHERE n % 2 = 0)) <= 2
            )
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
                
            Object[] jogoArray = jogo.toArray();
            stmt.setArray(1, conn.createArrayOf("integer", jogoArray));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalJogos = rs.getInt("total_jogos");
                int jogosPremidos = rs.getInt("jogos_premiados");
                
                if (totalJogos == 0) return 0.5;
                return (double) jogosPremidos / totalJogos;
            }
        } catch (SQLException e) {
            logger.error("Erro ao avaliar probabilidade de acertos: ", e);
        }
        return 0.5;
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
        int maxTentativas = quantidade * 30; // Aumentado para dar mais chances
        
        while (jogos.size() < quantidade && tentativas < maxTentativas) {
            List<Integer> jogoAtual = new ArrayList<>(numerosBase);
            aplicarVariacoesInteligentes(jogoAtual, historico);
            
            if (validarJogoAvancado(jogoAtual) && 
                validarSimilaridade(jogoAtual, jogos, historico)) {
                
                double probabilidade = avaliarProbabilidadeAcertos(jogoAtual, historico);
                if (probabilidade >= 0.4) { // Mantemos o uso da avaliação de probabilidade
                    jogos.add(new ArrayList<>(jogoAtual));
                    logger.info("Jogo {} de {} gerado com sucesso (probabilidade: {:.2f})", 
                              jogos.size(), quantidade, probabilidade);
                }
            }
            
            tentativas++;
            if (tentativas % 10 == 0) {
                logger.info("Tentativa {}/{}, jogos gerados: {}/{}",
                           tentativas, maxTentativas, jogos.size(), quantidade);
            }
        }
        
        // Se não conseguiu gerar todos os jogos, log de aviso
        if (jogos.size() < quantidade) {
            logger.warn("Gerados apenas {} de {} jogos solicitados após {} tentativas",
                       jogos.size(), quantidade, tentativas);
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
            
            // Adicionar ao método validarJogoAvancado
            int soma = jogo.stream().mapToInt(Integer::intValue).sum();
            if (soma < 190 || soma > 220) { // Faixa típica de somas premiadas
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