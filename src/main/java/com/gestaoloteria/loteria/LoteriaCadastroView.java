package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.model.FaixaPremiacao;
import com.gestaoloteria.loteria.model.Loteria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class LoteriaCadastroView {

    private final VBox root;
    private final TextField nomeField, descricaoField, qtdMinField, qtdMaxField, qtdSorteadosField;
    private final ObservableList<FaixaPremiacaoRow> faixasList;
    private final TableView<FaixaPremiacaoRow> faixasTable;
    private final LoteriaListaView parentView;
    private final Loteria editingLoteria;

    public LoteriaCadastroView(Loteria loteria, LoteriaListaView parentView) {
        this.editingLoteria = loteria;
        this.parentView = parentView;
        root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #23395d 0%, #4069a3 100%);");

        Label title = new Label(loteria == null ? "Nova Loteria" : "Editar Loteria");
        title.setFont(Font.font("Segoe UI", 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(6, Color.BLACK));

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        form.setAlignment(Pos.CENTER_LEFT);

        nomeField = new TextField();
        descricaoField = new TextField();
        qtdMinField = new TextField();
        qtdMaxField = new TextField();
        qtdSorteadosField = new TextField();

        form.add(new Label("Nome:"), 0, 0); form.add(nomeField, 1, 0);
        form.add(new Label("Descrição:"), 0, 1); form.add(descricaoField, 1, 1);
        form.add(new Label("Qtd. mín. números aposta:"), 0, 2); form.add(qtdMinField, 1, 2);
        form.add(new Label("Qtd. máx. números aposta:"), 0, 3); form.add(qtdMaxField, 1, 3);
        form.add(new Label("Qtd. números sorteados:"), 0, 4); form.add(qtdSorteadosField, 1, 4);

        // Faixas de Premiação
        Label faixasLabel = new Label("Faixas de Premiação");
        faixasLabel.setFont(Font.font("Segoe UI", 18));
        faixasLabel.setTextFill(Color.WHITE);

        faixasList = FXCollections.observableArrayList();
        faixasTable = new TableView<>(faixasList);
        faixasTable.setEditable(true);
        faixasTable.setPrefHeight(120);

        TableColumn<FaixaPremiacaoRow, String> nomeFaixaCol = new TableColumn<>("Nome");
        nomeFaixaCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        nomeFaixaCol.setPrefWidth(140);

        TableColumn<FaixaPremiacaoRow, Integer> acertosCol = new TableColumn<>("Acertos");
        acertosCol.setCellValueFactory(new PropertyValueFactory<>("acertos"));
        acertosCol.setPrefWidth(80);

        TableColumn<FaixaPremiacaoRow, Integer> ordemCol = new TableColumn<>("Ordem");
        ordemCol.setCellValueFactory(new PropertyValueFactory<>("ordem"));
        ordemCol.setPrefWidth(80);

        TableColumn<FaixaPremiacaoRow, Void> removeCol = new TableColumn<>("Remover");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("X");
            {
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
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
        removeCol.setPrefWidth(70);

        faixasTable.getColumns().addAll(nomeFaixaCol, acertosCol, ordemCol, removeCol);

        // Add faixa controls
        HBox addFaixaBox = new HBox(10);
        addFaixaBox.setAlignment(Pos.CENTER_LEFT);
        TextField nomeFaixaField = new TextField();
        nomeFaixaField.setPromptText("Nome da Faixa");
        TextField acertosField = new TextField();
        acertosField.setPromptText("Acertos");
        TextField ordemField = new TextField();
        ordemField.setPromptText("Ordem");
        Button addFaixaBtn = new Button("Adicionar");
        addFaixaBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
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
        Button voltarBtn = new Button("Voltar");
        voltarBtn.setOnAction(e -> Main.showLoteriaLista());

        footer.getChildren().addAll(salvarBtn, voltarBtn);

        salvarBtn.setOnAction(e -> salvarOuAtualizar());

        VBox faixasBox = new VBox(8, faixasLabel, faixasTable, addFaixaBox);
        faixasBox.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, form, faixasBox, footer);

        if (loteria != null) preencherCampos(loteria);
    }

    private void preencherCampos(Loteria loteria) {
        nomeField.setText(loteria.getNome());
        descricaoField.setText(loteria.getDescricao());
        qtdMinField.setText(String.valueOf(loteria.getQtdMin()));
        qtdMaxField.setText(String.valueOf(loteria.getQtdMax()));
        qtdSorteadosField.setText(String.valueOf(loteria.getQtdSorteados()));
        faixasList.clear();
        if (loteria.getFaixas() != null)
            for (FaixaPremiacao f : loteria.getFaixas())
                faixasList.add(new FaixaPremiacaoRow(f.getNome(), f.getAcertos(), f.getOrdem()));
    }

    private void salvarOuAtualizar() {
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
            if (editingLoteria != null)
                loteria.setId(editingLoteria.getId());
            loteria.setNome(nome);
            loteria.setDescricao(descricao);
            loteria.setQtdMin(qtdMin);
            loteria.setQtdMax(qtdMax);
            loteria.setQtdSorteados(qtdSorteados);
            loteria.setFaixas(faixas);

            LoteriaDAO dao = new LoteriaDAO();
            if (editingLoteria != null)
                dao.atualizarLoteriaComFaixas(loteria);
            else
                dao.salvarLoteriaComFaixas(loteria);

            showAlert("Sucesso", "Cadastro salvo com sucesso!");
            Main.showLoteriaLista();
            if (parentView != null) parentView.atualizarTabela();
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