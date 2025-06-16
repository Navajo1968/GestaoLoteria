package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.JogoDAO;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.model.Jogo;
import com.gestaoloteria.loteria.model.Loteria;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GerarJogosView extends Stage {

    private ComboBox<Loteria> loteriaCombo;
    private TextField qtdJogosField;
    private TextField concursoPrevistoField;
    private TableView<List<Integer>> jogosTable;
    private List<List<Integer>> jogosSugeridos = new ArrayList<>();
    private Button gerarBtn, salvarBtn;

    public GerarJogosView() {
        setTitle("Gerar Jogos Sugeridos");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        loteriaCombo = new ComboBox<>();
        qtdJogosField = new TextField();
        concursoPrevistoField = new TextField();

        // Carrega apenas Lotofacil
        List<Loteria> loterias = carregarLoterias();
        loteriaCombo.setItems(FXCollections.observableArrayList(loterias));
        if (!loterias.isEmpty()) loteriaCombo.getSelectionModel().selectFirst();

        HBox linha1 = new HBox(10, new Label("Loteria:"), loteriaCombo, new Label("Nº jogos:"), qtdJogosField, new Label("Concurso previsto:"), concursoPrevistoField);
        linha1.setPadding(new Insets(0,0,10,0));

        jogosTable = new TableView<>();
        for (int i = 1; i <= 15; i++) {
            final int idx = i - 1;
            TableColumn<List<Integer>, String> col = new TableColumn<>("D" + i);
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%02d", data.getValue().get(idx))));
            jogosTable.getColumns().add(col);
        }

        gerarBtn = new Button("Gerar Sugestões");
        gerarBtn.setOnAction(e -> gerarSugestoes());

        salvarBtn = new Button("Salvar Jogos");
        salvarBtn.setDisable(true);
        salvarBtn.setOnAction(e -> salvarJogos());

        HBox botoes = new HBox(10, gerarBtn, salvarBtn);
        botoes.setPadding(new Insets(10,0,0,0));

        root.getChildren().addAll(linha1, jogosTable, botoes);

        setScene(new Scene(root, 720, 400));
        initModality(Modality.APPLICATION_MODAL);
    }

    private List<Loteria> carregarLoterias() {
        try {
            // Filtra para Lotofacil (por nome)
            return new LoteriaDAO().listarLoterias().stream()
                    .filter(l -> l.getNome().equalsIgnoreCase("Lotofácil"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void gerarSugestoes() {
        jogosSugeridos.clear();
        jogosTable.getItems().clear();
        int qtd;
        try {
            qtd = Integer.parseInt(qtdJogosField.getText().trim());
            if (qtd < 1 || qtd > 100) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Informe a quantidade de jogos (1 a 100)");
            return;
        }
        if (loteriaCombo.getValue() == null) {
            showAlert("Selecione a loteria");
            return;
        }
        // Geração baseada em estatística/probabilidade de Lotofacil
        for (int i = 0; i < qtd; i++) {
            jogosSugeridos.add(gerarJogoMelhorProbabilidade());
        }
        jogosTable.setItems(FXCollections.observableArrayList(jogosSugeridos));
        salvarBtn.setDisable(false);
    }

    private List<Integer> gerarJogoMelhorProbabilidade() {
        // Estratégia estatística: dezenas mais frequentes, distribuição par/ímpar, evita repetições exatas dos últimos concursos etc.
        // Para Lotofacil: 15 dezenas entre 1 e 25. Exemplo simples usando aleatório e combinando frequentes:
        List<Integer> todas = new ArrayList<>();
        for (int i = 1; i <= 25; i++) todas.add(i);

        Collections.shuffle(todas, new Random());
        return todas.subList(0, 15).stream().sorted().collect(Collectors.toList());
        // Para produção, use um algoritmo avançado de estatística/histórico, se desejar.
    }

    private void salvarJogos() {
        if (jogosSugeridos.isEmpty()) return;
        Loteria loteria = loteriaCombo.getValue();
        Integer numeroConcursoPrevisto;
        try {
            numeroConcursoPrevisto = Integer.parseInt(concursoPrevistoField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Informe o número do concurso previsto!");
            return;
        }
        List<Jogo> jogos = new ArrayList<>();
        for (List<Integer> lista : jogosSugeridos) {
            String nums = lista.stream().map(n -> String.format("%02d", n)).collect(Collectors.joining(","));
            jogos.add(new Jogo(
                    loteria.getId(),
                    null, // concursoId, ainda não existe
                    numeroConcursoPrevisto,
                    nums,
                    LocalDateTime.now(),
                    null,
                    null
            ));
        }
        try {
            new JogoDAO().salvarJogos(jogos);
            showAlert("Jogos salvos com sucesso!");
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro ao salvar jogos: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}