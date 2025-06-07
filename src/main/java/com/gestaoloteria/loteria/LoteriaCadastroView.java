package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.model.Loteria;
import com.gestaoloteria.loteria.model.FaixaPremiacao;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class LoteriaCadastroView {

    private VBox root;
    private TextField nomeField, descricaoField, qtdMinField, qtdMaxField, qtdSorteadosField;
    private ObservableList<FaixaPremiacaoRow> faixasList;
    private TableView<FaixaPremiacaoRow> faixasTable;

    public LoteriaCadastroView() {
        root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #23395d 0%, #4069a3 100%);");

        Label title = new Label("Cadastro de Loteria");
        title.setFont(Font.font("Segoe UI", 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(6, Color.BLACK));
        title.setTextAlignment(TextAlignment.CENTER);

        // Loteria Fields
        GridPane loteriaPane = new GridPane();
        loteriaPane.setHgap(10);
        loteriaPane.setVgap(10);
        loteriaPane.setAlignment(Pos.CENTER);
        loteriaPane.setPadding(new Insets(10));

        nomeField = new TextField();
        descricaoField = new TextField();
        qtdMinField = new TextField();
        qtdMaxField = new TextField();
        qtdSorteadosField = new TextField();

        loteriaPane.add(new Label("Nome:"), 0, 0); loteriaPane.add(nomeField, 1, 0);
        loteriaPane.add(new Label("Descrição:"), 0, 1); loteriaPane.add(descricaoField, 1, 1);
        loteriaPane.add(new Label("Qtd. mín. números aposta:"), 0, 2); loteriaPane.add(qtdMinField, 1, 2);
        loteriaPane.add(new Label("Qtd. máx. números aposta:"), 0, 3); loteriaPane.add(qtdMaxField, 1, 3);
        loteriaPane.add(new Label("Qtd. números sorteados:"), 0, 4); loteriaPane.add(qtdSorteadosField, 1, 4);

        // Faixas de Premiação
        Label faixasLabel = new Label("Faixas de Premiação");
        faixasLabel.setFont(Font.font("Segoe UI", 18));
        faixasLabel.setTextFill(Color.WHITE);

        faixasList = FXCollections.observableArrayList();
        faixasTable = new TableView<>(faixasList);
        faixasTable.setEditable(true);
        faixasTable.setPrefHeight(180);

        TableColumn<FaixaPremiacaoRow, String> nomeFaixaCol = new TableColumn<>("Nome");
        nomeFaixaCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        nomeFaixaCol.setPrefWidth(160);

        TableColumn<FaixaPremiacaoRow, Integer> acertosCol = new TableColumn<>("Acertos");
        acertosCol.setCellValueFactory(new PropertyValueFactory<>("acertos"));
        acertosCol.setPrefWidth(90);

        TableColumn<FaixaPremiacaoRow, Integer> ordemCol = new TableColumn<>("Ordem");
        ordemCol.setCellValueFactory(new PropertyValueFactory<>("ordem"));
        ordemCol.setPrefWidth(80);

        TableColumn<FaixaPremiacaoRow, Void> removeCol = new TableColumn<>("Remover");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("X");
            {
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    FaixaPremiacaoRow item = getTableView().getItems().get(getIndex());
                    faixasList.remove(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
        removeCol.setPrefWidth(80);

        faixasTable.getColumns().addAll(nomeFaixaCol, acertosCol, ordemCol, removeCol);

        // Add faixa controls
        HBox addFaixaBox = new HBox(10);
        addFaixaBox.setAlignment(Pos.CENTER);
        TextField nomeFaixaField = new TextField();
        nomeFaixaField.setPromptText("Nome da Faixa");
        TextField acertosField = new TextField();
        acertosField.setPromptText("Acertos");
        TextField ordemField = new TextField();
        ordemField.setPromptText("Ordem");
        Button addFaixaBtn = new Button("Adicionar Faixa");
        addFaixaBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        addFaixaBtn.setOnAction(e -> {
            String nome = nomeFaixaField.getText().trim();
            String acertosTxt = acertosField.getText().trim();
            String ordemTxt = ordemField.getText().trim();
            if (!nome.isEmpty() && !acertosTxt.isEmpty() && !ordemTxt.isEmpty()) {
                try {
                    int acertos = Integer.parseInt(acertosTxt);
                    int ordem = Integer.parseInt(ordemTxt);
                    faixasList.add(new FaixaPremiacaoRow(nome, acertos, ordem));
                    nomeFaixaField.clear(); acertosField.clear(); ordemField.clear();
                } catch (NumberFormatException ex) {
                    showAlert("Atenção", "Acertos e Ordem devem ser números inteiros.");
                }
            } else {
                showAlert("Atenção", "Preencha todos os campos da faixa!");
            }
        });
        addFaixaBox.getChildren().addAll(nomeFaixaField, acertosField, ordemField, addFaixaBtn);

        // Footer buttons
        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button salvarBtn = new Button("Salvar");
        salvarBtn.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.setOnAction(e -> Main.showLoteriaCadastro()); // Volta pro menu de Loterias
        footer.getChildren().addAll(salvarBtn, cancelarBtn);

        salvarBtn.setOnAction(e -> salvarCadastro());

        // Monta layout
        VBox faixasBox = new VBox(8, faixasLabel, faixasTable, addFaixaBox);
        faixasBox.setAlignment(Pos.CENTER);
        faixasBox.setPadding(new Insets(10, 0, 0, 0));
        root.getChildren().addAll(title, loteriaPane, faixasBox, footer);
    }

    private void salvarCadastro() {
        // Validação
        String nome = nomeField.getText().trim();
        String descricao = descricaoField.getText().trim();
        String minTxt = qtdMinField.getText().trim();
        String maxTxt = qtdMaxField.getText().trim();
        String sorteadosTxt = qtdSorteadosField.getText().trim();

        if (nome.isEmpty() || descricao.isEmpty() || minTxt.isEmpty() || maxTxt.isEmpty() || sorteadosTxt.isEmpty() || faixasList.isEmpty()) {
            showAlert("Atenção", "Preencha todos os campos da loteria e adicione ao menos uma faixa de premiação!");
            return;
        }

        try {
            int qtdMin = Integer.parseInt(minTxt);
            int qtdMax = Integer.parseInt(maxTxt);
            int qtdSorteados = Integer.parseInt(sorteadosTxt);

            List<FaixaPremiacao> faixas = new ArrayList<>();
            for (FaixaPremiacaoRow row : faixasList) {
                FaixaPremiacao f = new FaixaPremiacao();
                f.setNome(row.getNome());
                f.setAcertos(row.getAcertos());
                f.setOrdem(row.getOrdem());
                faixas.add(f);
            }
            Loteria loteria = new Loteria();
            loteria.setNome(nome);
            loteria.setDescricao(descricao);
            loteria.setQtdMin(qtdMin);
            loteria.setQtdMax(qtdMax);
            loteria.setQtdSorteados(qtdSorteados);
            loteria.setFaixas(faixas);

            LoteriaDAO dao = new LoteriaDAO();
            dao.salvarLoteriaComFaixas(loteria);

            showAlert("Salvo", "Cadastro salvo com sucesso!");
            // Limpa campos
            nomeField.clear(); descricaoField.clear();
            qtdMinField.clear(); qtdMaxField.clear(); qtdSorteadosField.clear();
            faixasList.clear();
        } catch (NumberFormatException ex) {
            showAlert("Atenção", "Qtd. mín/máx/sorteados devem ser números inteiros.");
        } catch (Exception ex) {
            showAlert("Erro", "Erro ao salvar no banco: " + ex.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }

    // Classe auxiliar para a TableView
    public static class FaixaPremiacaoRow {
        private final String nome;
        private final int acertos;
        private final int ordem;

        public FaixaPremiacaoRow(String nome, int acertos, int ordem) {
            this.nome = nome;
            this.acertos = acertos;
            this.ordem = ordem;
        }
        public String getNome() { return nome; }
        public int getAcertos() { return acertos; }
        public int getOrdem() { return ordem; }
    }
}