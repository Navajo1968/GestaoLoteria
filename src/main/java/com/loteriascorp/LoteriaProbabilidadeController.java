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

public class LoteriaProbabilidadeController {
    @FXML
    private TableView<LoteriaProbabilidade> tabelaLoteriasProbabilidade;
    @FXML
    private TableColumn<LoteriaProbabilidade, Integer> colunaIdLoteriasProbabilidade;
    @FXML
    private TableColumn<LoteriaProbabilidade, String> colunaNomeLoteria;
    @FXML
    private TableColumn<LoteriaProbabilidade, String> colunaNomeLoteriaPreco;
    @FXML
    private TableColumn<LoteriaProbabilidade, Integer> colunaQtNumerosAcertos;
    @FXML
    private TableColumn<LoteriaProbabilidade, Integer> colunaQtNumerosJogados;
    @FXML
    private TableColumn<LoteriaProbabilidade, Integer> colunaQtProbabilidade;
    @FXML
    private TableColumn<LoteriaProbabilidade, Double> colunaVlrFatorPremiacao;
    @FXML
    private ComboBox<String> comboBoxNomeLoteria;
    @FXML
    private ComboBox<String> comboBoxNomeLoteriaPreco;
    @FXML
    private TextField txtQtNumerosAcertos;
    @FXML
    private TextField txtQtNumerosJogados;
    @FXML
    private TextField txtQtProbabilidade;
    @FXML
    private TextField txtVlrFatorPremiacao;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnNovo;
    @FXML
    private Button btnExcluir;

    private ObservableList<LoteriaProbabilidade> loteriasProbabilidade = FXCollections.observableArrayList();
    private LoteriaProbabilidade loteriaProbabilidadeSelecionada;
    private Map<String, Integer> loteriasMap = new HashMap<>();
    private Map<String, Integer> loteriasPrecoMap = new HashMap<>();

    @FXML
    public void initialize() {
        colunaIdLoteriasProbabilidade.setCellValueFactory(new PropertyValueFactory<>("idLoteriasProbabilidade"));
        colunaNomeLoteria.setCellValueFactory(new PropertyValueFactory<>("nomeLoteria"));
        colunaNomeLoteriaPreco.setCellValueFactory(new PropertyValueFactory<>("nomeLoteriaPreco"));
        colunaQtNumerosAcertos.setCellValueFactory(new PropertyValueFactory<>("qtNumerosAcertos"));
        colunaQtNumerosJogados.setCellValueFactory(new PropertyValueFactory<>("qtNumerosJogados"));
        colunaQtProbabilidade.setCellValueFactory(new PropertyValueFactory<>("qtProbabilidade"));
        colunaVlrFatorPremiacao.setCellValueFactory(new PropertyValueFactory<>("vlrFatorPremiacao"));

        carregarLoterias();
        carregarLoteriasProbabilidade();

        tabelaLoteriasProbabilidade.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newSelection) -> {
            if (newSelection != null) {
                loteriaProbabilidadeSelecionada = newSelection;
                preencherCampos(loteriaProbabilidadeSelecionada);
            }
        });

        comboBoxNomeLoteria.valueProperty().addListener((observable, oldValue, newVal) -> carregarLoteriasPreco(newVal));
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

    private void carregarLoteriasPreco(String nomeLoteria) {
        comboBoxNomeLoteriaPreco.getItems().clear();
        loteriasPrecoMap.clear();

        if (nomeLoteria == null) {
            return;
        }

        int idLoterias = loteriasMap.get(nomeLoteria);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_loterias_preco, qt_numeros_jogados FROM tb_loterias_preco WHERE id_loterias = ?")) {
            stmt.setInt(1, idLoterias);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idLoteriasPreco = rs.getInt("id_loterias_preco");
                String nomeLoteriaPreco = "Qt. Números: " + rs.getInt("qt_numeros_jogados");
                loteriasPrecoMap.put(nomeLoteriaPreco, idLoteriasPreco);
                comboBoxNomeLoteriaPreco.getItems().add(nomeLoteriaPreco);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarLoteriasProbabilidade() {
        loteriasProbabilidade.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT lp.*, l.des_nome, lpreco.qt_numeros_jogados AS des_nome_preco " +
                     "FROM tb_loterias_probabilidade lp " +
                     "JOIN tb_loterias l ON lp.id_loterias = l.id_loterias " +
                     "JOIN tb_loterias_preco lpreco ON lp.id_loterias_preco = lpreco.id_loterias_preco");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LoteriaProbabilidade loteriaProbabilidade = new LoteriaProbabilidade();
                loteriaProbabilidade.setIdLoterias(rs.getInt("id_loterias"));
                loteriaProbabilidade.setIdLoteriasPreco(rs.getInt("id_loterias_preco"));
                loteriaProbabilidade.setIdLoteriasProbabilidade(rs.getInt("id_loterias_probabilidade"));
                loteriaProbabilidade.setQtNumerosAcertos(rs.getInt("qt_numeros_acertos"));
                loteriaProbabilidade.setQtNumerosJogados(rs.getInt("qt_numeros_jogados"));
                loteriaProbabilidade.setQtProbabilidade(rs.getInt("qt_probabilidade"));
                loteriaProbabilidade.setVlrFatorPremiacao(rs.getDouble("vlr_fator_premiacao"));
                loteriaProbabilidade.setNomeLoteria(rs.getString("des_nome"));
                loteriaProbabilidade.setNomeLoteriaPreco("Qt. Números: " + rs.getInt("des_nome_preco"));
                loteriasProbabilidade.add(loteriaProbabilidade);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tabelaLoteriasProbabilidade.setItems(loteriasProbabilidade);
    }

    private void preencherCampos(LoteriaProbabilidade loteriaProbabilidade) {
        comboBoxNomeLoteria.setValue(loteriaProbabilidade.getNomeLoteria());
        carregarLoteriasPreco(loteriaProbabilidade.getNomeLoteria());
        comboBoxNomeLoteriaPreco.setValue(loteriaProbabilidade.getNomeLoteriaPreco());
        txtQtNumerosAcertos.setText(String.valueOf(loteriaProbabilidade.getQtNumerosAcertos()));
        txtQtNumerosJogados.setText(String.valueOf(loteriaProbabilidade.getQtNumerosJogados()));
        txtQtProbabilidade.setText(String.valueOf(loteriaProbabilidade.getQtProbabilidade()));
        txtVlrFatorPremiacao.setText(String.valueOf(loteriaProbabilidade.getVlrFatorPremiacao()).replace('.', ','));
    }

    @FXML
    private void salvarLoteriaProbabilidade() {
        String nomeLoteria = comboBoxNomeLoteria.getValue();
        int idLoterias = loteriasMap.get(nomeLoteria);
        String nomeLoteriaPreco = comboBoxNomeLoteriaPreco.getValue();
        int idLoteriasPreco = loteriasPrecoMap.get(nomeLoteriaPreco);
        int qtNumerosAcertos = Integer.parseInt(txtQtNumerosAcertos.getText());
        int qtNumerosJogados = Integer.parseInt(txtQtNumerosJogados.getText());
        int qtProbabilidade = Integer.parseInt(txtQtProbabilidade.getText());
        double vlrFatorPremiacao = Double.parseDouble(txtVlrFatorPremiacao.getText().replace(',', '.'));

        if (loteriaProbabilidadeSelecionada != null) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE tb_loterias_probabilidade SET id_loterias = ?, id_loterias_preco = ?, qt_numeros_acertos = ?, qt_numeros_jogados = ?, qt_probabilidade = ?, vlr_fator_premiacao = ? WHERE id_loterias_probabilidade = ?")) {
                stmt.setInt(1, idLoterias);
                stmt.setInt(2, idLoteriasPreco);
                stmt.setInt(3, qtNumerosAcertos);
                stmt.setInt(4, qtNumerosJogados);
                stmt.setInt(5, qtProbabilidade);
                stmt.setDouble(6, vlrFatorPremiacao);
                stmt.setInt(7, loteriaProbabilidadeSelecionada.getIdLoteriasProbabilidade());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO tb_loterias_probabilidade (id_loterias, id_loterias_preco, qt_numeros_acertos, qt_numeros_jogados, qt_probabilidade, vlr_fator_premiacao) VALUES (?, ?, ?, ?, ?, ?)")) {
                stmt.setInt(1, idLoterias);
                stmt.setInt(2, idLoteriasPreco);
                stmt.setInt(3, qtNumerosAcertos);
                stmt.setInt(4, qtNumerosJogados);
                stmt.setInt(5, qtProbabilidade);
                stmt.setDouble(6, vlrFatorPremiacao);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        carregarLoteriasProbabilidade();
        limparCampos();
    }

    @FXML
    private void novoLoteriaProbabilidade() {
        limparCampos();
    }

    @FXML
    private void excluirLoteriaProbabilidade() {
        LoteriaProbabilidade loteriaProbabilidade = tabelaLoteriasProbabilidade.getSelectionModel().getSelectedItem();
        if (loteriaProbabilidade != null) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_loterias_probabilidade WHERE id_loterias_probabilidade = ?")) {
                stmt.setInt(1, loteriaProbabilidade.getIdLoteriasProbabilidade());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            carregarLoteriasProbabilidade();
            limparCampos();
        }
    }

    private void limparCampos() {
        comboBoxNomeLoteria.getSelectionModel().clearSelection();
        comboBoxNomeLoteriaPreco.getSelectionModel().clearSelection();
        txtQtNumerosAcertos.clear();
        txtQtNumerosJogados.clear();
        txtQtProbabilidade.clear();
        txtVlrFatorPremiacao.clear();
        loteriaProbabilidadeSelecionada = null;
    }
}