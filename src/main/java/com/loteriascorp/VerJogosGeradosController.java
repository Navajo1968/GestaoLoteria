package com.loteriascorp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VerJogosGeradosController {
    @FXML
    private ComboBox<String> comboBoxLoteria;
    @FXML
    private TextField textFieldConcurso;
    @FXML
    private Button buttonConferir;
    @FXML
    private TableView<JogoGerado> tableViewJogosGerados;
    @FXML
    private TableColumn<JogoGerado, Integer> columnNumeroJogo;
    @FXML
    private TableColumn<JogoGerado, String> columnNumeros;
    @FXML
    private TableColumn<JogoGerado, Integer> columnTotAcertos;

    private ObservableList<JogoGerado> jogosGerados = FXCollections.observableArrayList();
    private List<Integer> numerosSorteados = new ArrayList<>();

    @FXML
    public void initialize() {
        columnNumeroJogo.setCellValueFactory(new PropertyValueFactory<>("numeroJogo"));
        columnNumeros.setCellValueFactory(new PropertyValueFactory<>("numeros"));
        columnTotAcertos.setCellValueFactory(new PropertyValueFactory<>("totAcertos"));
        carregarLoterias();
    }

    private void carregarLoterias() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT des_nome FROM tb_loterias");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                comboBoxLoteria.getItems().add(rs.getString("des_nome"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConferir() {
        String nomeLoteria = comboBoxLoteria.getValue();
        String concurso = textFieldConcurso.getText();

        if (nomeLoteria == null || concurso.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Atenção");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecione uma loteria e informe o número do concurso.");
            alert.showAndWait();
            return;
        }

        int idLoteria = getIdLoteria(nomeLoteria);
        int numConcurso = Integer.parseInt(concurso);

        carregarNumerosSorteados(idLoteria, numConcurso);
        carregarJogosGerados(idLoteria, numConcurso);
        conferirJogos(idLoteria, numConcurso);
    }

    private int getIdLoteria(String nomeLoteria) {
        int idLoteria = -1;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_loterias FROM tb_loterias WHERE des_nome = ?")) {
            stmt.setString(1, nomeLoteria);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                idLoteria = rs.getInt("id_loterias");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idLoteria;
    }

    private void carregarNumerosSorteados(int idLoteria, int numConcurso) {
        numerosSorteados.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, num11, num12, num13, num14, num15 FROM tb_historico_jogos WHERE id_loterias = ? AND num_concurso = ?")) {
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, numConcurso);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                for (int i = 1; i <= 15; i++) {
                    numerosSorteados.add(rs.getInt("num" + i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarJogosGerados(int idLoteria, int numConcurso) {
        jogosGerados.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT numero_jogo, num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, num11, num12, num13, num14, num15, tot_acertos FROM tb_jogos_gerados WHERE id_loterias = ? AND num_concurso = ?")) {
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, numConcurso);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                List<Integer> numeros = new ArrayList<>();
                for (int i = 1; i <= 15; i++) {
                    numeros.add(rs.getInt("num" + i));
                }
                JogoGerado jogoGerado = new JogoGerado(rs.getInt("numero_jogo"), numeros, rs.getInt("tot_acertos"));
                jogosGerados.add(jogoGerado);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableViewJogosGerados.setItems(jogosGerados);
    }

    private void conferirJogos(int idLoteria, int numConcurso) {
        for (JogoGerado jogo : jogosGerados) {
            int totAcertos = 0;
            for (int numero : jogo.getNumeros()) {
                if (numerosSorteados.contains(numero)) {
                    totAcertos++;
                }
            }
            jogo.setTotAcertos(totAcertos);
            atualizarTotAcertos(jogo.getNumeroJogo(), totAcertos, idLoteria, numConcurso);
        }
        tableViewJogosGerados.refresh();
    }

    private void atualizarTotAcertos(int numeroJogo, int totAcertos, int idLoteria, int numConcurso) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_jogos_gerados SET tot_acertos = ? WHERE numero_jogo = ? AND id_loterias = ? AND num_concurso = ?")) {
            stmt.setInt(1, totAcertos);
            stmt.setInt(2, numeroJogo);
            stmt.setInt(3, idLoteria);
            stmt.setInt(4, numConcurso);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}