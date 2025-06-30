package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.model.Loteria;
import com.gestaoloteria.loteria.model.Jogo;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.dao.JogoDAO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GerarJogosInteligentesView extends Stage {

    private ComboBox<Loteria> loteriaCombo;
    private TextField concursoPrevistoField;
    private ToggleGroup dezenasGroup;
    private RadioButton rb18, rb19, rb20;
    private CheckBox[] dezenasCheckboxes;
    private TextField qtdJogosField;
    private TableView<List<Integer>> jogosTable;
    private Button gerarBtn, salvarBtn, voltarBtn, trazerDezenasHistoricasBtn;
    private Label simulacaoLabel;

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

        // Linha 2: Opção de dezenas (18, 19, 20) e quantidade de jogos
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

        Label lblQtdJogos = new Label("Qtd. Jogos:");
        qtdJogosField = new TextField("20");
        qtdJogosField.setPrefWidth(60);

        trazerDezenasHistoricasBtn = new Button("Trazer dezenas históricas");

        linha2.getChildren().addAll(lblQtdDezenas, rb18, rb19, rb20, lblQtdJogos, qtdJogosField, trazerDezenasHistoricasBtn);

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

        simulacaoLabel = new Label("Cobertura: ");

        gerarBtn.setOnAction(e -> gerarJogos());
        salvarBtn.setOnAction(e -> salvarJogos());
        voltarBtn.setOnAction(e -> this.close());

        trazerDezenasHistoricasBtn.setOnAction(e -> trazerDezenasHistoricas());

        root.getChildren().addAll(linha1, linha2, dezenasGrid, linhaBotoes, simulacaoLabel, jogosTable);

        setScene(new Scene(root, 760, 540));
        initModality(Modality.APPLICATION_MODAL);

        atualizarSelecaoDezenas();
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
                cb.setDisable(false);
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
        int qtdJogosDesejado;
        try {
            qtdJogosDesejado = Integer.parseInt(qtdJogosField.getText().trim());
            if (qtdJogosDesejado <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "Informe uma quantidade válida de jogos (>0).", ButtonType.OK);
            alerta.setHeaderText("Atenção");
            alerta.showAndWait();
            return;
        }

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

        long totalComb = combinacoes(dezenasSelecionadas.size(), 15);

        if (qtdJogosDesejado > totalComb) {
            qtdJogosDesejado = (int) totalComb;
            qtdJogosField.setText(String.valueOf(qtdJogosDesejado));
        }

        jogosGerados = gerarComb15(dezenasSelecionadas, qtdJogosDesejado);

        jogosTable.setItems(FXCollections.observableArrayList(jogosGerados));
        salvarBtn.setDisable(jogosGerados.isEmpty());
    }

    private long combinacoes(int n, int r) {
        if (r > n) return 0;
        long numerador = 1, denominador = 1;
        for (int i = 1; i <= r; i++) {
            numerador *= (n - (i - 1));
            denominador *= i;
        }
        return numerador / denominador;
    }

    private List<List<Integer>> gerarComb15(List<Integer> dezenasSelecionadas, int limite) {
        List<List<Integer>> resultado = new ArrayList<>(limite);
        long totalComb = combinacoes(dezenasSelecionadas.size(), 15);
        if (totalComb <= 10000 && limite >= totalComb) {
            combinar(dezenasSelecionadas, 15, 0, new ArrayList<>(), resultado, limite);
        } else {
            Set<String> jogosUnicos = new HashSet<>();
            Random rand = new Random();
            int tentativas = 0;
            while (resultado.size() < limite && tentativas < limite * 10) {
                List<Integer> copia = new ArrayList<>(dezenasSelecionadas);
                Collections.shuffle(copia, rand);
                List<Integer> jogo = copia.subList(0, 15).stream().sorted().collect(Collectors.toList());
                String chave = jogo.toString();
                if (jogosUnicos.add(chave)) {
                    resultado.add(jogo);
                }
                tentativas++;
            }
        }
        return resultado;
    }

    private void combinar(List<Integer> base, int k, int ini, List<Integer> parcial, List<List<Integer>> resultado, int limite) {
        if (resultado.size() >= limite) return;
        if (parcial.size() == k) {
            resultado.add(new ArrayList<>(parcial));
            return;
        }
        for (int i = ini; i <= base.size() - (k - parcial.size()); i++) {
            parcial.add(base.get(i));
            combinar(base, k, i + 1, parcial, resultado, limite);
            parcial.remove(parcial.size() - 1);
            if (resultado.size() >= limite) break;
        }
    }

    private void salvarJogos() {
        try {
            Loteria loteria = loteriaCombo.getValue();
            if (loteria == null) {
                Alert alerta = new Alert(Alert.AlertType.WARNING, "Selecione uma loteria.", ButtonType.OK);
                alerta.setHeaderText("Atenção");
                alerta.showAndWait();
                return;
            }
            String concursoPrevistoStr = concursoPrevistoField.getText().trim();
            if (concursoPrevistoStr.isEmpty()) {
                Alert alerta = new Alert(Alert.AlertType.WARNING, "Informe o concurso previsto.", ButtonType.OK);
                alerta.setHeaderText("Atenção");
                alerta.showAndWait();
                return;
            }
            Integer numeroConcursoPrevisto = Integer.parseInt(concursoPrevistoStr);

            List<Jogo> jogosParaSalvar = new ArrayList<>();
            for (List<Integer> dezenas : jogosGerados) {
                Jogo jogo = new Jogo();
                jogo.setLoteriaId(loteria.getId());
                jogo.setNumeroConcursoPrevisto(numeroConcursoPrevisto);
                String numerosStr = dezenas.stream()
                        .map(n -> String.format("%02d", n))
                        .collect(Collectors.joining(","));
                jogo.setNumeros(numerosStr);
                jogo.setDataHora(LocalDateTime.now());
                jogo.setObservacao("Gerado automaticamente");
                jogosParaSalvar.add(jogo);
            }
            new JogoDAO().salvarJogos(jogosParaSalvar);

            Alert alerta = new Alert(Alert.AlertType.INFORMATION, jogosParaSalvar.size() + " jogos salvos com sucesso!", ButtonType.OK);
            alerta.setHeaderText("Jogos salvos");
            alerta.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alerta = new Alert(Alert.AlertType.ERROR, "Erro ao salvar jogos: " + ex.getMessage(), ButtonType.OK);
            alerta.setHeaderText("Erro");
            alerta.showAndWait();
        }
    }

    private void trazerDezenasHistoricas() {
        int numDezenas = rb18.isSelected() ? 18 : rb19.isSelected() ? 19 : 20;
        List<String> opcoes = Arrays.asList(
                "Mais frequentes (histórico completo)",
                "Mais frequentes (últimos 50 concursos)",
                "Estratégia mista (clássico + recentes + atrasados)"
        );
        ChoiceDialog<String> dialog = new ChoiceDialog<>(opcoes.get(0), opcoes);
        dialog.setTitle("Opção de Estratégia");
        dialog.setHeaderText("Escolha como os números serão selecionados:");
        dialog.setContentText("Estratégia:");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) return;
        String estrategia = result.get();

        Stage progressoStage = new Stage();
        progressoStage.initOwner(this);
        progressoStage.initModality(Modality.APPLICATION_MODAL);
        progressoStage.setTitle("Processando histórico...");
        VBox vbox = new VBox(18);
        vbox.setPadding(new Insets(24));
        vbox.setAlignment(Pos.CENTER);
        Label lbl = new Label("Analisando histórico e selecionando dezenas...");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(320);
        Label lblPercent = new Label("0%");
        vbox.getChildren().addAll(lbl, progressBar, lblPercent);
        progressoStage.setScene(new Scene(vbox, 400, 160));

        Task<List<Integer>> task = new Task<List<Integer>>() {
            @Override
            protected List<Integer> call() throws Exception {
                File csvFile = new File("Lotofácil.csv");
                if (!csvFile.exists())
                    throw new FileNotFoundException("Arquivo Lotofácil.csv não encontrado na pasta do projeto.");

                List<List<Integer>> historico = new ArrayList<>();
                int totalLinhas = contarLinhas(csvFile);
                int linhaAtual = 0;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
                    String linha;
                    br.readLine(); // cabeçalho
                    while ((linha = br.readLine()) != null) {
                        String[] campos = linha.split(";");
                        List<Integer> bolas = new ArrayList<>();
                        for (int i = 2; i <= 16; i++) {
                            if (i < campos.length) {
                                try {
                                    int dez = Integer.parseInt(campos[i].trim());
                                    if (dez >= 1 && dez <= 25) bolas.add(dez);
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                        if (bolas.size() == 15) historico.add(bolas);
                        linhaAtual++;
                        if (linhaAtual % 200 == 0) {
                            updateProgress(linhaAtual, totalLinhas);
                            updateMessage(String.format("%.0f%%", 100.0 * linhaAtual / totalLinhas));
                        }
                    }
                }
                updateProgress(totalLinhas, totalLinhas);
                updateMessage("100%");

                List<Integer> dezenasEscolhidas;
                if (estrategia.contains("histórico completo")) {
                    dezenasEscolhidas = maisFrequentes(historico, numDezenas, historico.size());
                } else if (estrategia.contains("últimos 50")) {
                    dezenasEscolhidas = maisFrequentes(historico, numDezenas, 50);
                } else {
                    // Estratégia mista: 10 mais frequentes do histórico + 5 dos últimos 50 (sem repetir) + 5 menos sorteados dos últimos 50 (sem repetir)
                    Set<Integer> topHist = new HashSet<>(maisFrequentes(historico, Math.min(10, numDezenas), historico.size()));
                    Set<Integer> top50 = new HashSet<>(maisFrequentes(historico, Math.min(5, numDezenas - topHist.size()), 50));
                    top50.removeAll(topHist);
                    Set<Integer> menos50 = new HashSet<>(menosFrequentes(historico, Math.min(5, numDezenas - topHist.size() - top50.size()), 50));
                    menos50.removeAll(topHist);
                    menos50.removeAll(top50);
                    List<Integer> total = new ArrayList<>();
                    total.addAll(topHist); total.addAll(top50); total.addAll(menos50);
                    while (total.size() < numDezenas) {
                        for (int i = 1; i <= 25 && total.size() < numDezenas; i++)
                            if (!total.contains(i)) total.add(i);
                    }
                    total.sort(Comparator.naturalOrder());
                    dezenasEscolhidas = total;
                }
                // Simulação retroativa
                int concursosSim = 100;
                int inicioSim = Math.max(0, historico.size() - concursosSim);
                List<List<Integer>> ultimos = historico.subList(inicioSim, historico.size());
                List<Integer> acertosPorConcurso = new ArrayList<>();
                for (List<Integer> bolas : ultimos) {
                    int acertos = 0;
                    for (Integer dez : bolas) if (dezenasEscolhidas.contains(dez)) acertos++;
                    acertosPorConcurso.add(acertos);
                }
                int min = acertosPorConcurso.stream().min(Integer::compareTo).orElse(0);
                int max = acertosPorConcurso.stream().max(Integer::compareTo).orElse(0);
                double media = acertosPorConcurso.stream().mapToInt(Integer::intValue).average().orElse(0);
                StringBuilder sb = new StringBuilder();
                sb.append("Cobertura nos últimos ").append(concursosSim).append(" concursos: ")
                  .append("mín. ").append(min)
                  .append(", máx. ").append(max)
                  .append(", média ").append(String.format("%.2f", media));
                Platform.runLater(() -> simulacaoLabel.setText(sb.toString()));

                return dezenasEscolhidas;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        lblPercent.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            progressoStage.close();
            List<Integer> dezenasParaMarcar = task.getValue();
            for (int i = 0; i < 25; i++) {
                dezenasCheckboxes[i].setSelected(dezenasParaMarcar.contains(i + 1));
            }
            atualizarSelecaoDezenas();
            Alert alerta = new Alert(Alert.AlertType.INFORMATION, "Dezenas carregadas: " + dezenasParaMarcar.stream().map(Object::toString).collect(Collectors.joining(", ")), ButtonType.OK);
            alerta.setHeaderText("Seleção automática concluída");
            alerta.showAndWait();
        });

        task.setOnFailed(e -> {
            progressoStage.close();
            Throwable ex = task.getException();
            Alert alerta = new Alert(Alert.AlertType.ERROR, "Erro ao ler arquivo histórico: " + (ex != null ? ex.getMessage() : ""), ButtonType.OK);
            alerta.setHeaderText("Erro na análise histórica");
            alerta.showAndWait();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        progressoStage.showAndWait();
    }

    private List<Integer> maisFrequentes(List<List<Integer>> historico, int qtd, int ultimosConcursos) {
        int ini = Math.max(0, historico.size() - ultimosConcursos);
        Map<Integer, Integer> freq = new HashMap<>();
        for (int i = 1; i <= 25; i++) freq.put(i, 0);
        for (int i = ini; i < historico.size(); i++) {
            for (Integer dez : historico.get(i)) freq.put(dez, freq.get(dez) + 1);
        }
        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(qtd)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }
    private List<Integer> menosFrequentes(List<List<Integer>> historico, int qtd, int ultimosConcursos) {
        int ini = Math.max(0, historico.size() - ultimosConcursos);
        Map<Integer, Integer> freq = new HashMap<>();
        for (int i = 1; i <= 25; i++) freq.put(i, 0);
        for (int i = ini; i < historico.size(); i++) {
            for (Integer dez : historico.get(i)) freq.put(dez, freq.get(dez) + 1);
        }
        return freq.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .limit(qtd)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    private int contarLinhas(File file) throws IOException {
        int linhas = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.readLine() != null) linhas++;
        }
        return linhas > 0 ? linhas : 1;
    }
}