package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.ConcursoDAO;
import com.gestaoloteria.loteria.dao.JogoDAO;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.model.Concurso;
import com.gestaoloteria.loteria.model.Jogo;
import com.gestaoloteria.loteria.model.Loteria;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class AnaliseResultadosView extends Stage {

    private ComboBox<Loteria> loteriaCombo;
    private BarChart<String, Number> barChart;

    public AnaliseResultadosView() {
        setTitle("Análise Estatística dos Jogos Sugeridos");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        loteriaCombo = new ComboBox<>();
        carregarLoterias();

        loteriaCombo.setOnAction(e -> atualizarGrafico());

        // Eixo das categorias (número de acertos) e eixo dos valores (número de jogos)
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Número de Acertos");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Quantidade de Jogos");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Distribuição de Acertos");

        Button voltarBtn = new Button("Voltar");
        voltarBtn.setOnAction(e -> this.close());

        root.getChildren().addAll(new Label("Selecione a Loteria:"), loteriaCombo, barChart, voltarBtn);

        setScene(new Scene(root, 700, 500));
        initModality(Modality.APPLICATION_MODAL);
    }

    private void carregarLoterias() {
        try {
            List<Loteria> loterias = new LoteriaDAO().listarLoterias();
            loteriaCombo.setItems(FXCollections.observableArrayList(loterias));
            if (!loterias.isEmpty()) {
                loteriaCombo.getSelectionModel().selectFirst();
                atualizarGrafico();
            }
        } catch (Exception e) {
            showAlert("Erro ao carregar loterias: " + e.getMessage());
        }
    }

    private void atualizarGrafico() {
        try {
            Loteria loteria = loteriaCombo.getValue();
            if (loteria == null) return;

            // Busca jogos conferidos para esta loteria
            List<Concurso> concursos = new ConcursoDAO().listarConcursosPorLoteria(loteria.getId());
            List<Integer> concursoIds = concursos.stream().map(Concurso::getId).collect(Collectors.toList());
            List<Jogo> jogos = new JogoDAO().buscarJogosConferidosPorConcursos(concursoIds);

            // Distribuição de acertos
            Map<Integer, Long> distAcertos = jogos.stream()
                    .filter(j -> j.getAcertos() != null)
                    .collect(Collectors.groupingBy(Jogo::getAcertos, Collectors.counting()));

            // Gera o gráfico
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (int i = 0; i <= 15; i++) {
                long qtd = distAcertos.getOrDefault(i, 0L);
                series.getData().add(new XYChart.Data<>(String.valueOf(i), qtd));
            }

            barChart.getData().clear();
            barChart.getData().add(series);

            // Sugestão matemática/estatística
            StringBuilder sugestoes = new StringBuilder();
            sugestoes.append("\nSugestão de melhoria:\n");

            long total = jogos.size();
            long premiados = distAcertos.entrySet().stream().filter(e -> e.getKey() >= 11).mapToLong(Map.Entry::getValue).sum();
            sugestoes.append(String.format("Total de jogos conferidos: %d\n", total));
            sugestoes.append(String.format("Jogos premiados (>=11): %d (%.2f%%)\n", premiados, total == 0 ? 0.0 : 100.0 * premiados / total));

            TextArea sugestaoArea = new TextArea(sugestoes.toString());
            sugestaoArea.setEditable(false);
            getRoot().addListener((obs, oldRoot, newRoot) -> {
                if (newRoot != null && !newRoot.getChildrenUnmodifiable().contains(sugestaoArea)) {
                    ((VBox)newRoot).getChildren().add(sugestaoArea);
                }
            });
        } catch (Exception e) {
            showAlert("Erro ao carregar análise: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
    public Parent getRoot() {
        return this.root; // substitua por seu painel raiz real
    }
}