package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.JogoDAO;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.model.Jogo;
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
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class ProcessosView {

    private final VBox root;
    private final ComboBox<Loteria> cbLoteria;
    private final TableView<JogoRow> tabela;
    private final ObservableList<JogoRow> dadosTabela;
    private File arquivoSelecionado;

    public ProcessosView() {
        root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #23395d 0%, #4069a3 100%);");

        Label title = new Label("Importação de Jogos");
        title.setFont(Font.font("Segoe UI", 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(6, Color.BLACK));

        HBox filtrosBox = new HBox(12);
        filtrosBox.setAlignment(Pos.CENTER_LEFT);

        cbLoteria = new ComboBox<>();
        cbLoteria.setPromptText("Selecione a Loteria");
        carregarLoterias();
        cbLoteria.setMinWidth(200);
        cbLoteria.setOnAction(e -> carregarJogos());

        Button btnArquivo = new Button("Selecionar Arquivo");
        btnArquivo.setOnAction(e -> selecionarArquivo());

        Label lblArquivo = new Label("Nenhum arquivo selecionado.");
        lblArquivo.setTextFill(Color.WHITE);

        btnArquivo.setMaxHeight(28);

        Button btnImportar = new Button("Importar");
        btnImportar.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold;");
        btnImportar.setOnAction(e -> importarJogos(lblArquivo));

        filtrosBox.getChildren().addAll(cbLoteria, btnArquivo, lblArquivo, btnImportar);

        dadosTabela = FXCollections.observableArrayList();
        tabela = new TableView<>(dadosTabela);

        TableColumn<JogoRow, Integer> colConcurso = new TableColumn<>("Concurso");
        colConcurso.setCellValueFactory(cell -> cell.getValue().concursoProperty().asObject());
        colConcurso.setPrefWidth(80);

        TableColumn<JogoRow, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(cell -> cell.getValue().dataProperty());
        colData.setPrefWidth(110);

        TableColumn<JogoRow, String> colDezenas = new TableColumn<>("Dezenas");
        colDezenas.setCellValueFactory(cell -> cell.getValue().dezenasProperty());
        colDezenas.setPrefWidth(350);

        tabela.getColumns().addAll(colConcurso, colData, colDezenas);
        tabela.setPrefHeight(300);

        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction(e -> Main.showMainMenu());
        footer.getChildren().add(btnVoltar);

        root.getChildren().addAll(title, filtrosBox, tabela, footer);
    }

    private void carregarLoterias() {
        try {
            LoteriaDAO dao = new LoteriaDAO();
            List<Loteria> loterias = dao.listarLoterias();
            cbLoteria.getItems().setAll(loterias);
        } catch (Exception ex) {
            mostrarErro("Erro ao carregar loterias: " + ex.getMessage());
        }
    }

    private void selecionarArquivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o arquivo da loteria");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Planilha Excel", "*.xlsx"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            arquivoSelecionado = file;
            ((Label)((HBox)root.getChildren().get(1)).getChildren().get(2)).setText(file.getName());
        }
    }

    private void importarJogos(Label lblArquivo) {
        Loteria loteria = cbLoteria.getValue();
        if (loteria == null) {
            mostrarErro("Selecione a loteria para importar.");
            return;
        }
        if (arquivoSelecionado == null) {
            mostrarErro("Selecione o arquivo de importação.");
            return;
        }
        try {
            // TODO: Implemente aqui a leitura do arquivo XLSX e importação para o banco
            // Exemplo: JogoImportacaoUtil.importar(loteria, arquivoSelecionado);
            mostrarInfo("Importação concluída com sucesso!");
            carregarJogos();
            lblArquivo.setText("Nenhum arquivo selecionado.");
            arquivoSelecionado = null;
        } catch (Exception ex) {
            mostrarErro("Erro ao importar: " + ex.getMessage());
        }
    }

    private void carregarJogos() {
        dadosTabela.clear();
        Loteria loteria = cbLoteria.getValue();
        if (loteria == null) return;
        try {
            JogoDAO dao = new JogoDAO();
            List<Jogo> jogos = dao.listarJogosPorLoteria(loteria.getId());
            jogos.sort((a, b) -> Integer.compare(b.getConcurso(), a.getConcurso())); // último primeiro
            for (Jogo jogo : jogos) {
                dadosTabela.add(new JogoRow(jogo));
            }
            if (!dadosTabela.isEmpty()) {
                tabela.getSelectionModel().select(0); // destaca o último
            }
        } catch (Exception ex) {
            mostrarErro("Erro ao carregar jogos: " + ex.getMessage());
        }
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }
}