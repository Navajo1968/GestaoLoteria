package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.ConcursoDAO;
import com.gestaoloteria.loteria.dao.ConcursoNumeroSorteadoDAO;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.model.Concurso;
import com.gestaoloteria.loteria.model.ConcursoNumeroSorteado;
import com.gestaoloteria.loteria.model.Loteria;
import com.gestaoloteria.loteria.ui.ConcursoRow;
import com.gestaoloteria.loteria.util.ImportadorHistoricoLoteria;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessosView {
    private final VBox root;
    private final ComboBox<Loteria> cbLoteria;
    private final TableView<ConcursoRow> tabela;
    private final ObservableList<ConcursoRow> dadosTabela;
    private File arquivoSelecionado;

    public ProcessosView() {
        root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #23395d 0%, #4069a3 100%);");

        Label title = new Label("Importação de Jogos - Histórico");
        title.setFont(Font.font("Segoe UI", 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(6, Color.BLACK));

        HBox filtrosBox = new HBox(12);
        filtrosBox.setAlignment(Pos.CENTER_LEFT);

        cbLoteria = new ComboBox<>();
        cbLoteria.setPromptText("Selecione a Loteria");

        // Exibir o nome da loteria na combo e no botão (NÃO a descrição)
        cbLoteria.setCellFactory(listView -> new ListCell<Loteria>() {
            @Override
            protected void updateItem(Loteria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
        cbLoteria.setButtonCell(new ListCell<Loteria>() {
            @Override
            protected void updateItem(Loteria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        carregarLoterias();
        cbLoteria.setMinWidth(200);
        cbLoteria.setOnAction(e -> carregarConcursos());

        Button btnArquivo = new Button("Selecionar Arquivo");
        btnArquivo.setOnAction(e -> selecionarArquivo());

        Label lblArquivo = new Label("Nenhum arquivo selecionado.");
        lblArquivo.setTextFill(Color.WHITE);

        btnArquivo.setMaxHeight(28);

        Button btnImportar = new Button("Importar");
        btnImportar.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold;");
        btnImportar.setOnAction(e -> importarHistorico(lblArquivo));

        filtrosBox.getChildren().addAll(cbLoteria, btnArquivo, lblArquivo, btnImportar);

        dadosTabela = FXCollections.observableArrayList();
        tabela = new TableView<>(dadosTabela);

        TableColumn<ConcursoRow, Integer> colConcurso = new TableColumn<>("Concurso");
        colConcurso.setCellValueFactory(cell -> cell.getValue().numeroConcursoProperty().asObject());
        colConcurso.setPrefWidth(80);

        TableColumn<ConcursoRow, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(cell -> cell.getValue().dataConcursoProperty());
        colData.setPrefWidth(110);

        TableColumn<ConcursoRow, String> colDezenas = new TableColumn<>("Dezenas");
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
            mostrarErro("Erro ao carregar loterias: " + ex.getMessage(), ex);
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

    private void importarHistorico(Label lblArquivo) {
        Loteria loteria = cbLoteria.getValue();
        if (loteria == null) {
            mostrarErro("Selecione a loteria para importar.", null);
            return;
        }
        if (arquivoSelecionado == null) {
            mostrarErro("Selecione o arquivo de importação.", null);
            return;
        }
        try {
            ImportadorHistoricoLoteria.importar(loteria, arquivoSelecionado);
            mostrarInfo("Importação concluída com sucesso!");
            carregarConcursos();
            lblArquivo.setText("Nenhum arquivo selecionado.");
            arquivoSelecionado = null;
        } catch (Exception ex) {
            mostrarErro("Erro ao importar!", ex);
        }
    }

    private void carregarConcursos() {
        dadosTabela.clear();
        Loteria loteria = cbLoteria.getValue();
        if (loteria == null) return;
        try {
            ConcursoDAO concursoDAO = new ConcursoDAO();
            ConcursoNumeroSorteadoDAO numeroDAO = new ConcursoNumeroSorteadoDAO();
            List<Concurso> concursos = concursoDAO.listarConcursosPorLoteria(loteria.getId());
            for (Concurso concurso : concursos) {
                List<ConcursoNumeroSorteado> dezenas = numeroDAO.listarNumerosPorConcurso(concurso.getId());
                String dezenasStr = dezenas.stream()
                        .map(n -> String.format("%02d", n.getNumero()))
                        .collect(Collectors.joining(" - "));
                dadosTabela.add(new ConcursoRow(concurso, dezenasStr));
            }
            if (!dadosTabela.isEmpty()) {
                tabela.getSelectionModel().select(0); // destaca o último
            }
        } catch (Exception ex) {
            mostrarErro("Erro ao carregar concursos: " + ex.getMessage(), ex);
        }
    }

    private void mostrarErro(String msg, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(msg);

        if (ex != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(new Label("Detalhes do erro:"), 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
        }
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