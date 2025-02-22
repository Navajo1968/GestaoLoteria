package com.loteriascorp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class LoteriaPrecoController {
    @FXML
    private TableView<LoteriaPreco> tabelaLoteriasPreco;
    @FXML
    private TableColumn<LoteriaPreco, String> colunaNomeLoteria;
    @FXML
    private TableColumn<LoteriaPreco, Integer> colunaIdLoteriasPreco;
    @FXML
    private TableColumn<LoteriaPreco, Integer> colunaQtNumerosJogados;
    @FXML
    private TableColumn<LoteriaPreco, Double> colunaVlrAposta;
    @FXML
    private ComboBox<String> comboBoxNomeLoteria;
    @FXML
    private TextField txtQtNumerosJogados;
    @FXML
    private TextField txtVlrAposta;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnNovo;
    @FXML
    private Button btnExcluir;

    private ObservableList<LoteriaPreco> loteriasPreco = FXCollections.observableArrayList();
    private LoteriaPreco loteriaPrecoSelecionada;
    private Map<String, Integer> loteriasMap = new HashMap<>();

    @FXML
    public void initialize() {
        colunaNomeLoteria.setCellValueFactory(new PropertyValueFactory<>("nomeLoteria"));
        colunaIdLoteriasPreco.setCellValueFactory(new PropertyValueFactory<>("idLoteriasPreco"));
        colunaQtNumerosJogados.setCellValueFactory(new PropertyValueFactory<>("qtNumerosJogados"));
        colunaVlrAposta.setCellValueFactory(new PropertyValueFactory<>("vlrAposta"));

        carregarLoterias();
        carregarLoteriasPreco();

        tabelaLoteriasPreco.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newSelection) -> {
            if (newSelection != null) {
                loteriaPrecoSelecionada = newSelection;
                preencherCampos(loteriaPrecoSelecionada);
            }
        });
    }

    private void carregarLoterias() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_loterias, des_nome FROM tb_loterias");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idLoterias = rs.getInt("id_loterias");
                String nomeLoteria = rs.getString("des_nome");
                loteriasMap.put(nomeLoteria, idLoterias);
                comboBoxNomeLoteria.getItems().add(nomeLoteria);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarLoteriasPreco() {
        loteriasPreco.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT lp.*, l.des_nome FROM tb_loterias_preco lp JOIN tb_loterias l ON lp.id_loterias = l.id_loterias");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LoteriaPreco loteriaPreco = new LoteriaPreco();
                loteriaPreco.setIdLoterias(rs.getInt("id_loterias"));
                loteriaPreco.setIdLoteriasPreco(rs.getInt("id_loterias_preco"));
                loteriaPreco.setQtNumerosJogados(rs.getInt("qt_numeros_jogados"));
                loteriaPreco.setVlrAposta(rs.getDouble("vlr_aposta"));
                loteriaPreco.setNomeLoteria(rs.getString("des_nome"));
                loteriasPreco.add(loteriaPreco);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tabelaLoteriasPreco.setItems(loteriasPreco);
    }

    private void preencherCampos(LoteriaPreco loteriaPreco) {
        comboBoxNomeLoteria.setValue(loteriaPreco.getNomeLoteria());
        txtQtNumerosJogados.setText(String.valueOf(loteriaPreco.getQtNumerosJogados()));
        txtVlrAposta.setText(String.valueOf(loteriaPreco.getVlrAposta()));
    }

    @FXML
    private void salvarLoteriaPreco() {
        String nomeLoteria = comboBoxNomeLoteria.getValue();
        int idLoterias = loteriasMap.get(nomeLoteria);
        int qtNumerosJogados = Integer.parseInt(txtQtNumerosJogados.getText());
        double vlrAposta = Double.parseDouble(txtVlrAposta.getText().replace(',', '.'));

        if (loteriaPrecoSelecionada != null) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE tb_loterias_preco SET id_loterias = ?, qt_numeros_jogados = ?, vlr_aposta = ? WHERE id_loterias_preco = ?")) {
                stmt.setInt(1, idLoterias);
                stmt.setInt(2, qtNumerosJogados);
                stmt.setDouble(3, vlrAposta);
                stmt.setInt(4, loteriaPrecoSelecionada.getIdLoteriasPreco());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO tb_loterias_preco (id_loterias, qt_numeros_jogados, vlr_aposta) VALUES (?, ?, ?)")) {
                stmt.setInt(1, idLoterias);
                stmt.setInt(2, qtNumerosJogados);
                stmt.setDouble(3, vlrAposta);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        carregarLoteriasPreco();
        limparCampos();
    }

    @FXML
    private void novoLoteriaPreco() {
        limparCampos();
    }

    @FXML
    private void excluirLoteriaPreco() {
        LoteriaPreco loteriaPreco = tabelaLoteriasPreco.getSelectionModel().getSelectedItem();
        if (loteriaPreco != null) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_loterias_preco WHERE id_loterias_preco = ?")) {
                stmt.setInt(1, loteriaPreco.getIdLoteriasPreco());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            carregarLoteriasPreco();
            limparCampos();
        }
    }

    private void limparCampos() {
        comboBoxNomeLoteria.getSelectionModel().clearSelection();
        txtQtNumerosJogados.clear();
        txtVlrAposta.clear();
        loteriaPrecoSelecionada = null;
    }
}