package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.model.Loteria;
import com.gestaoloteria.loteria.dao.LoteriaDAO;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tela criada em 22/06/2025 para o recurso "Gerar Jogos Inteligentes".
 * Permite seleção de loteria, concurso previsto, quantidade de dezenas (18/19/20),
 * escolha manual das dezenas e geração de jogos inteligentes.
 *
 * Ajuste em 22/06/2025: Adicionado StringConverter e cellFactory para exibir o nome da loteria na ComboBox.
 * Ajuste em 22/06/2025: Impede selecionar mais números do que o limite de dezenas permitido pelos radio buttons.
 */
public class GerarJogosInteligentesView extends Stage {

    private ComboBox<Loteria> loteriaCombo;
    private TextField concursoPrevistoField;
    private ToggleGroup dezenasGroup;
    private RadioButton rb18, rb19, rb20;
    private CheckBox[] dezenasCheckboxes;
    private TableView<List<Integer>> jogosTable;
    private Button gerarBtn, salvarBtn, voltarBtn;

    private List<List<Integer>> jogosGerados = new ArrayList<>();

    public GerarJogosInteligentesView() {
        setTitle("Gerar Jogos Inteligentes");

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));

        // Linha 1: Loteria e Concurso
        HBox linha1 = new HBox(12);
        linha1.setAlignment(Pos.CENTER_LEFT);

        loteriaCombo = new ComboBox<>();
        loteriaCombo.setPrefWidth(180);
        loteriaCombo.setItems(FXCollections.observableArrayList(carregarLoterias()));
        if (!loteriaCombo.getItems().isEmpty()) loteriaCombo.getSelectionModel().selectFirst();

        // Ajuste em 22/06/2025: exibir nome da loteria corretamente na ComboBox
        loteriaCombo.setConverter(new javafx.util.StringConverter<Loteria>() {
            @Override
            public String toString(Loteria loteria) {
                return (loteria == null) ? "" : loteria.getNome();
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

        Label lblLoteria = new Label("Loteria:");
        Label lblConcurso = new Label("Concurso Previsto:");
        concursoPrevistoField = new TextField();
        concursoPrevistoField.setPrefWidth(100);

        linha1.getChildren().addAll(lblLoteria, loteriaCombo, lblConcurso, concursoPrevistoField);

        // Linha 2: Opção de dezenas (18, 19, 20)
        HBox linha2 = new HBox(18);
        linha2.setAlignment(Pos.CENTER_LEFT);
        Label lblQtdDezenas = new Label("Qtd. Dezenas:");
        dezenasGroup = new ToggleGroup();
        rb18 = new RadioButton("18");
        rb19 = new RadioButton("19");
        rb20 = new RadioButton("20");
        rb18.setToggleGroup(dezenasGroup);
        rb19.setToggleGroup(dezenasGroup);
        rb20.setToggleGroup(dezenasGroup);
        rb18.setSelected(true);
        linha2.getChildren().addAll(lblQtdDezenas, rb18, rb19, rb20);

        // Linha 3: Grade de dezenas (checkboxes 1~25)
        GridPane dezenasGrid = new GridPane();
        dezenasGrid.setHgap(7);
        dezenasGrid.setVgap(7);
        dezenasGrid.setPadding(new Insets(7, 0, 7, 0));
        dezenasCheckboxes = new CheckBox[25];
        for (int i = 0; i < 25; i++) {
            dezenasCheckboxes[i] = new CheckBox(String.format("%02d", i + 1));
            dezenasCheckboxes[i].setPrefWidth(48);
            dezenasGrid.add(dezenasCheckboxes[i], i % 10, i / 10); // 10 colunas
        }

        // Ajuste em 22/06/2025: impedir selecionar mais do que o permitido
        for (CheckBox cb : dezenasCheckboxes) {
            cb.setOnAction(e -> atualizarSelecaoDezenas());
        }
        rb18.setOnAction(e -> atualizarSelecaoDezenas());
        rb19.setOnAction(e -> atualizarSelecaoDezenas());
        rb20.setOnAction(e -> atualizarSelecaoDezenas());

        // Linha 4: Botões
        HBox linhaBotoes = new HBox(14);
        linhaBotoes.setAlignment(Pos.CENTER_LEFT);
        gerarBtn = new Button("Gerar Jogos");
        salvarBtn = new Button("Salvar Jogos");
        voltarBtn = new Button("Voltar");
        salvarBtn.setDisable(true);
        linhaBotoes.getChildren().addAll(gerarBtn, salvarBtn, voltarBtn);

        // Tabela de jogos gerados
        jogosTable = new TableView<>();
        jogosTable.setPrefHeight(170);
        for (int i = 1; i <= 15; i++) {
            final int idx = i - 1;
            TableColumn<List<Integer>, String> col = new TableColumn<>("D" + i);
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                    data.getValue().size() > idx ? String.format("%02d", data.getValue().get(idx)) : ""));
            col.setPrefWidth(36);
            jogosTable.getColumns().add(col);
        }

        // Eventos dos botões
        gerarBtn.setOnAction(e -> gerarJogos());
        salvarBtn.setOnAction(e -> salvarJogos());
        voltarBtn.setOnAction(e -> this.close());

        root.getChildren().addAll(linha1, linha2, dezenasGrid, linhaBotoes, jogosTable);

        setScene(new Scene(root, 700, 470));
        initModality(Modality.APPLICATION_MODAL);

        atualizarSelecaoDezenas(); // inicializa bloqueio correto conforme radio selecionado
    }

    private void atualizarSelecaoDezenas() {
        int maxSelecionadas = rb18.isSelected() ? 18 : rb19.isSelected() ? 19 : 20;
        int selecionadas = 0;
        for (CheckBox cb : dezenasCheckboxes) if (cb.isSelected()) selecionadas++;
        boolean bloquear = selecionadas >= maxSelecionadas;
        for (CheckBox cb : dezenasCheckboxes) {
            if (!cb.isSelected()) {
                cb.setDisable(bloquear);
            } else {
                cb.setDisable(false); // nunca desabilita uma já marcada
            }
        }
    }

    private List<Loteria> carregarLoterias() {
        try {
            return new LoteriaDAO().listarLoterias();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void gerarJogos() {
        jogosGerados.clear();
        jogosTable.getItems().clear();

        int qtdDezenas = rb18.isSelected() ? 18 : rb19.isSelected() ? 19 : 20;
        List<Integer> dezenasSelecionadas = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            if (dezenasCheckboxes[i].isSelected()) dezenasSelecionadas.add(i + 1);
        }

        if (dezenasSelecionadas.size() != qtdDezenas) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "Selecione exatamente " + qtdDezenas + " dezenas.", ButtonType.OK);
            alerta.setHeaderText("Atenção");
            alerta.showAndWait();
            return;
        }

        // Lógica de geração de jogos inteligentes será implementada aqui
        // Por enquanto, simula 5 jogos aleatórios como exemplo
        Random rand = new Random();
        for (int j = 0; j < 5; j++) {
            List<Integer> jogo = dezenasSelecionadas.stream().collect(Collectors.toList());
            Collections.shuffle(jogo, rand);
            List<Integer> jogo15 = jogo.subList(0, 15).stream().sorted().collect(Collectors.toList());
            jogosGerados.add(jogo15);
        }
        jogosTable.setItems(FXCollections.observableArrayList(jogosGerados));
        salvarBtn.setDisable(jogosGerados.isEmpty());
    }

    private void salvarJogos() {
        // TODO: implementar persistência dos jogos gerados
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, "Jogos salvos com sucesso! (Simulação)", ButtonType.OK);
        alerta.setHeaderText("Salvar Jogos");
        alerta.showAndWait();
    }
}