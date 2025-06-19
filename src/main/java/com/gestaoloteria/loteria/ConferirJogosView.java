package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.ConcursoDAO;
import com.gestaoloteria.loteria.dao.ConcursoNumeroSorteadoDAO;
import com.gestaoloteria.loteria.dao.JogoDAO;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.model.Concurso;
import com.gestaoloteria.loteria.model.ConcursoNumeroSorteado;
import com.gestaoloteria.loteria.model.Jogo;
import com.gestaoloteria.loteria.model.Loteria;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.*;
import java.util.stream.Collectors;

public class ConferirJogosView extends Stage {

    private ComboBox<Loteria> loteriaCombo;
    private ComboBox<Concurso> concursoCombo;
    private TableView<JogoConferencia> jogosTable;
    private Button conferirBtn, salvarBtn, voltarBtn;
    private List<JogoConferencia> jogosConferencia = new ArrayList<>();
    private List<Integer> dezenasSorteadas = Collections.emptyList();

    public ConferirJogosView() {
        setTitle("Conferir Jogos Sugeridos");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        loteriaCombo = new ComboBox<>();
        concursoCombo = new ComboBox<>();
        carregarLoterias();

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

        concursoCombo.setConverter(new StringConverter<Concurso>() {
            @Override
            public String toString(Concurso object) {
                if (object == null) return "";
                return String.format("Nº %d (%s)", object.getNumero(), object.getData() != null ? object.getData().toString() : "");
            }
            @Override
            public Concurso fromString(String string) {
                for (Concurso c : concursoCombo.getItems()) {
                    if (toString(c).equals(string)) return c;
                }
                return null;
            }
        });

        HBox linha1 = new HBox(10, new Label("Loteria:"), loteriaCombo, new Label("Concurso:"), concursoCombo);
        linha1.setPadding(new Insets(0,0,10,0));

        jogosTable = new TableView<>();
        TableColumn<JogoConferencia, String> dezenasCol = new TableColumn<>("Dezenas Apostadas");
        dezenasCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDezenasString()));

        TableColumn<JogoConferencia, String> acertosCol = new TableColumn<>("Acertos");
        acertosCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAcertosResumo()));

        TableColumn<JogoConferencia, String> premioCol = new TableColumn<>("Premiação");
        premioCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPremiacao()));

        jogosTable.getColumns().addAll(dezenasCol, acertosCol, premioCol);

        conferirBtn = new Button("Conferir");
        conferirBtn.setOnAction(e -> conferirJogos());

        salvarBtn = new Button("Salvar Conferência");
        salvarBtn.setOnAction(e -> salvarConferencia());
        salvarBtn.setDisable(true);

        voltarBtn = new Button("Voltar");
        voltarBtn.setOnAction(e -> this.close());

        HBox botoes = new HBox(10, conferirBtn, salvarBtn, voltarBtn);
        botoes.setPadding(new Insets(10,0,0,0));

        root.getChildren().addAll(linha1, jogosTable, botoes);

        loteriaCombo.setOnAction(e -> carregarConcursos());
        concursoCombo.setOnAction(e -> carregarJogosNaoConferidos());

        setScene(new Scene(root, 750, 400));
        initModality(Modality.APPLICATION_MODAL);
    }

    private void carregarLoterias() {
        try {
            List<Loteria> loterias = new LoteriaDAO().listarLoterias();
            loteriaCombo.setItems(FXCollections.observableArrayList(loterias));
            if (!loterias.isEmpty()) loteriaCombo.getSelectionModel().selectFirst();
            carregarConcursos();
        } catch (Exception e) {
            showAlert("Erro ao carregar loterias: " + e.getMessage());
        }
    }

    private void carregarConcursos() {
        try {
            Loteria loteria = loteriaCombo.getValue();
            if (loteria == null) return;
            List<Concurso> concursos = new ConcursoDAO().listarConcursosPorLoteria(loteria.getId());
            concursos.sort(Comparator.comparing(Concurso::getNumero).reversed());
            concursoCombo.setItems(FXCollections.observableArrayList(concursos));
            if (!concursos.isEmpty()) concursoCombo.getSelectionModel().selectFirst();
            carregarJogosNaoConferidos();
        } catch (Exception e) {
            showAlert("Erro ao carregar concursos: " + e.getMessage());
        }
    }

    private void carregarJogosNaoConferidos() {
        jogosConferencia.clear();
        jogosTable.getItems().clear();
        salvarBtn.setDisable(true);
        try {
            Concurso concurso = concursoCombo.getValue();
            if (concurso == null) return;
            List<Jogo> jogos = new JogoDAO().buscarJogosPorConcursoNaoConferidos(concurso.getId());
            for (Jogo jogo : jogos) {
                jogosConferencia.add(new JogoConferencia(jogo));
            }
            jogosTable.setItems(FXCollections.observableArrayList(jogosConferencia));
            dezenasSorteadas = buscarDezenasSorteadas(concurso.getId());
        } catch (Exception e) {
            showAlert("Erro ao carregar jogos: " + e.getMessage());
        }
    }

    private List<Integer> buscarDezenasSorteadas(int concursoId) throws Exception {
        List<ConcursoNumeroSorteado> numeros = new ConcursoNumeroSorteadoDAO().listarNumerosPorConcurso(concursoId);
        return numeros.stream().map(ConcursoNumeroSorteado::getNumero).collect(Collectors.toList());
    }

    private void conferirJogos() {
        if (dezenasSorteadas == null || dezenasSorteadas.isEmpty()) {
            showAlert("Resultado oficial ainda não cadastrado para este concurso.");
            return;
        }
        for (JogoConferencia jc : jogosConferencia) {
            jc.conferir(dezenasSorteadas);
        }
        jogosTable.refresh();
        salvarBtn.setDisable(false);
    }

    private void salvarConferencia() {
        int salvos = 0;
        try {
            JogoDAO jogoDAO = new JogoDAO();
            for (JogoConferencia jc : jogosConferencia) {
                // Só salva se realmente foi conferido
                if (jc.foiConferido()) {
                    jc.getJogo().setAcertos(jc.getAcertos());
                    jogoDAO.atualizarAcertos(jc.getJogo());
                    salvos++;
                }
            }
            showAlert(salvos + " jogos atualizados com sucesso!");
            this.close();
        } catch (Exception e) {
            showAlert("Erro ao salvar conferência: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Classe auxiliar para conferência dos jogos.
     */
    public static class JogoConferencia {
        private final Jogo jogo;
        private int acertos = -1;
        private boolean conferido = false;

        public JogoConferencia(Jogo jogo) {
            this.jogo = jogo;
        }

        public Jogo getJogo() { return jogo; }
        public int getAcertos() { return acertos; }
        public boolean foiConferido() { return conferido; }

        public void conferir(List<Integer> dezenasSorteadas) {
            Set<Integer> apostadas = Arrays.stream(jogo.getDezenas().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
            acertos = 0;
            for (Integer dez : dezenasSorteadas) {
                if (apostadas.contains(dez)) acertos++;
            }
            conferido = true;
        }

        public String getDezenasString() {
            return Arrays.stream(jogo.getDezenas().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(" - "));
        }

        public String getAcertosResumo() {
            return conferido ? String.valueOf(acertos) : "";
        }

        public String getPremiacao() {
            if (!conferido) return "";
            if (acertos >= 15) return "15 acertos (máximo)";
            if (acertos == 14) return "14 acertos";
            if (acertos == 13) return "13 acertos";
            if (acertos == 12) return "12 acertos";
            if (acertos == 11) return "11 acertos";
            if (acertos < 11) return "Sem prêmio";
            return "";
        }
    }
}