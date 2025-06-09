package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.model.Loteria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Optional;

public class LoteriaListaView {

    private final VBox root;
    private final TableView<LoteriaRow> tabela;
    private final ObservableList<LoteriaRow> dadosTabela;

    public LoteriaListaView() {
        root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #23395d 0%, #4069a3 100%);");

        Label title = new Label("Cadastro de Loterias");
        title.setFont(Font.font("Segoe UI", 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(6, Color.BLACK));

        dadosTabela = FXCollections.observableArrayList();
        tabela = new TableView<>(dadosTabela);

        TableColumn<LoteriaRow, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(cell -> cell.getValue().nomeProperty());
        nomeCol.setPrefWidth(160);

        TableColumn<LoteriaRow, String> descCol = new TableColumn<>("Descrição");
        descCol.setCellValueFactory(cell -> cell.getValue().descricaoProperty());
        descCol.setPrefWidth(210);

        TableColumn<LoteriaRow, String> acaoCol = new TableColumn<>("Ações");
        acaoCol.setCellFactory(tc -> new TableCell<>() {
            final Button editar = new Button("Editar");
            final Button excluir = new Button("Excluir");
            final HBox box = new HBox(5, editar, excluir);
            {
                editar.setStyle("-fx-background-color: #238636; -fx-text-fill: white;");
                excluir.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                editar.setOnAction(e -> editarLoteria(getTableView().getItems().get(getIndex()).getId()));
                excluir.setOnAction(e -> excluirLoteria(getTableView().getItems().get(getIndex()).getId(), getTableView().getItems().get(getIndex()).getNome()));
            }
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        acaoCol.setPrefWidth(160);

        tabela.getColumns().addAll(nomeCol, descCol, acaoCol);
        tabela.setPrefHeight(300);

        HBox botoes = new HBox(10);
        botoes.setAlignment(Pos.CENTER_LEFT);

        Button btnNovo = new Button("Novo");
        btnNovo.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNovo.setOnAction(e -> Main.showLoteriaCadastro(new LoteriaCadastroView(null, this)));

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setStyle("-fx-background-color: #395886; -fx-text-fill: white; -fx-font-weight: bold;");
        btnVoltar.setOnAction(e -> Main.showMainMenu());

        botoes.getChildren().addAll(btnNovo, btnVoltar);

        root.getChildren().addAll(title, tabela, botoes);

        atualizarTabela();
    }

    public void atualizarTabela() {
        dadosTabela.clear();
        try {
            LoteriaDAO dao = new LoteriaDAO();
            for (Loteria l : dao.listarLoterias()) {
                dadosTabela.add(new LoteriaRow(l.getId(), l.getNome(), l.getDescricao()));
            }
        } catch (Exception ex) {
            mostrarErro("Erro ao buscar loterias: " + ex.getMessage());
        }
    }

    private void editarLoteria(int loteriaId) {
        try {
            LoteriaDAO dao = new LoteriaDAO();
            Loteria loteria = dao.obterLoteriaPorId(loteriaId);
            Main.showLoteriaCadastro(new LoteriaCadastroView(loteria, this));
        } catch (Exception ex) {
            mostrarErro("Erro ao carregar loteria para edição:\n" + ex.getMessage());
        }
    }

    private void excluirLoteria(int loteriaId, String nome) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Deseja excluir a loteria \"" + nome + "\" e todas as suas faixas?", ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirma exclusão");
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                LoteriaDAO dao = new LoteriaDAO();
                dao.excluirLoteria(loteriaId);
                atualizarTabela();
            } catch (Exception ex) {
                mostrarErro("Erro ao excluir: " + ex.getMessage());
            }
        }
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }
}