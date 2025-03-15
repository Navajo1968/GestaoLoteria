package com.loteriascorp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.sql.*;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.loteriascorp.service.GeradorJogosOptimizado;
import com.loteriascorp.Database;
import com.loteriascorp.Jogo;
import javafx.stage.Stage;  

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
    
    private GeradorJogosOptimizado geradorJogos;
    private List<List<Integer>> jogosGerados;
    
    private void configurarColunas() {
        numeroJogoColumn.setCellValueFactory(new PropertyValueFactory<>("numeroJogo"));
        numerosColumn.setCellValueFactory(new PropertyValueFactory<>("numerosFormatados"));
        
        // Ajustar largura das colunas
        numeroJogoColumn.prefWidthProperty().bind(jogosTable.widthProperty().multiply(0.2));
        numerosColumn.prefWidthProperty().bind(jogosTable.widthProperty().multiply(0.8));
    }
    
    
    @FXML
    public void initialize() {
        geradorJogos = new GeradorJogosOptimizado();
        carregarLoterias();
        configurarColunas();
        
        quantidadeJogosSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 7)
        );
        
        // Adicionar listener para atualizar o número do próximo concurso
        loteriaComboBox.setOnAction(e -> atualizarNumeroConcurso());
    }
    
    @FXML
    private void handleAceitarSugestao() {
        // Por enquanto, apenas fecha a janela
        Stage stage = (Stage) jogosTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleDescartarSugestao() {
        // Limpa a tabela e as análises
        jogosTable.getItems().clear();
        txtAnalise.clear();
        valorTotalApostaLabel.setText("Valor Total da Aposta: R$ 0,00");
    }

    @FXML
    private void handleFechar() {
        Stage stage = (Stage) jogosTable.getScene().getWindow();
        stage.close();
    }
    
    private void carregarLoterias() {
        String sql = "SELECT id_loterias, des_nome FROM tb_loterias ORDER BY des_nome";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            ObservableList<String> loterias = FXCollections.observableArrayList();
            while (rs.next()) {
                loterias.add(rs.getString("des_nome"));
            }
            loteriaComboBox.setItems(loterias);
            
        } catch (SQLException e) {
            logger.error("Erro ao carregar loterias: ", e);
            mostrarErro("Erro", "Não foi possível carregar a lista de loterias.");
        }
    }
    
    private void atualizarNumeroConcurso() {
        if (loteriaComboBox.getValue() == null) return;
        
        String sql = """
            SELECT MAX(num_concurso) as ultimo_concurso 
            FROM tb_historico_jogos 
            WHERE id_loterias = (
                SELECT id_loterias FROM tb_loterias WHERE des_nome = ?
            )
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, loteriaComboBox.getValue());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int ultimoConcurso = rs.getInt("ultimo_concurso");
                numeroConcursoTextField.setText(String.valueOf(ultimoConcurso + 1));
                // Atualizar análise do último concurso realizado
                analisarUltimoConcurso();
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar último concurso: ", e);
        }
    }
    
    private void analisarUltimoConcurso() {
        try {
            int idLoteria = getIdLoteriaSelected();
            int ultimoConcurso = getUltimoConcursoRealizado(idLoteria);
            
            if (ultimoConcurso == 0) {
                txtAnalise.setText("Nenhum concurso encontrado para análise.");
                return;
            }
            
            String sql = """
                SELECT 
                    num_concurso,
                    num1, num2, num3, num4, num5, num6, num7, num8, num9, 
                    num10, num11, num12, num13, num14, num15,
                    dt_jogo
                FROM tb_historico_jogos 
                WHERE id_loterias = ? AND num_concurso = ?
            """;
            
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, idLoteria);
                stmt.setInt(2, ultimoConcurso);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    StringBuilder analise = new StringBuilder();
                    analise.append(String.format("Análise do Concurso %d\n\n", ultimoConcurso));
                    analise.append("Números Sorteados: ");
                    
                    List<Integer> numerosSorteados = new ArrayList<>();
                    for (int i = 1; i <= 15; i++) {
                        numerosSorteados.add(rs.getInt("num" + i));
                    }
                    Collections.sort(numerosSorteados);
                    
                    analise.append(numerosSorteados.toString()).append("\n\n");
                    
                    // Adicionar estatísticas do último concurso
                    Map<String, Double> estatisticas = calcularEstatisticas(numerosSorteados);
                    analise.append("Estatísticas:\n");
                    analise.append(String.format("Números Pares: %.0f%%\n", estatisticas.get("percentualPares") * 100));
                    analise.append(String.format("Média dos Números: %.2f\n", estatisticas.get("media")));
                    analise.append(String.format("Soma dos Números: %.0f\n", estatisticas.get("soma")));
                    
                    txtAnalise.setText(analise.toString());
                }
                
            }
            
        } catch (Exception e) {
            logger.error("Erro ao analisar último concurso: ", e);
            txtAnalise.setText("Erro ao analisar o último concurso.");
        }
    }
    
    private Map<String, Double> calcularEstatisticas(List<Integer> numeros) {
        Map<String, Double> stats = new HashMap<>();
        
        // Calcular percentual de pares
        long pares = numeros.stream().filter(n -> n % 2 == 0).count();
        stats.put("percentualPares", (double) pares / numeros.size());
        
        // Calcular média
        double media = numeros.stream().mapToInt(Integer::intValue).average().orElse(0);
        stats.put("media", media);
        
        // Calcular soma
        double soma = numeros.stream().mapToInt(Integer::intValue).sum();
        stats.put("soma", soma);
        
        return stats;
    }
    
    private int getIdLoteriaSelected() {
        String sql = "SELECT id_loterias FROM tb_loterias WHERE des_nome = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, loteriaComboBox.getValue());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_loterias");
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar ID da loteria: ", e);
        }
        return 0;
    }
    
    private int getUltimoConcursoRealizado(int idLoteria) {
        String sql = "SELECT MAX(num_concurso) as ultimo FROM tb_historico_jogos WHERE id_loterias = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("ultimo");
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar último concurso realizado: ", e);
        }
        return 0;
    }
    
    @FXML
    private void handleGerarProbabilidade() {
        try {
            int idLoteria = getIdLoteriaSelected();
            int quantidade = quantidadeJogosSpinner.getValue();
            
            if (idLoteria == 0) {
                mostrarErro("Erro", "Selecione uma loteria.");
                return;
            }
            
            jogosGerados = geradorJogos.gerarJogos(idLoteria, quantidade);
            
            if (jogosGerados.isEmpty()) {
                mostrarErro("Aviso", "Não foi possível gerar jogos com os critérios atuais.");
                return;
            }
            
            exibirJogosGerados();
            atualizarValorTotal(jogosGerados.size());
            
        } catch (Exception e) {
            logger.error("Erro ao gerar probabilidades: ", e);
            mostrarErro("Erro", "Ocorreu um erro ao gerar as probabilidades.");
        }
    }
    
    private void exibirJogosGerados() {
        ObservableList<Jogo> jogosObservable = FXCollections.observableArrayList();
        for (int i = 0; i < jogosGerados.size(); i++) {
            jogosObservable.add(new Jogo(i + 1, jogosGerados.get(i)));
        }
        jogosTable.setItems(jogosObservable);
    }
    
    private void atualizarValorTotal(int quantidadeJogos) {
        // Assumindo R$ 3,00 por jogo
        double valorTotal = quantidadeJogos * 3.0;
        valorTotalApostaLabel.setText(String.format("Valor Total da Aposta: R$ %.2f", valorTotal));
    }
    
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}