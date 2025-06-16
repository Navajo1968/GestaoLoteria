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
import javafx.util.StringConverter;

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

        // Carrega todas as loterias
        List<Loteria> loterias = carregarLoterias();
        loteriaCombo.setItems(FXCollections.observableArrayList(loterias));
        if (!loterias.isEmpty()) loteriaCombo.getSelectionModel().selectFirst();

        // Ajuste para exibir o nome na ComboBox
        loteriaCombo.setConverter(new StringConverter<Loteria>() {
            @Override
            public String toString(Loteria object) {
                return (object != null) ? object.getNome() : "";
            }
            @Override
            public Loteria fromString(String string) {
                for (Loteria l : loteriaCombo.getItems()) {
                    if (l.getNome().equals(string)) return l;
                }
                return null;
            }
        });

        loteriaCombo.setCellFactory(listView -> new ListCell<Loteria>() {
            @Override
            protected void updateItem(Loteria item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? "" : item.getNome());
            }
        });

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
            // Retorna todas as loterias cadastradas. Se quiser filtrar, basta ajustar aqui.
            return new LoteriaDAO().listarLoterias();
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
        for (int i = 0; i < qtd; i++) {
            jogosSugeridos.add(gerarJogoMelhorProbabilidade());
        }
        jogosTable.setItems(FXCollections.observableArrayList(jogosSugeridos));
        salvarBtn.setDisable(false);
    }

    private List<Integer> gerarJogoMelhorProbabilidade() {
        List<Integer> todas = new ArrayList<>();
        for (int i = 1; i <= 25; i++) todas.add(i);
        Collections.shuffle(todas, new Random());
        return todas.subList(0, 15).stream().sorted().collect(Collectors.toList());
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