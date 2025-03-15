package com.loteriascorp.service;

import com.loteriascorp.Database;
import com.loteriascorp.Jogo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@Service
public class JogoService {

    private static final Logger logger = LoggerFactory.getLogger(JogoService.class);
    private Classifier classifier;

    public JogoService() {
        // Inicializar o classificador de machine learning
        this.classifier = new RandomForest();
        treinarModelo();
    }

    @Scheduled(cron = "0 15 22 * * *") // Agendado para rodar todos os dias às 22:15
    public void treinarModelo() {
        logger.info("Iniciando o treinamento do modelo de Machine Learning...");
        try {
            // Carregar os dados históricos
            DataSource source = new DataSource("path/to/historical/data.arff");
            Instances dataset = source.getDataSet();
            dataset.setClassIndex(dataset.numAttributes() - 1); // Definir o atributo de classe (último atributo)

            // Treinar o modelo com validação cruzada
            Evaluation evaluation = new Evaluation(dataset);
            evaluation.crossValidateModel(classifier, dataset, 10, new Random(1));
            classifier.buildClassifier(dataset);

            logger.info("Modelo treinado com sucesso com validação cruzada.");
            logger.info("Avaliação do modelo: " + evaluation.toSummaryString());
        } catch (Exception e) {
            logger.error("Erro ao treinar o modelo de Machine Learning", e);
        }
    }

    public List<Jogo> gerarJogos(int idLoteria, int numeroConcurso, int quantidadeJogos) {
        List<Integer> numerosMaisFrequentes = calcularNumerosMaisFrequentes(idLoteria);
        List<Integer> numerosPrevistos = preverNumeros(numerosMaisFrequentes);
        return criarJogos(numeroConcurso, quantidadeJogos, numerosPrevistos);
    }

    private List<Integer> calcularNumerosMaisFrequentes(int idLoteria) {
        Map<Integer, Integer> frequenciaNumeros = new HashMap<>();

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, num11, num12, num13, num14, num15 " +
                         "FROM tb_historico_jogos WHERE id_loterias = " + idLoteria;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    for (int i = 1; i <= 15; i++) {
                        int numero = rs.getInt("num" + i);
                        frequenciaNumeros.put(numero, frequenciaNumeros.getOrDefault(numero, 0) + 1);
                    }
                }
            }
            logger.info("Cálculo dos números mais frequentes concluído.");
        } catch (Exception e) {
            logger.error("Erro ao calcular os números mais frequentes", e);
        }

        return frequenciaNumeros.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(15)
            .map(Map.Entry::getKey)
            .toList();
    }

    private List<Integer> preverNumeros(List<Integer> numerosMaisFrequentes) {
        List<Integer> numerosPrevistos = new ArrayList<>();

        try {
            // Criar instância para previsão
            DataSource source = new DataSource("path/to/historical/data.arff");
            Instances dataset = source.getDataSet();
            dataset.setClassIndex(dataset.numAttributes() - 1);

            for (Instance instance : dataset) {
                double predicao = classifier.classifyInstance(instance);
                numerosPrevistos.add((int) predicao);
            }
            logger.info("Previsão dos números concluída.");
        } catch (Exception e) {
            logger.error("Erro ao prever os números", e);
        }

        return numerosPrevistos;
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
    
    public void acionarSimulacaoApostas(int idLoteria, int quantidadeJogos) {
        SimuladorApostas simulador = new SimuladorApostas();
        simulador.simularApostas(idLoteria, quantidadeJogos);
    }
}