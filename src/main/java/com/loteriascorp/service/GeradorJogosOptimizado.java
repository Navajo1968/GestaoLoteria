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
    private static final int MAX_SEQUENCIAS_CONSECUTIVAS = 4;
   
     
    /**
     * Gera números prováveis baseado em análise estatística
     */
    private List<Integer> gerarNumerosProvaveis(int idLoteria) {
        String sql = """
            WITH ultimos_100 AS (
                SELECT 
                    num_concurso,
                    ARRAY[num1,num2,num3,num4,num5,num6,num7,num8,
                         num9,num10,num11,num12,num13,num14,num15] as numeros
                FROM tb_historico_jogos
                WHERE id_loterias = ?
                ORDER BY num_concurso DESC
                LIMIT 100
            ),
            analise_numeros AS (
                SELECT 
                    n.numero,
                    COUNT(*) as frequencia,
                    AVG((
                        SELECT COUNT(*) 
                        FROM UNNEST(u.numeros) n2 
                        WHERE n2 % 2 = 0
                    )) as media_pares
                FROM ultimos_100 u,
                     LATERAL UNNEST(u.numeros) as n(numero)
                GROUP BY n.numero
            )
            SELECT 
                numero,
                frequencia,
                media_pares
            FROM analise_numeros
            ORDER BY frequencia DESC, numero ASC
        """;
        
        List<Integer> numerosBase = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
                
            stmt.setInt(1, idLoteria);
            ResultSet rs = stmt.executeQuery();
            
            // Seleciona os números mais frequentes que mantenham o equilíbrio par/ímpar
            int pares = 0, impares = 0;
            while (rs.next() && numerosBase.size() < TAMANHO_JOGO) {
                int numero = rs.getInt("numero");
                if (numero % 2 == 0) {
                    if (pares < 8) { // Máximo 8 pares
                        numerosBase.add(numero);
                        pares++;
                    }
                } else {
                    if (impares < 8) { // Máximo 8 ímpares
                        numerosBase.add(numero);
                        impares++;
                    }
                }
            }
            
            Collections.sort(numerosBase);
            logger.info("Números base gerados: {}", numerosBase);
            
        } catch (SQLException e) {
            logger.error("Erro ao gerar números prováveis: ", e);
        }
        
        return numerosBase;
    }

   
    /**
     * Gera uma lista de jogos baseada em análise estatística
     */
    public List<List<Integer>> gerarJogos(int idLoteria, int quantidade) {
        logger.info("Iniciando geração de {} jogos para loteria {}", quantidade, idLoteria);
        
        List<List<Integer>> jogos = new ArrayList<>();
        List<Integer> numerosBase = gerarNumerosProvaveis(idLoteria);
        
        if (numerosBase.isEmpty()) {
            logger.error("Não foi possível gerar números base para a loteria {}", idLoteria);
            return jogos;
        }
        
        logger.info("Números base gerados: {}", numerosBase);
        
        Random random = new Random();
        int tentativas = 0;
        int maxTentativas = quantidade * 100;
        
        while (jogos.size() < quantidade) {
            List<Integer> jogoAtual = new ArrayList<>(numerosBase);
            
            // Garantir variação entre os jogos
            int numerosParaTrocar = 3 + random.nextInt(3); // 3 a 5 números
            for (int i = 0; i < numerosParaTrocar; i++) {
                if (!jogoAtual.isEmpty()) {
                    // Remove um número aleatório
                    int indexRemover = random.nextInt(jogoAtual.size());
                    jogoAtual.remove(indexRemover);
                    
                    // Adiciona um novo número que não está no jogo
                    List<Integer> numerosDisponiveis = new ArrayList<>();
                    for (int num = 1; num <= NUMERO_MAXIMO; num++) {
                        if (!jogoAtual.contains(num)) {
                            numerosDisponiveis.add(num);
                        }
                    }
                    
                    if (!numerosDisponiveis.isEmpty()) {
                        int novoNumero = numerosDisponiveis.get(random.nextInt(numerosDisponiveis.size()));
                        jogoAtual.add(novoNumero);
                    }
                }
            }
            
            Collections.sort(jogoAtual);
            
            if (validarJogoSimples(jogoAtual) && !temMuitaSimilaridade(jogoAtual, jogos)) {
                jogos.add(new ArrayList<>(jogoAtual));
                logger.info("Jogo {} de {} gerado com sucesso: {}", jogos.size(), quantidade, jogoAtual);
            }
            
            tentativas++;
            if (tentativas >= maxTentativas) {
                logger.warn("Atingido limite de tentativas ({}) com apenas {} jogos gerados", maxTentativas, jogos.size());
                break;
            }
            
            if (tentativas % 10 == 0) {
                logger.info("Progresso: {}/{} jogos gerados após {} tentativas", jogos.size(), quantidade, tentativas);
            }
        }
        
        logger.info("Finalizada geração de jogos. Solicitados: {}, Gerados: {}", quantidade, jogos.size());
        return jogos;
    }
    
    private boolean validarJogoSimples(List<Integer> jogo) {
        // Verifica tamanho
        if (jogo.size() != TAMANHO_JOGO) {
            logger.debug("Jogo inválido: tamanho incorreto {}", jogo.size());
            return false;
        }
        
        // Verifica números dentro do intervalo
        if (jogo.stream().anyMatch(n -> n < 1 || n > NUMERO_MAXIMO)) {
            logger.debug("Jogo inválido: números fora do intervalo permitido");
            return false;
        }
        
        // Verifica duplicatas
        if (new HashSet<>(jogo).size() != TAMANHO_JOGO) {
            logger.debug("Jogo inválido: números duplicados");
            return false;
        }
        
        // Verifica distribuição par/ímpar
        long pares = jogo.stream().filter(n -> n % 2 == 0).count();
        if (pares < 5 || pares > 10) {
            logger.debug("Jogo inválido: distribuição par/ímpar fora do padrão ({} pares)", pares);
            return false;
        }
        
        return true;
    }    

private boolean temMuitaSimilaridade(List<Integer> novoJogo, List<List<Integer>> jogosExistentes) {
    for (List<Integer> jogoExistente : jogosExistentes) {
        int similares = 0;
        for (Integer numero : novoJogo) {
            if (jogoExistente.contains(numero)) {
                similares++;
            }
        }
        if (similares > 11) { // Aumentado para permitir mais similaridade
            logger.debug("Jogo muito similar: {} números iguais", similares);
            return true;
        }
    }
    return false;
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
}    
  