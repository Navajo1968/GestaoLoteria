package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.model.Loteria;
import com.gestaoloteria.loteria.model.FaixaPremiacao;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
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
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoteriaCadastroView {

    private VBox root;
    private TextField nomeField, descricaoField, qtdMinField, qtdMaxField, qtdSorteadosField;
    private ObservableList<FaixaPremiacaoRow> faixasList;
    private TableView<FaixaPremiacaoRow> faixasTable;
    private TableView<LoteriaRow> loteriasTable;
    private ObservableList<LoteriaRow> loteriasList;
    private Button salvarBtn, cancelarBtn, novoBtn;
    private Integer editandoId = null;

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
        faixasTable.setPrefHeight(150);

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
        addFaixaBox.setAlignment(Pos.CENTER_LEFT);
        TextField nomeFaixaField = new TextField();
        nomeFaixaField.setPromptText("Nome da Faixa");
        TextField acertosField = new TextField();
        acertosField.setPromptText("Acertos");
        TextField ordemField = new TextField();
        ordemField.setPromptText("Ordem");
        Button addFaixaBtn = new Button("Adicionar");
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

        // CRUD TableView de Loterias
        loteriasList = FXCollections.observableArrayList();
        loteriasTable = new TableView<>(loteriasList);
        loteriasTable.setPrefHeight(180);

        TableColumn<LoteriaRow, String> nomeCol = new TableColumn<>("Loteria");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        nomeCol.setPrefWidth(150);

        TableColumn<LoteriaRow, String> descCol = new TableColumn<>("Descrição");
        descCol.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        descCol.setPrefWidth(150);

        TableColumn<LoteriaRow, String> editarCol = new TableColumn<>("Editar");
        editarCol.setCellFactory(col -> new TableCell<>() {
            private final Button editarBtn = new Button("Editar");
            {
                editarBtn.setOnAction(e -> {
                    LoteriaRow row = getTableView().getItems().get(getIndex());
                    carregarParaEdicao(row.getId());
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editarBtn);
            }
        });
        editarCol.setPrefWidth(70);

        TableColumn<LoteriaRow, String> excluirCol = new TableColumn<>("Excluir");
        excluirCol.setCellFactory(col -> new TableCell<>() {
            private final Button excluirBtn = new Button("Excluir");
            {
                excluirBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                excluirBtn.setOnAction(e -> {
                    LoteriaRow row = getTableView().getItems().get(getIndex());
                    excluirLoteria(row.getId());
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : excluirBtn);
            }
        });
        excluirCol.setPrefWidth(70);

        loteriasTable.getColumns().addAll(nomeCol, descCol, editarCol, excluirCol);

        // Footer buttons
        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_RIGHT);
        salvarBtn = new Button("Salvar");
        salvarBtn.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelarBtn = new Button("Cancelar");
        cancelarBtn.setOnAction(e -> limparCampos());
        novoBtn = new Button("Novo");
        novoBtn.setOnAction(e -> limparCampos());
        footer.getChildren().addAll(salvarBtn, cancelarBtn, novoBtn);

        salvarBtn.setOnAction(e -> {
            if (editandoId == null) salvarCadastro();
            else atualizarCadastro();
        });

        VBox faixasBox = new VBox(8, faixasLabel, faixasTable, addFaixaBox);
        faixasBox.setAlignment(Pos.CENTER_LEFT);
        faixasBox.setPadding(new Insets(10, 0, 0, 0));
        VBox formulario = new VBox(10, loteriaPane, faixasBox, footer);
        formulario.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 10;");
        formulario.setPadding(new Insets(15));
        formulario.setAlignment(Pos.CENTER);

        Label listaLabel = new Label("Loterias Cadastradas");
        listaLabel.setFont(Font.font("Segoe UI", 17));
        listaLabel.setTextFill(Color.WHITE);

        root.getChildren().addAll(title, formulario, listaLabel, loteriasTable);
        atualizarLista();
    }

    // CRUD - salvar novo
    private void salvarCadastro() {
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
            limparCampos();
            atualizarLista();
        } catch (NumberFormatException ex) {
            showAlert("Atenção", "Qtd. mín/máx/sorteados devem ser números inteiros.");
        } catch (Exception ex) {
            showAlert("Erro", "Erro ao salvar no banco: " + ex.getMessage());
        }
    }

    // CRUD - atualizar
    private void atualizarCadastro() {
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
            loteria.setId(editandoId);
            loteria.setNome(nome);
            loteria.setDescricao(descricao);
            loteria.setQtdMin(qtdMin);
            loteria.setQtdMax(qtdMax);
            loteria.setQtdSorteados(qtdSorteados);
            loteria.setFaixas(faixas);

            LoteriaDAO dao = new LoteriaDAO();
            dao.atualizarLoteriaComFaixas(loteria);

            showAlert("Atualizado", "Cadastro atualizado com sucesso!");
            limparCampos();
            atualizarLista();
        } catch (NumberFormatException ex) {
            showAlert("Atenção", "Qtd. mín/máx/sorteados devem ser números inteiros.");
        } catch (Exception ex) {
            showAlert("Erro", "Erro ao atualizar no banco: " + ex.getMessage());
        }
    }

    // CRUD - carregar para edição
    private void carregarParaEdicao(int loteriaId) {
        try {
            LoteriaDAO dao = new LoteriaDAO();
            for (Loteria lot : dao.listarLoterias()) {
                if (lot.getId() == loteriaId) {
                    editandoId = lot.getId();
                    nomeField.setText(lot.getNome());
                    descricaoField.setText(lot.getDescricao());
                    qtdMinField.setText(lot.getQtdMin().toString());
                    qtdMaxField.setText(lot.getQtdMax().toString());
                    qtdSorteadosField.setText(lot.getQtdSorteados().toString());
                    faixasList.clear();
                    for (FaixaPremiacao f : lot.getFaixas()) {
                        faixasList.add(new FaixaPremiacaoRow(f.getNome(), f.getAcertos(), f.getOrdem()));
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            showAlert("Erro", "Erro ao buscar loteria: " + ex.getMessage());
        }
    }

    // CRUD - excluir
    private void excluirLoteria(int loteriaId) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Deseja excluir esta loteria e todas as faixas?", ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirma exclusão");
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                LoteriaDAO dao = new LoteriaDAO();
                dao.excluirLoteria(loteriaId);
                showAlert("Excluído", "Loteria excluída com sucesso!");
                atualizarLista();
                limparCampos();
            } catch (Exception ex) {
                showAlert("Erro", "Erro ao excluir: " + ex.getMessage());
            }
        }
    }

    // Atualiza tabela de listagem
    private void atualizarLista() {
        loteriasList.clear();
        try {
            LoteriaDAO dao = new LoteriaDAO();
            for (Loteria l : dao.listarLoterias()) {
                loteriasList.add(new LoteriaRow(l.getId(), l.getNome(), l.getDescricao()));
            }
        } catch (Exception ex) {
            showAlert("Erro", "Erro ao buscar loterias: " + ex.getMessage());
        }
    }

    // Limpa formulário
    private void limparCampos() {
        editandoId = null;
        nomeField.clear();
        descricaoField.clear();
        qtdMinField.clear();
        qtdMaxField.clear();
        qtdSorteadosField.clear();
        faixasList.clear();
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

    // Auxiliares para TableView
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

    public static class LoteriaRow {
        private final int id;
        private final SimpleStringProperty nome;
        private final SimpleStringProperty descricao;

        public LoteriaRow(int id, String nome, String descricao) {
            this.id = id;
            this.nome = new SimpleStringProperty(nome);
            this.descricao = new SimpleStringProperty(descricao);
        }
        public int getId() { return id; }
        public String getNome() { return nome.get(); }
        public String getDescricao() { return descricao.get(); }
    }
}