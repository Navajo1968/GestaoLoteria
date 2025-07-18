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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.control.ListCell;

import java.util.Comparator;
import java.util.List;

public class CorrecaoResultadosView extends Stage {

    private ComboBox<Loteria> loteriaCombo;
    private ComboBox<Concurso> concursoCombo;
    private TableView<Jogo> jogosTable;
    private Button corrigirBtn, salvarBtn, voltarBtn;
    private VBox root;

    public CorrecaoResultadosView() {
        setTitle("Correção de Jogos");

        root = new VBox(12);
        root.setPadding(new Insets(15));

        loteriaCombo = new ComboBox<>();
        concursoCombo = new ComboBox<>();
        jogosTable = new TableView<>();

        corrigirBtn = new Button("Corrigir Jogos");
        salvarBtn = new Button("Salvar Resultados");
        voltarBtn = new Button("Voltar");

        loteriaCombo.setPromptText("Selecione a Loteria");
        concursoCombo.setPromptText("Selecione o Concurso");

        // Configurações da Combo de Loteria
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

        // Configurações da Combo de Concurso - ordem decrescente por número
        concursoCombo.setConverter(new StringConverter<Concurso>() {
            @Override
            public String toString(Concurso object) {
                return (object != null) ? "Nº " + object.getNumero() : "";
            }
            @Override
            public Concurso fromString(String string) {
                for (Concurso c : concursoCombo.getItems()) {
                    if (("Nº " + c.getNumero()).equals(string)) return c;
                }
                return null;
            }
        });
        concursoCombo.setCellFactory(listView -> new ListCell<Concurso>() {
            @Override
            protected void updateItem(Concurso item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? "" : "Nº " + item.getNumero());
            }
        });

        carregarLoterias();

        loteriaCombo.setOnAction(e -> carregarConcursos());
        concursoCombo.setOnAction(e -> carregarJogos());

        corrigirBtn.setOnAction(e -> corrigirJogos());
        salvarBtn.setOnAction(e -> salvarResultados());
        voltarBtn.setOnAction(e -> this.close());

        HBox filtros = new HBox(10, loteriaCombo, concursoCombo, corrigirBtn, salvarBtn, voltarBtn);
        filtros.setPadding(new Insets(0, 0, 10, 0));

        root.getChildren().addAll(new Label("Correção dos Jogos por Concurso"), filtros, jogosTable);

        setScene(new Scene(root, 800, 500));
        initModality(Modality.APPLICATION_MODAL);
        configurarTabela();
    }

    private void carregarLoterias() {
        try {
            List<Loteria> loterias = new LoteriaDAO().listarLoterias();
            loteriaCombo.setItems(FXCollections.observableArrayList(loterias));
        } catch (Exception e) {
            showAlert("Erro ao carregar loterias: " + e.getMessage());
        }
    }

    private void carregarConcursos() {
        try {
            Loteria loteria = loteriaCombo.getValue();
            if (loteria != null) {
                List<Concurso> concursos = new ConcursoDAO().listarConcursosPorLoteria(loteria.getId());
                concursos.sort(Comparator.comparing(Concurso::getNumero).reversed()); // Ordem decrescente
                concursoCombo.setItems(FXCollections.observableArrayList(concursos));
            } else {
                concursoCombo.getItems().clear();
            }
        } catch (Exception e) {
            showAlert("Erro ao carregar concursos: " + e.getMessage());
        }
    }

    private void carregarJogos() {
        try {
            Concurso concurso = concursoCombo.getValue();
            Loteria loteria = loteriaCombo.getValue();
            if (concurso != null && loteria != null) {
                List<Jogo> jogos = new JogoDAO().buscarJogosPorNumeroConcursoPrevisto(loteria.getId(), concurso.getNumero());
                jogosTable.setItems(FXCollections.observableArrayList(jogos));
            } else {
                jogosTable.getItems().clear();
            }
        } catch (Exception e) {
            showAlert("Erro ao carregar jogos: " + e.getMessage());
        }
    }

    private void corrigirJogos() {
        Concurso concurso = concursoCombo.getValue();
        if (concurso == null) {
            showAlert("Selecione um concurso!");
            return;
        }
        List<Jogo> jogos = jogosTable.getItems();
        List<Integer> numerosSorteados;
        try {
            numerosSorteados = new ConcursoDAO().buscarNumerosSorteadosDoConcurso(concurso.getId());
        } catch (Exception e) {
            showAlert("Erro ao buscar os números sorteados: " + e.getMessage());
            return;
        }
        for (Jogo jogo : jogos) {
            int acertos = calcularAcertos(jogo, numerosSorteados);
            jogo.setAcertos(acertos);
        }
        jogosTable.refresh();
    }

    private void salvarResultados() {
        try {
            List<Jogo> jogos = jogosTable.getItems();
            for (Jogo jogo : jogos) {
                new JogoDAO().atualizarAcertos(jogo);
            }
            showAlert("Resultados salvos com sucesso!");
        } catch (Exception e) {
            showAlert("Erro ao salvar resultados: " + e.getMessage());
        }
    }

    private int calcularAcertos(Jogo jogo, List<Integer> numerosSorteados) {
        List<Integer> numerosJogo = jogo.getNumerosList(); // Certifique-se que Jogo tem esse método!
        int acertos = 0;
        for (Integer n : numerosJogo) {
            if (numerosSorteados.contains(n)) acertos++;
        }
        return acertos;
    }

    private void configurarTabela() {
        TableColumn<Jogo, String> numerosCol = new TableColumn<>("Números do Jogo");
        numerosCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNumeros()
            )
        );

        TableColumn<Jogo, Integer> acertosCol = new TableColumn<>("Acertos");
        acertosCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleIntegerProperty(
                data.getValue().getAcertos() != null ? data.getValue().getAcertos() : 0
            ).asObject()
        );

        jogosTable.getColumns().clear();
        jogosTable.getColumns().addAll(numerosCol, acertosCol);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}