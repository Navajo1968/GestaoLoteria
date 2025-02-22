package com.loteriascorp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.loteriascorp.analise.AnalisadorConcurso;
import com.loteriascorp.service.GeradorJogosOptimizado;

public class GerarProbabilidadeController {
    
    private static final Logger logger = LogManager.getLogger(GerarProbabilidadeController.class);
    
    @FXML private ComboBox<String> loteriaComboBox;
    @FXML private Spinner<Integer> quantidadeJogosSpinner;
    @FXML private TextField numeroConcursoTextField;
    @FXML private TableView<Jogo> jogosTable;
    @FXML private TableColumn<Jogo, Integer> numeroJogoColumn;
    @FXML private TableColumn<Jogo, String> numerosColumn;
    @FXML private Label valorTotalApostaLabel;
    @FXML private TextArea txtAnalise;
    
    private AnalisadorConcurso analisador;
    private GeradorJogosOptimizado geradorJogos;
    private List<List<Integer>> jogosGerados;
    
    @FXML
    public void initialize() {
        geradorJogos = new GeradorJogosOptimizado();
        carregarLoterias();
        configurarColunas();
        
        quantidadeJogosSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 5)
        );
    }
    
    private int buscarUltimoConcursoRealizado(int idLoteria) {
        String sql = """
            SELECT MAX(num_concurso) as ultimo_concurso 
            FROM tb_historico_jogos 
            WHERE id_loterias = ?
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("ultimo_concurso");
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar último concurso: ", e);
        }
        return 0;
    }
    
    private void analisarConcursoAtual(int idLoteria, int numeroConcurso) {
        try {
            // Busca o último concurso realizado
            int ultimoConcurso = buscarUltimoConcursoRealizado(idLoteria);
            if (ultimoConcurso == 0) {
                logger.error("Nenhum concurso encontrado para a loteria {}", idLoteria);
                mostrarErro("Erro na Análise", "Nenhum concurso encontrado para esta loteria.");
                return;
            }
            
            // Analisa o último concurso realizado
            analisador = new AnalisadorConcurso(idLoteria, ultimoConcurso);
            analisador.analisarConcurso();
            
            Map<String, Double> metricas = analisador.getMetricas();
            exibirAnalise(metricas, ultimoConcurso, numeroConcurso);
        } catch (Exception e) {
            logger.error("Erro ao analisar concurso: ", e);
            mostrarErro("Erro na Análise", "Não foi possível analisar o concurso: " + e.getMessage());
        }
    }

    private void exibirAnalise(Map<String, Double> metricas, int concursoAnalisado, int proximoConcurso) {
        StringBuilder sb = new StringBuilder();
        sb.append("Análise do Concurso ").append(concursoAnalisado)
          .append(" para gerar jogos do Concurso ").append(proximoConcurso).append(":\n\n");
        
        // Paridade
        sb.append(String.format("Números Pares: %.0f\n", metricas.get("quantidade_pares")));
        sb.append(String.format("Números Ímpares: %.0f\n", metricas.get("quantidade_impares")));
        
        // Distribuição por dezenas
        sb.append("\nDistribuição por Dezenas:\n");
        sb.append(String.format("1ª Dezena (1-10): %.0f\n", metricas.get("qtd_primeira_dezena")));
        sb.append(String.format("2ª Dezena (11-20): %.0f\n", metricas.get("qtd_segunda_dezena")));
        // ... resto do método continua igual
        
        txtAnalise.setText(sb.toString());
    }

    
    private void configurarColunas() {
        numeroJogoColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        numerosColumn.setCellValueFactory(new PropertyValueFactory<>("numerosFormatados"));
    }
    
    private void carregarLoterias() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT des_nome FROM tb_loterias")) {
            
            ResultSet rs = stmt.executeQuery();
            ObservableList<String> loterias = FXCollections.observableArrayList();
            
            while (rs.next()) {
                loterias.add(rs.getString("des_nome"));
            }
            
            loteriaComboBox.setItems(loterias);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao carregar loterias", e.getMessage());
        }
    }
    
    @FXML
    private void handleGerarProbabilidade() {
        String selectedLoteria = loteriaComboBox.getValue();
        int quantidadeJogos = quantidadeJogosSpinner.getValue();
        String numeroConcursoText = numeroConcursoTextField.getText();
        
        if (selectedLoteria == null) {
            mostrarAlerta("Atenção", "Por favor, selecione uma loteria.");
            return;
        }
        
        if (numeroConcursoText.trim().isEmpty()) {
            mostrarAlerta("Atenção", "Por favor, informe o número do concurso.");
            return;
        }

        try {
            int numeroConcurso = Integer.parseInt(numeroConcursoText.trim());
            int idLoteria = getIdLoteria(selectedLoteria);
            
            // Analisa o concurso atual
            analisarConcursoAtual(idLoteria, numeroConcurso);
            
            // Gera os jogos apenas uma vez
            jogosGerados = geradorJogos.gerarJogos(idLoteria, quantidadeJogos);
            
            // Verifica se os jogos já existem no banco
            if (verificaJogosExistentes(idLoteria, numeroConcurso, jogosGerados)) {
                mostrarErro("Jogos Duplicados", 
                    "Já existem jogos cadastrados para este concurso. Por favor, verifique.");
                jogosGerados = null;
                return;
            }
            
            // Mostra na tabela
            List<Jogo> jogos = new ArrayList<>();
            for (int i = 0; i < jogosGerados.size(); i++) {
                jogos.add(new Jogo(i + 1, jogosGerados.get(i), numeroConcurso));
            }
            
            ObservableList<Jogo> observableList = FXCollections.observableArrayList(jogos);
            jogosTable.setItems(observableList);
            
            // Atualiza o valor total das apostas
            double valorAposta = getValorAposta(idLoteria, 15);
            double valorTotalAposta = quantidadeJogos * valorAposta;
            valorTotalApostaLabel.setText(String.format("Valor Total da Aposta: R$ %.2f", valorTotalAposta));
            
        } catch (NumberFormatException e) {
            mostrarErro("Erro de formato", "O número do concurso deve ser um número inteiro válido.");
            jogosGerados = null;
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao gerar jogos", e.getMessage());
            jogosGerados = null;
        }
    }    
    
    private boolean verificaJogosExistentes(int idLoteria, int numeroConcurso, List<List<Integer>> jogos) {
        String sql = """
            SELECT COUNT(*) as total 
            FROM tb_jogos_gerados 
            WHERE id_loterias = ? AND num_concurso = ?
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, numeroConcurso);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void salvarJogosGerados(int idLoteria, List<List<Integer>> jogos, int numeroConcurso) throws Exception {
        // Primeiro verifica se já existem jogos
        if (verificaJogosExistentes(idLoteria, numeroConcurso, jogos)) {
            throw new Exception("Já existem jogos cadastrados para este concurso.");
        }

        String sql = """
            INSERT INTO tb_jogos_gerados 
            (id_loterias, num_concurso, dt_inclusao, dt_aposta, num1, num2, num3, num4, num5, 
             num6, num7, num8, num9, num10, num11, num12, num13, num14, num15)
            VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_DATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false); // Inicia uma transação
            
            try {
                for (List<Integer> jogo : jogos) {
                    stmt.setInt(1, idLoteria);
                    stmt.setInt(2, numeroConcurso);
                    for (int i = 0; i < jogo.size(); i++) {
                        stmt.setInt(i + 3, jogo.get(i));
                    }
                    stmt.addBatch();
                }
                
                stmt.executeBatch();
                conn.commit(); // Confirma a transação
            } catch (Exception e) {
                conn.rollback(); // Desfaz a transação em caso de erro
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    
    @FXML
    private void handleAceitarSugestao() {
        if (jogosGerados == null || jogosGerados.isEmpty()) {
            mostrarAlerta("Atenção", "Não há jogos gerados para aceitar.");
            return;
        }
        
        try {
            String selectedLoteria = loteriaComboBox.getValue();
            int idLoteria = getIdLoteria(selectedLoteria);
            int numeroConcurso = Integer.parseInt(numeroConcursoTextField.getText().trim());
            
            // Salva os jogos gerados no banco de dados
            salvarJogosGerados(idLoteria, jogosGerados, numeroConcurso);
            
            mostrarAlerta("Sucesso", "Jogos salvos com sucesso para o concurso " + numeroConcurso + "!");
            
            // Limpa os jogos após salvar com sucesso
            handleDescartarSugestao();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao salvar jogos", e.getMessage());
        }
    }
    
    @FXML
    private void handleDescartarSugestao() {
        jogosTable.getItems().clear();
        jogosGerados = null;
        valorTotalApostaLabel.setText("Valor Total da Aposta: R$ 0.00");
    }
    
    @FXML
    private void handleFechar() {
        Stage stage = (Stage) jogosTable.getScene().getWindow();
        stage.close();
    }
    
    private int getIdLoteria(String nomeLoteria) throws Exception {
        String sql = "SELECT id_loterias FROM tb_loterias WHERE des_nome = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nomeLoteria);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_loterias");
            } else {
                throw new Exception("Loteria não encontrada");
            }
        }
    }
    
    private double getValorAposta(int idLoteria, int qtNumeros) throws Exception {
        String sql = """
            SELECT vlr_aposta 
            FROM tb_loterias_preco 
            WHERE id_loterias = ? AND qt_numeros_jogados = ?
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, qtNumeros);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("vlr_aposta");
            } else {
                throw new Exception("Preço não encontrado para esta quantidade de números");
            }
        }
    }
    
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    // Classe interna para representar um jogo na tabela
    // Classe Jogo com dois construtores
    public static class Jogo {
        private final int numero;
        private final List<Integer> numeros;
        private int numeroConcurso;
        
        // Construtor original para manter compatibilidade
        public Jogo(int numero, List<Integer> numeros) {
            this.numero = numero;
            this.numeros = numeros;
            this.numeroConcurso = 0; // valor padrão
        }
        
        // Novo construtor com numeroConcurso
        public Jogo(int numero, List<Integer> numeros, int numeroConcurso) {
            this.numero = numero;
            this.numeros = numeros;
            this.numeroConcurso = numeroConcurso;
        }
        
        public int getNumero() {
            return numero;
        }
        
        public String getNumerosFormatados() {
            return numeros.toString()
                .replace("[", "")
                .replace("]", "");
        }
        
        public List<Integer> getNumeros() {
            return numeros;
        }
        
        public int getNumeroConcurso() {
            return numeroConcurso;
        }
        
        public void setNumeroConcurso(int numeroConcurso) {
            this.numeroConcurso = numeroConcurso;
        }
    }
}