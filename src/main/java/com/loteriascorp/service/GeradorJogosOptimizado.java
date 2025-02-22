package com.loteriascorp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;  // Adicione esta importação
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.loteriascorp.Database;
import com.loteriascorp.analise.AnaliseEstatistica;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import com.loteriascorp.model.PadraoSucesso;

public class GeradorJogosOptimizado {
    private static final Logger logger = LogManager.getLogger(GeradorJogosOptimizado.class);
    private final AnaliseEstatistica analiseEstatistica;
    private static final int TAMANHO_JOGO = 15;
    private static final int NUMERO_MAXIMO = 25;
    private static final double PESO_RECENCIA = 0.3;
    private static final double PESO_FREQUENCIA = 0.4;
    private static final double PESO_PADRAO = 0.3;
    private static final int HISTORICO_MINIMO = 100; // Mínimo de jogos históricos para análise
    private static final double PESO_SEQUENCIA = 0.15;
    private static final double PESO_DISTRIBUICAO = 0.15;
    private static final int MAX_SEQUENCIAS_CONSECUTIVAS = 4;
    
    public GeradorJogosOptimizado() {
        this.analiseEstatistica = new AnaliseEstatistica();
    }

 // Adicione estes métodos à classe GeradorJogosOptimizado

    private int contarFrequencia(int numero, List<List<Integer>> historico) {
        return (int) historico.stream()
            .filter(jogo -> jogo.contains(numero))
            .count();
    }

    private double calcularAjusteDistribuicao(int numero, List<PadraoSucesso> padroes) {
        if (padroes.isEmpty()) {
            return 0.0;
        }

        int dezena = (numero - 1) / 10;
        double[] distribuicaoIdeal = calcularDistribuicaoIdeal(padroes);
        
        // Usa a constante PESO_DISTRIBUICAO aqui
        return PESO_DISTRIBUICAO * distribuicaoIdeal[dezena];
    }
    
    private List<PadraoSucesso> analisarPadroesSucesso(List<List<Integer>> historico) {
        return historico.stream()
            .limit(100) // Analisa apenas os últimos 100 concursos
            .map((List<Integer> jogo) -> { // Tipo explícito adicionado
                // Calcula proporção de números pares
                double propPares = jogo.stream()
                    .filter(n -> n % 2 == 0)
                    .count() / (double) jogo.size();

                // Calcula distribuição por dezenas
                int[] distDezenas = new int[3];
                for (Integer n : jogo) {
                    distDezenas[(n-1)/10]++;
                }

                // Conta sequências consecutivas
                int seqConsec = contarSequenciasConsecutivas(jogo);

                // Calcula soma total
                double soma = jogo.stream()
                    .mapToDouble(Integer::doubleValue)
                    .sum();

                return new PadraoSucesso(propPares, distDezenas, seqConsec, soma);
            })
            .collect(Collectors.toList());
    }

    private int contarSequenciasConsecutivas(List<Integer> numeros) {
        List<Integer> ordenados = new ArrayList<>(numeros);
        Collections.sort(ordenados);
        int sequencias = 0;
        for (int i = 1; i < ordenados.size(); i++) {
            if (ordenados.get(i) == ordenados.get(i-1) + 1) {
                sequencias++;
            }
        }
        return sequencias;
    }

    private int contarSequenciasAnteriores(int numero, Map<Integer, Double> pesos) {
        int sequencias = 0;
        int numeroAnterior = numero - 1;
        while (numeroAnterior > 0 && pesos.containsKey(numeroAnterior) && 
               pesos.get(numeroAnterior) > 0.5) {
            sequencias++;
            numeroAnterior--;
        }
        return sequencias;
    }

    private double normalizarPeso(double peso) {
        return Math.max(0.0, Math.min(1.0, peso));
    }
    
    private Map<Integer, Double> ajustarPesosComPadroes(Map<Integer, Double> pesosIniciais, List<List<Integer>> historico) {
        Map<Integer, Double> pesosAjustados = new HashMap<>(pesosIniciais);
        
        // Analisa padrões de sucesso nos últimos jogos
        List<PadraoSucesso> padroesSucesso = analisarPadroesSucesso(historico);
        
        for (int numero = 1; numero <= NUMERO_MAXIMO; numero++) {
            double pesoAtual = pesosAjustados.get(numero);
            
            // Ajusta baseado em padrões de paridade
            double ajusteParidade = calcularAjusteParidade(numero, padroesSucesso);
            
            // Ajusta baseado em distribuição por dezenas
            double ajusteDistribuicao = calcularAjusteDistribuicao(numero, padroesSucesso);
            
            // Ajusta baseado em sequências
            double ajusteSequencia = calcularAjusteSequencia(numero, pesosAjustados);
            
            // Aplica os ajustes
            double pesoFinal = pesoAtual * (1 + ajusteParidade + ajusteDistribuicao + ajusteSequencia);
            pesosAjustados.put(numero, normalizarPeso(pesoFinal));
        }
        
        return pesosAjustados;
    }

    private double calcularAjusteParidade(int numero, List<PadraoSucesso> padroes) {
        boolean ehPar = numero % 2 == 0;
        double ajuste = 0;
        
        // Analisa proporção ideal de pares/ímpares nos padrões de sucesso
        double proporcaoPares = padroes.stream()
            .mapToDouble(p -> p.getProporçaoPares())
            .average()
            .orElse(0.5);
            
        if ((ehPar && proporcaoPares > 0.5) || (!ehPar && proporcaoPares < 0.5)) {
            ajuste += 0.1;
        }
        
        return ajuste;
    }

    private double[] calcularDistribuicaoIdeal(List<PadraoSucesso> padroes) {
        double[] distribuicaoMedia = new double[3];
        
        if (padroes.isEmpty()) {
            // Distribuição padrão se não houver padrões (aproximadamente 6-6-3)
            distribuicaoMedia[0] = 0.4; // 6/15 para primeira dezena
            distribuicaoMedia[1] = 0.4; // 6/15 para segunda dezena
            distribuicaoMedia[2] = 0.2; // 3/15 para terceira dezena
            return distribuicaoMedia;
        }
        
        // Calcula a média da distribuição dos padrões de sucesso
        for (PadraoSucesso padrao : padroes) {
            int[] distribuicao = padrao.getDistribuicaoDezenas();
            for (int i = 0; i < 3; i++) {
                distribuicaoMedia[i] += distribuicao[i];
            }
        }
        
        // Normaliza as médias
        for (int i = 0; i < 3; i++) {
            distribuicaoMedia[i] = distribuicaoMedia[i] / (padroes.size() * TAMANHO_JOGO);
        }
        
        return distribuicaoMedia;
    }

    private double calcularAjusteSequencia(int numero, Map<Integer, Double> pesos) {
        int sequenciasAnteriores = contarSequenciasAnteriores(numero, pesos);
        if (sequenciasAnteriores >= MAX_SEQUENCIAS_CONSECUTIVAS) {
            return -PESO_SEQUENCIA; // Usa a constante aqui
        }
        return 0;
    }

    public void validarJogoAvancado(List<Integer> jogo) {
        // Validações adicionais para melhorar a qualidade dos jogos
        
        // 1. Verifica distribuição de pares e ímpares
        long pares = jogo.stream().filter(n -> n % 2 == 0).count();
        if (pares < 6 || pares > 9) {
            throw new IllegalStateException("Distribuição de pares/ímpares fora do padrão ideal");
        }
        
        // 2. Verifica distribuição por dezenas
        Map<Integer, Long> distribuicaoDezenas = jogo.stream()
            .collect(Collectors.groupingBy(n -> (n-1)/10, Collectors.counting()));
        
        if (distribuicaoDezenas.values().stream().anyMatch(v -> v > 6)) {
            throw new IllegalStateException("Muitos números em uma única dezena");
        }
        
        // 3. Verifica sequências consecutivas
        List<Integer> ordenado = new ArrayList<>(jogo);
        Collections.sort(ordenado);
        int sequencias = 0;
        for (int i = 1; i < ordenado.size(); i++) {
            if (ordenado.get(i) == ordenado.get(i-1) + 1) {
                sequencias++;
                if (sequencias > MAX_SEQUENCIAS_CONSECUTIVAS) {
                    throw new IllegalStateException("Muitas sequências consecutivas");
                }
            } else {
                sequencias = 0;
            }
        }
    }
    
    
    private double calcularMediaPosicao(int numero, List<List<Integer>> historico) {
        double somaPosicoes = 0;
        int ocorrencias = 0;
        
        for (List<Integer> jogo : historico) {
            int posicao = jogo.indexOf(numero);
            if (posicao >= 0) {
                somaPosicoes += posicao;
                ocorrencias++;
            }
        }
        
        return ocorrencias > 0 ? somaPosicoes / ocorrencias : 0;
    }

    private double calcularDiversidade(List<List<Integer>> historico) {
        Set<Integer> numerosUnicos = new HashSet<>();
        historico.forEach(jogo -> numerosUnicos.addAll(jogo));
        return (double) numerosUnicos.size() / NUMERO_MAXIMO;
    }

    private double calcularEstabilidade(List<List<Integer>> historico) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        historico.forEach(jogo -> 
            stats.addValue(jogo.stream().mapToInt(Integer::intValue).sum())
        );
        return 1 - (stats.getStandardDeviation() / stats.getMean());
    }

    private double calcularDispersao(List<List<Integer>> historico) {
        double somaDispersoes = 0;
        for (List<Integer> jogo : historico) {
            List<Integer> ordenado = new ArrayList<>(jogo);
            Collections.sort(ordenado);
            double dispersao = ordenado.get(ordenado.size()-1) - ordenado.get(0);
            somaDispersoes += dispersao;
        }
        return somaDispersoes / (historico.size() * (NUMERO_MAXIMO - 1));
    }

    private double calcularMediaParesImpares(List<List<Integer>> historico) {
        double somaRatios = 0;
        for (List<Integer> jogo : historico) {
            long pares = jogo.stream().filter(n -> n % 2 == 0).count();
            somaRatios += (double) pares / jogo.size();
        }
        return somaRatios / historico.size();
    }

    private double calcularMediaSequencias(List<List<Integer>> historico) {
        double somaSequencias = 0;
        for (List<Integer> jogo : historico) {
            List<Integer> ordenado = new ArrayList<>(jogo);
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

    private double calcularMediaSoma(List<List<Integer>> historico) {
        return historico.stream()
            .mapToDouble(jogo -> jogo.stream().mapToInt(Integer::intValue).sum())
            .average()
            .orElse(0);
    }
    
    public List<List<Integer>> gerarJogos(int idLoterias, int quantidadeJogos) {
        logger.info("Iniciando geração de {} jogos para loteria ID {}", quantidadeJogos, idLoterias);
        
        List<List<Integer>> jogosHistoricos = buscarJogosHistoricos(idLoterias);
        atualizarAnalises(idLoterias, jogosHistoricos);
        Map<Integer, Double> pesosNumeros = calcularPesosNumeros(jogosHistoricos);
        List<PadraoJogo> padroesSucesso = identificarPadroesSuccess(jogosHistoricos);
        
        List<List<Integer>> jogosGerados = new ArrayList<>();
        int tentativas = 0;
        int maxTentativas = quantidadeJogos * 3; // Limite de tentativas para evitar loop infinito
        
        while (jogosGerados.size() < quantidadeJogos && tentativas < maxTentativas) {
            List<Integer> novoJogo = gerarJogoOptimizado(pesosNumeros, padroesSucesso);
            if (validarJogo(novoJogo, jogosHistoricos, jogosGerados)) {
                jogosGerados.add(novoJogo);
                logger.debug("Jogo {} gerado com sucesso", jogosGerados.size());
            }
            tentativas++;
        }
        
        logger.info("Geração concluída. {} jogos gerados em {} tentativas", jogosGerados.size(), tentativas);
        return jogosGerados;
    }

    private List<List<Integer>> buscarJogosHistoricos(int idLoterias) {
        List<List<Integer>> historico = new ArrayList<>();
        String sql = """
            SELECT num1, num2, num3, num4, num5, num6, num7, num8, num9, 
                   num10, num11, num12, num13, num14, num15 
            FROM tb_historico_jogos 
            WHERE id_loterias = ? 
            ORDER BY dt_jogo DESC 
            LIMIT 100
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoterias);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                List<Integer> numeros = new ArrayList<>();
                for (int i = 1; i <= 15; i++) {
                    numeros.add(rs.getInt("num" + i));
                }
                historico.add(numeros);
            }
            logger.info("Recuperados {} jogos históricos", historico.size());
        } catch (Exception e) {
            logger.error("Erro ao buscar jogos históricos: ", e);
        }
        
        return historico;
    }

    private double calcularAjusteRecencia(int numero, List<List<Integer>> historico) {
        double ajuste = 1.0;
        int ultimos = Math.min(20, historico.size());
        int ocorrenciasRecentes = 0;
        
        // Analisa os últimos 10 jogos
        for (int i = 0; i < ultimos; i++) {
            List<Integer> jogo = historico.get(i);
            if (jogo.contains(numero)) {
                // Quanto mais recente a ocorrência, maior o peso
                ajuste += (ultimos - i) * 0.1;
                ocorrenciasRecentes++;
            }
        }
        
        // Ajuste para evitar números muito frequentes nos últimos jogos
        if (ocorrenciasRecentes > 5) {
            // Reduz o peso se o número apareceu mais de 5 vezes nos últimos 10 jogos
            ajuste *= 0.7;
        } else if (ocorrenciasRecentes == 0) {
            // Aumenta levemente o peso se o número não apareceu nos últimos 10 jogos
            ajuste *= 1.2;
        }
        
        // Normaliza o ajuste para ficar entre 0.5 e 1.5
        ajuste = Math.max(0.5, Math.min(1.5, ajuste));
        
        logger.debug("Ajuste de recência para número {}: {}", numero, ajuste);
        return ajuste;
    }
    
    private Map<Integer, Double> calcularPesosNumeros(List<List<Integer>> historico) {
        if (historico.size() < HISTORICO_MINIMO) {
            logger.warn("Histórico insuficiente para análise estatística confiável");
        }

        Map<Integer, Double> pesos = new HashMap<>();
        Frequency freq = new Frequency();
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        // Análise de frequência com peso temporal
        for (int i = 0; i < historico.size(); i++) {
            List<Integer> jogo = historico.get(i);
            double pesoTemporal = 1.0 + (PESO_RECENCIA * (historico.size() - i) / historico.size());
            
            for (int num : jogo) {
                freq.addValue(num);
                stats.addValue(num * pesoTemporal);
            }
        }
        
        // Normalização dos pesos
        double maxFreq = 0;
        for (int i = 1; i <= NUMERO_MAXIMO; i++) {
            maxFreq = Math.max(maxFreq, freq.getCount(i));
        }
        
        // Calcula pesos considerando múltiplos fatores
        for (int i = 1; i <= NUMERO_MAXIMO; i++) {
            double freqNormalizada = freq.getCount(i) / maxFreq;
            double recenciaNormalizada = calcularRecenciaNormalizada(i, historico);
            double padraoNormalizado = calcularPesosPadroes(i, historico);
            double ajusteRecencia = calcularAjusteRecencia(i, historico);
            
            // Peso final com todos os fatores
            double pesoFinal = (PESO_FREQUENCIA * freqNormalizada * ajusteRecencia) +
                              (PESO_RECENCIA * recenciaNormalizada) +
                              (PESO_PADRAO * padraoNormalizado);
            
            // Normaliza o peso final para ficar entre 0 e 1
            pesoFinal = Math.max(0.0, Math.min(1.0, pesoFinal));
            
            pesos.put(i, pesoFinal);
            
            logger.debug("Número {}: freq={}, recencia={}, padrao={}, ajuste={}, peso={}",
                        i, freqNormalizada, recenciaNormalizada, padraoNormalizado, 
                        ajusteRecencia, pesoFinal);
        }
        Map<Integer, Double> pesosIniciais = pesos;
        pesos = ajustarPesosComPadroes(pesosIniciais, historico);
        
        return pesos;
    }
    
    private boolean verificarPesosSalvos(Connection conn, int idLoterias) {
        String sql = "SELECT COUNT(*) FROM tb_pesos_numeros WHERE id_loterias = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLoterias);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                logger.info("Encontrados {} pesos salvos para loteria {}", count, idLoterias);
                return count > 0;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar pesos salvos: ", e);
        }
        return false;
    }
    
    private double calcularPesosPadroes(int numero, List<List<Integer>> historico) {
        // Análise de padrões específicos da loteria
        int numeroPar = numero % 2 == 0 ? 1 : 0;
        int dezena = (numero - 1) / 10;
        
        double pesoPadrao = 0;
        int jogosAnalisados = Math.min(50, historico.size());
        
        for (int i = 0; i < jogosAnalisados; i++) {
            List<Integer> jogo = historico.get(i);
            
            // Analisa distribuição de pares/ímpares
            int pares = (int) jogo.stream().filter(n -> n % 2 == 0).count();
            if ((numeroPar == 1 && pares < 8) || (numeroPar == 0 && (15 - pares) < 8)) {
                pesoPadrao += 0.5;
            }
            
            // Analisa distribuição por dezenas
            long numerosDaDezena = jogo.stream()
                .filter(n -> (n - 1) / 10 == dezena)
                .count();
            
            if (numerosDaDezena < 4) { // Evita concentração excessiva em uma dezena
                pesoPadrao += 0.5;
            }
        }
        
        return pesoPadrao / jogosAnalisados;
    }
    
    private double calcularRecenciaNormalizada(int numero, List<List<Integer>> historico) {
        double recencia = 0;
        int ultimasOcorrencias = 0;
        
        // Analisa últimos 20 jogos com peso decrescente
        for (int i = 0; i < Math.min(20, historico.size()); i++) {
            if (historico.get(i).contains(numero)) {
                recencia += 1.0 / (i + 1);
                ultimasOcorrencias++;
            }
        }
        
        // Penaliza números que aparecem muito frequentemente nos últimos jogos
        if (ultimasOcorrencias > 10) {
            recencia *= 0.8; // Reduz peso de números muito frequentes
        }
        
        return recencia;
    }
    
    private List<Integer> gerarJogoOptimizado(Map<Integer, Double> pesos, List<PadraoJogo> padroes) {
        List<Integer> numerosCandidatos = new ArrayList<>();
        for (int i = 1; i <= NUMERO_MAXIMO; i++) {
            // Adiciona cada número proporcionalmente ao seu peso
            int quantidade = (int) (pesos.get(i) * 100);
            for (int j = 0; j < quantidade; j++) {
                numerosCandidatos.add(i);
            }
        }
        
        // Gera jogo aleatório considerando os pesos
        Set<Integer> jogoGerado = new TreeSet<>();
        Random random = new Random();
        
        while (jogoGerado.size() < TAMANHO_JOGO) {
            int idx = random.nextInt(numerosCandidatos.size());
            jogoGerado.add(numerosCandidatos.get(idx));
        }
        
        List<Integer> jogo = new ArrayList<>(jogoGerado);
        
        // Aplica ajustes baseados nos padrões de sucesso
        ajustarJogoComPadroes(jogo, padroes);
        
        return jogo;
    }

    private void ajustarJogoComPadroes(List<Integer> jogo, List<PadraoJogo> padroes) {
        // Analisa o jogo atual
        AnaliseEstatistica.ResultadoAnalise analise = analiseEstatistica.analisarJogo(jogo);
        
        // Verifica se precisa ajustar baseado nos padrões de sucesso
        if (!validarPadroes(analise, padroes)) {
            // Tenta ajustar mantendo alguns números e substituindo outros
            ajustarNumeros(jogo, padroes);
        }
    }

    private static class PadraoJogo {
        private final int qtPares;
        private final double scoreQualidade;
        
        public PadraoJogo(int qtPares, double scoreQualidade) {
            this.qtPares = qtPares;
            this.scoreQualidade = scoreQualidade;
        }

        public double getScoreQualidade() {
            return scoreQualidade;
        }

        public int getQtPares() {
            return qtPares;
        }
    }

    private List<PadraoJogo> identificarPadroesSuccess(List<List<Integer>> historico) {
        List<PadraoJogo> padroes = new ArrayList<>();
        
        for (List<Integer> jogo : historico) {
            // Analisa o jogo
            int pares = 0;
            Map<String, Integer> distribuicao = new HashMap<>();
            
            for (int num : jogo) {
                if (num % 2 == 0) pares++;
                
                if (num <= 10) distribuicao.merge("1-10", 1, Integer::sum);
                else if (num <= 20) distribuicao.merge("11-20", 1, Integer::sum);
                else distribuicao.merge("21-25", 1, Integer::sum);
            }
            
            // Calcula score de qualidade
            double score = calcularScoreQualidadePadrao(pares, distribuicao);
            
            // Adiciona apenas padrões com score alto
            if (score >= 0.7) {
            	padroes.add(new PadraoJogo(pares, score));
            }
        }
        
        logger.info("Identificados {} padrões de sucesso", padroes.size());
        return padroes;
    }

    private double calcularScoreQualidadePadrao(int pares, Map<String, Integer> distribuicao) {
        double score = 0.0;
        
        // Avalia paridade (ideal: 7-8 ou 8-7)
        if (pares >= 7 && pares <= 8) score += 0.4;
        
        // Avalia distribuição por dezenas (ideal aproximado: 6-6-3)
        int d1 = distribuicao.getOrDefault("1-10", 0);
        int d2 = distribuicao.getOrDefault("11-20", 0);
        int d3 = distribuicao.getOrDefault("21-25", 0);
        
        if (d1 >= 5 && d1 <= 7) score += 0.2;
        if (d2 >= 5 && d2 <= 7) score += 0.2;
        if (d3 >= 2 && d3 <= 4) score += 0.2;
        
        return score;
    }

    private boolean validarPadroes(AnaliseEstatistica.ResultadoAnalise analise, List<PadraoJogo> padroes) {
        if (padroes.isEmpty()) return true;
        
        // Cria um padrão temporário para o jogo atual
        Map<String, Integer> distribuicaoAtual = new HashMap<>();
        distribuicaoAtual.put("1-10", analise.distribuicaoDezenas().getOrDefault("1-10", 0));
        distribuicaoAtual.put("11-20", analise.distribuicaoDezenas().getOrDefault("11-20", 0));
        distribuicaoAtual.put("21-25", analise.distribuicaoDezenas().getOrDefault("21-25", 0));
        
        PadraoJogo padraoAtual = new PadraoJogo(
        	    analise.qtPares(),
        	    analise.scoreQualidade()
        	);
        
        // Verifica se corresponde a algum padrão de sucesso
        for (PadraoJogo padrao : padroes) {
            if (Math.abs(padrao.getScoreQualidade() - padraoAtual.getScoreQualidade()) <= 0.2) {
                return true;
            }
        }
        
        return false;
    }

    private void ajustarNumeros(List<Integer> jogo, List<PadraoJogo> padroes) {
        if (padroes.isEmpty()) return;
        
        // Encontra o melhor padrão
        PadraoJogo melhorPadrao = padroes.stream()
            .max(Comparator.comparingDouble(PadraoJogo::getScoreQualidade))
            .orElse(null);
            
        if (melhorPadrao == null) return;
        
        // Ajusta o jogo para se aproximar do melhor padrão
        Set<Integer> numerosDisponiveis = new HashSet<>();
        for (int i = 1; i <= NUMERO_MAXIMO; i++) {
            if (!jogo.contains(i)) {
                numerosDisponiveis.add(i);
            }
        }
        
        // Tenta ajustar a paridade
        final int paresIniciais = (int) jogo.stream().filter(n -> n % 2 == 0).count();
        if (Math.abs(paresIniciais - melhorPadrao.getQtPares()) > 1) {
            // Substitui alguns números para ajustar a paridade
            for (int i = 0; i < jogo.size() && Math.abs(paresIniciais - melhorPadrao.getQtPares()) > 1; i++) {
                if ((paresIniciais > melhorPadrao.getQtPares() && jogo.get(i) % 2 == 0) ||
                    (paresIniciais < melhorPadrao.getQtPares() && jogo.get(i) % 2 != 0)) {
                    
                    // Encontra um número substituto com a paridade oposta
                    Optional<Integer> substituto = numerosDisponiveis.stream()
                        .filter(n -> (paresIniciais > melhorPadrao.getQtPares()) != (n % 2 == 0))
                        .findFirst();
                        
                    if (substituto.isPresent()) {
                        numerosDisponiveis.remove(substituto.get());
                        numerosDisponiveis.add(jogo.get(i));
                        jogo.set(i, substituto.get());
                    }
                }
            }
        }
        
        // Ordena o jogo após os ajustes
        Collections.sort(jogo);
    }

        private boolean validarJogo(List<Integer> novoJogo, 
                                  List<List<Integer>> historico, 
                                  List<List<Integer>> jogosGerados) {
            // Evita jogos muito similares aos históricos
            for (List<Integer> jogoHistorico : historico) {
                if (calcularSimilaridade(novoJogo, jogoHistorico) > 13) {
                    logger.debug("Jogo rejeitado por similaridade com histórico");
                    return false;
                }
            }
            
            // Evita jogos muito similares entre os gerados
            for (List<Integer> jogoGerado : jogosGerados) {
                if (calcularSimilaridade(novoJogo, jogoGerado) > 13) {
                    logger.debug("Jogo rejeitado por similaridade com outro jogo gerado");
                    return false;
                }
            }

            // Validação adicional de distribuição
            AnaliseEstatistica.ResultadoAnalise analise = analiseEstatistica.analisarJogo(novoJogo);
            if (analise.scoreQualidade() < 0.6) {
                logger.debug("Jogo rejeitado por score de qualidade baixo: {}", analise.scoreQualidade());
                return false;
            }
            
            return true;
        }

        private int calcularSimilaridade(List<Integer> jogo1, List<Integer> jogo2) {
            int similares = 0;
            for (int num : jogo1) {
                if (jogo2.contains(num)) {
                    similares++;
                }
            }
            return similares;
        }

        // Método para salvar os jogos gerados no banco de dados
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

        // Método para buscar as métricas de qualidade do jogo
        public Map<String, Double> getMetricasJogo(List<Integer> jogo) {
            Map<String, Double> metricas = new HashMap<>();
            AnaliseEstatistica.ResultadoAnalise analise = analiseEstatistica.analisarJogo(jogo);
            
            metricas.put("scoreQualidade", analise.scoreQualidade());
            metricas.put("percentualPares", (double) analise.qtPares() / TAMANHO_JOGO);
            metricas.put("scoreDistribuicao", calcularScoreDistribuicao(analise.distribuicaoDezenas()));
            
            return metricas;
        }

        private double calcularScoreDistribuicao(Map<String, Integer> distribuicao) {
            int d1 = distribuicao.getOrDefault("1-10", 0);
            int d2 = distribuicao.getOrDefault("11-20", 0);
            int d3 = distribuicao.getOrDefault("21-25", 0);
            
            // Calcula o desvio em relação à distribuição ideal (6-6-3)
            double desvioD1 = Math.abs(6 - d1);
            double desvioD2 = Math.abs(6 - d2);
            double desvioD3 = Math.abs(3 - d3);
            
            // Normaliza o score (0-1, onde 1 é a distribuição ideal)
            return 1.0 - ((desvioD1 + desvioD2 + desvioD3) / 15.0);
        }
        
        private void salvarPesosNumeros(Connection conn, int idLoteria, Map<Integer, Double> pesos) {
            String sql = """
                INSERT INTO tb_pesos_numeros 
                (id_loterias, num_analisado, peso, dt_calculo)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Map.Entry<Integer, Double> entry : pesos.entrySet()) {
                    stmt.setInt(1, idLoteria);
                    stmt.setInt(2, entry.getKey());      // número
                    stmt.setDouble(3, entry.getValue());  // peso calculado
                    stmt.addBatch();
                }
                stmt.executeBatch();
                logger.info("Pesos dos números salvos com sucesso para loteria {}", idLoteria);
            } catch (SQLException e) {
                logger.error("Erro ao salvar pesos dos números: ", e);
            }
        }
     // Adicione estes métodos à sua classe atual

        private void salvarAnaliseEstatistica(Connection conn, int idLoteria, List<List<Integer>> historico) {
            String sql = """
                INSERT INTO tb_analise_estatistica 
                (id_loterias, num_analisado, frequencia, recencia, media_posicao, dt_analise)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int numero = 1; numero <= NUMERO_MAXIMO; numero++) {
                    int frequencia = contarFrequencia(numero, historico);
                    double recencia = calcularRecenciaNormalizada(numero, historico);
                    double mediaPosicao = calcularMediaPosicao(numero, historico);

                    stmt.setInt(1, idLoteria);
                    stmt.setInt(2, numero);          // Alterado de "numero" para "num_analisado"
                    stmt.setInt(3, frequencia);
                    stmt.setDouble(4, recencia);
                    stmt.setDouble(5, mediaPosicao);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                logger.info("Análise estatística salva com sucesso para loteria {}", idLoteria);
            } catch (SQLException e) {
                logger.error("Erro ao salvar análise estatística: ", e);
            }
        }

        private void salvarMetricasQualidade(Connection conn, int idLoteria, List<List<Integer>> historico) {
            String sql = """
                INSERT INTO tb_metricas_qualidade 
                (id_loterias, tipo_metrica, valor, dt_calculo)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Calcula métricas de qualidade
                double diversidade = calcularDiversidade(historico);
                double estabilidade = calcularEstabilidade(historico);
                double dispersao = calcularDispersao(historico);

                // Salva cada métrica
                Object[][] metricas = {
                    {"DIVERSIDADE", diversidade},
                    {"ESTABILIDADE", estabilidade},
                    {"DISPERSAO", dispersao}
                };

                for (Object[] metrica : metricas) {
                    stmt.setInt(1, idLoteria);
                    stmt.setString(2, (String)metrica[0]);
                    stmt.setDouble(3, (Double)metrica[1]);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                logger.info("Métricas de qualidade salvas com sucesso para loteria {}", idLoteria);
            } catch (SQLException e) {
                logger.error("Erro ao salvar métricas de qualidade: ", e);
            }
        }

        private void salvarPadroes(Connection conn, int idLoteria, List<List<Integer>> historico) {
            String sql = """
                INSERT INTO tb_padroes_identificados 
                (id_loterias, nome_padrao, valor, dt_identificacao)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Identifica padrões
                Map<String, Double> padroes = new HashMap<>();
                padroes.put("PARES_IMPARES_RATIO", calcularMediaParesImpares(historico));
                padroes.put("MEDIA_SEQUENCIAS", calcularMediaSequencias(historico));
                padroes.put("MEDIA_SOMA", calcularMediaSoma(historico));

                // Salva cada padrão
                for (Map.Entry<String, Double> padrao : padroes.entrySet()) {
                    stmt.setInt(1, idLoteria);
                    stmt.setString(2, padrao.getKey());
                    stmt.setDouble(3, padrao.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                logger.info("Padrões identificados salvos com sucesso para loteria {}", idLoteria);
            } catch (SQLException e) {
                logger.error("Erro ao salvar padrões identificados: ", e);
            }
        }

        // Adicione este método ao início do método gerarJogos
        private void atualizarAnalises(int idLoterias, List<List<Integer>> historico) {
            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Verifica se já existem pesos salvos
                    if (!verificarPesosSalvos(conn, idLoterias)) {
                        // Calcula os pesos dos números apenas se não existirem
                        Map<Integer, Double> pesosNumeros = calcularPesosNumeros(historico);
                        salvarPesosNumeros(conn, idLoterias, pesosNumeros);
                    }
                    
                    // Atualiza as demais análises
                    salvarAnaliseEstatistica(conn, idLoterias, historico);
                    salvarMetricasQualidade(conn, idLoterias, historico);
                    salvarPadroes(conn, idLoterias, historico);
                    
                    conn.commit();
                    logger.info("Análises atualizadas com sucesso para loteria {}", idLoterias);
                } catch (SQLException e) {
                    conn.rollback();
                    logger.error("Erro ao atualizar análises. Realizando rollback: ", e);
                    throw e;
                }
            } catch (SQLException e) {
                logger.error("Erro na conexão com banco de dados: ", e);
            }
        }
     }