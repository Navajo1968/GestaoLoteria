package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.ConcursoDAO;
import com.gestaoloteria.loteria.dao.ConcursoNumeroSorteadoDAO;
import com.gestaoloteria.loteria.dao.JogoDAO;
import com.gestaoloteria.loteria.dao.LoteriaDAO;
import com.gestaoloteria.loteria.model.Concurso;
import com.gestaoloteria.loteria.model.ConcursoNumeroSorteado;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GerarJogosView extends Stage {

    private ComboBox<Loteria> loteriaCombo;
    private TextField qtdJogosField;
    private TextField concursoPrevistoField;
    private TableView<List<Integer>> jogosTable;
    private List<List<Integer>> jogosSugeridos = new ArrayList<>();
    private Button gerarBtn, salvarBtn, voltarBtn;

    public GerarJogosView() {
        setTitle("Gerar Jogos Sugeridos");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        loteriaCombo = new ComboBox<>();
        qtdJogosField = new TextField();
        concursoPrevistoField = new TextField();

        // Carrega todas as loterias
        List<Loteria> loterias = carregarLoterias();
        loteriaCombo.setItems(FXCollections.observableArrayList(loterias));
        if (!loterias.isEmpty()) loteriaCombo.getSelectionModel().selectFirst();

        // Ajuste para exibir o nome na ComboBox
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

        HBox linha1 = new HBox(10, new Label("Loteria:"), loteriaCombo, new Label("Nº jogos:"), qtdJogosField, new Label("Concurso previsto:"), concursoPrevistoField);
        linha1.setPadding(new Insets(0,0,10,0));

        jogosTable = new TableView<>();
        for (int i = 1; i <= 15; i++) {
            final int idx = i - 1;
            TableColumn<List<Integer>, String> col = new TableColumn<>("D" + i);
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%02d", data.getValue().get(idx))));
            jogosTable.getColumns().add(col);
        }

        gerarBtn = new Button("Gerar Sugestões");
        gerarBtn.setOnAction(e -> gerarSugestoes());

        salvarBtn = new Button("Salvar Jogos");
        salvarBtn.setDisable(true);
        salvarBtn.setOnAction(e -> salvarJogos());

        voltarBtn = new Button("Voltar");
        voltarBtn.setOnAction(e -> this.close());

        HBox botoes = new HBox(10, gerarBtn, salvarBtn, voltarBtn);
        botoes.setPadding(new Insets(10,0,0,0));

        root.getChildren().addAll(linha1, jogosTable, botoes);

        setScene(new Scene(root, 720, 400));
        initModality(Modality.APPLICATION_MODAL);
    }

    private List<Loteria> carregarLoterias() {
        try {
            return new LoteriaDAO().listarLoterias();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void gerarSugestoes() {
        jogosSugeridos.clear();
        jogosTable.getItems().clear();
        int qtd;
        try {
            qtd = Integer.parseInt(qtdJogosField.getText().trim());
            if (qtd < 1 || qtd > 100) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Informe a quantidade de jogos (1 a 100)");
            return;
        }
        if (loteriaCombo.getValue() == null) {
            showAlert("Selecione a loteria");
            return;
        }
        for (int i = 0; i < qtd; i++) {
            jogosSugeridos.add(gerarJogoAvancado());
        }
        jogosTable.setItems(FXCollections.observableArrayList(jogosSugeridos));
        salvarBtn.setDisable(false);
    }

    /**
     * Algoritmo avançado de geração de jogos:
     * - Frequência e atraso das dezenas baseado no histórico de concursos (tabelas concurso e concurso_numero_sorteado)
     * - Balanceamento par/ímpar, baixo/alto, e distribuição no volante
     * - Evita sequências longas e repetições recentes
     */
    private List<Integer> gerarJogoAvancado() {
        try {
            Loteria loteria = loteriaCombo.getValue();
            int totalDezenas = 25;
            int dezenasPorJogo = 15;

            // 1. Buscar últimos X concursos para análise estatística (ex: 100 últimos)
            List<HistoricoConcurso> historico = getHistoricoUltimosConcursos(loteria.getId(), 100);

            Map<Integer, Integer> frequencia = new HashMap<>();
            Map<Integer, Integer> atraso = new HashMap<>();
            Set<String> jogosRecentes = new HashSet<>();

            for (int i = 1; i <= totalDezenas; i++) {
                frequencia.put(i, 0);
                atraso.put(i, 0);
            }

            int ordem = 0;
            for (HistoricoConcurso hc : historico) {
                List<Integer> dezenas = hc.dezenas;
                for (int d : dezenas) {
                    frequencia.put(d, frequencia.get(d) + 1);
                    atraso.put(d, 0);
                }
                // Armazena para evitar repetições exatas
                String jogoKey = dezenas.stream().sorted().map(Object::toString).collect(Collectors.joining(","));
                jogosRecentes.add(jogoKey);

                // Atualiza atraso das dezenas que não saíram nesse concurso
                for (int i = 1; i <= totalDezenas; i++) {
                    if (!dezenas.contains(i)) {
                        atraso.put(i, atraso.get(i) + 1);
                    }
                }
                ordem++;
            }

            // 3. Ordenar dezenas por score (frequência + atraso, normalizado)
            List<Integer> todasDezenas = new ArrayList<>();
            for (int i = 1; i <= totalDezenas; i++) todasDezenas.add(i);

            todasDezenas.sort(Comparator.comparingInt((Integer d) ->
                    frequencia.get(d) * 2 + atraso.get(d)
            ).reversed());

            // 4. Balancear par/ímpar e alto/baixo segundo padrões históricos
            int minPares = 6, maxPares = 9;
            int minBaixos = 6, maxBaixos = 9;

            // 5. Evitar sequências longas (>=4) e repetições exatas
            Random rand = new Random();
            for (int tentativa = 0; tentativa < 100; tentativa++) {
                // Seleciona dezenas mais prováveis (top 20) mas mistura com outras para diversidade
                List<Integer> provaveis = todasDezenas.subList(0, 20);
                List<Integer> outras = todasDezenas.subList(20, todasDezenas.size());
                List<Integer> jogo = new ArrayList<>();
                jogo.addAll(provaveis.subList(0, dezenasPorJogo - 3));
                Collections.shuffle(outras, rand);
                jogo.addAll(outras.subList(0, 3));
                Collections.shuffle(jogo, rand);
                List<Integer> ordenado = jogo.stream().sorted().collect(Collectors.toList());

                // Checar par/ímpar
                long qtdPares = ordenado.stream().filter(n -> n % 2 == 0).count();
                long qtdBaixos = ordenado.stream().filter(n -> n <= 13).count();

                // Checar sequências
                boolean temSequenciaLonga = false;
                int seq = 1;
                for (int i = 1; i < ordenado.size(); i++) {
                    if (ordenado.get(i) == ordenado.get(i - 1) + 1) {
                        seq++;
                        if (seq >= 4) {
                            temSequenciaLonga = true;
                            break;
                        }
                    } else {
                        seq = 1;
                    }
                }

                // Checar se já existe igual
                String chaveJogo = ordenado.stream().map(Object::toString).collect(Collectors.joining(","));

                if (qtdPares >= minPares && qtdPares <= maxPares
                        && qtdBaixos >= minBaixos && qtdBaixos <= maxBaixos
                        && !temSequenciaLonga
                        && !jogosRecentes.contains(chaveJogo)) {
                    return ordenado;
                }
            }

            // Se não passou nas regras, faz aleatório como fallback
            List<Integer> todas = new ArrayList<>();
            for (int i = 1; i <= totalDezenas; i++) todas.add(i);
            Collections.shuffle(todas, new Random());
            return todas.subList(0, dezenasPorJogo).stream().sorted().collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            List<Integer> todas = new ArrayList<>();
            for (int i = 1; i <= 25; i++) todas.add(i);
            Collections.shuffle(todas, new Random());
            return todas.subList(0, 15).stream().sorted().collect(Collectors.toList());
        }
    }

    /**
     * Recupera os últimos N concursos de uma loteria já com a lista de dezenas sorteadas.
     */
    private List<HistoricoConcurso> getHistoricoUltimosConcursos(int loteriaId, int quantidade) throws Exception {
        ConcursoDAO concursoDAO = new ConcursoDAO();
        ConcursoNumeroSorteadoDAO numeroDAO = new ConcursoNumeroSorteadoDAO();

        // Recupera últimos N concursos (ordenados do mais recente para o mais antigo)
        List<Concurso> concursos = concursoDAO.listarConcursosPorLoteria(loteriaId);
        concursos.sort(Comparator.comparing(Concurso::getNumero).reversed());
        List<HistoricoConcurso> historico = new ArrayList<>();

        int count = 0;
        for (Concurso c : concursos) {
            if (count >= quantidade) break;
            List<ConcursoNumeroSorteado> numeros = numeroDAO.listarNumerosPorConcurso(c.getId());
            List<Integer> dezenas = numeros.stream().map(ConcursoNumeroSorteado::getNumero).collect(Collectors.toList());
            historico.add(new HistoricoConcurso(c, dezenas));
            count++;
        }
        return historico;
    }

    private void salvarJogos() {
        if (jogosSugeridos.isEmpty()) {
            showAlert("Nenhum jogo gerado para salvar.");
            return;
        }
        Loteria loteria = loteriaCombo.getValue();
        if (loteria == null) {
            showAlert("Selecione a loteria.");
            return;
        }
        Integer numeroConcursoPrevisto;
        try {
            numeroConcursoPrevisto = Integer.parseInt(concursoPrevistoField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Informe o número do concurso previsto!");
            return;
        }
        List<Jogo> jogos = new ArrayList<>();
        for (List<Integer> lista : jogosSugeridos) {
            String nums = lista.stream().map(n -> String.format("%02d", n)).collect(Collectors.joining(","));
            jogos.add(new Jogo(
                    loteria.getId(),
                    null, // concursoId, ainda não existe
                    numeroConcursoPrevisto,
                    nums,
                    LocalDateTime.now(),
                    null,
                    null
            ));
        }
        try {
            new JogoDAO().salvarJogos(jogos);
            showAlert("Jogos salvos com sucesso!");
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro ao salvar jogos: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Classe interna para guardar concurso + dezenas sorteadas.
     */
    private static class HistoricoConcurso {
        public final Concurso concurso;
        public final List<Integer> dezenas;
        public HistoricoConcurso(Concurso concurso, List<Integer> dezenas) {
            this.concurso = concurso;
            this.dezenas = dezenas;
        }
    }
}