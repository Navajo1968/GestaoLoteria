package com.loteriascorp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class LoteriaHistoricoController {
    @FXML
    private TableView<LoteriaHistorico> tabelaLoteriasHistorico;
    @FXML
    private TableColumn<LoteriaHistorico, String> colunaNomeLoteria;
    @FXML
    private TableColumn<LoteriaHistorico, LocalDate> colunaDtJogo;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum1;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum2;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum3;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum4;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum5;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum6;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum7;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum8;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum9;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum10;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum11;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum12;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum13;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum14;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNum15;
    @FXML
    private TableColumn<LoteriaHistorico, Integer> colunaNumConcurso;
    @FXML
    private ComboBox<String> comboBoxNomeLoteria;
    @FXML
    private DatePicker datePickerDtJogo;
    @FXML
    private TextField txtNum1;
    @FXML
    private TextField txtNum2;
    @FXML
    private TextField txtNum3;
    @FXML
    private TextField txtNum4;
    @FXML
    private TextField txtNum5;
    @FXML
    private TextField txtNum6;
    @FXML
    private TextField txtNum7;
    @FXML
    private TextField txtNum8;
    @FXML
    private TextField txtNum9;
    @FXML
    private TextField txtNum10;
    @FXML
    private TextField txtNum11;
    @FXML
    private TextField txtNum12;
    @FXML
    private TextField txtNum13;
    @FXML
    private TextField txtNum14;
    @FXML
    private TextField txtNum15;
    @FXML
    private TextField txtNumConcurso;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnNovo;
    @FXML
    private Button btnExcluir;

    private ObservableList<LoteriaHistorico> loteriasHistorico = FXCollections.observableArrayList();
    private LoteriaHistorico loteriaHistoricoSelecionada;
    private Map<String, Integer> loteriasMap = new HashMap<>();
    private int qtNumeros;

    @FXML
    public void initialize() {
        colunaNomeLoteria.setCellValueFactory(new PropertyValueFactory<>("nomeLoteria"));
        colunaDtJogo.setCellValueFactory(new PropertyValueFactory<>("dtJogo"));
        colunaNum1.setCellValueFactory(new PropertyValueFactory<>("num1"));
        colunaNum2.setCellValueFactory(new PropertyValueFactory<>("num2"));
        colunaNum3.setCellValueFactory(new PropertyValueFactory<>("num3"));
        colunaNum4.setCellValueFactory(new PropertyValueFactory<>("num4"));
        colunaNum5.setCellValueFactory(new PropertyValueFactory<>("num5"));
        colunaNum6.setCellValueFactory(new PropertyValueFactory<>("num6"));
        colunaNum7.setCellValueFactory(new PropertyValueFactory<>("num7"));
        colunaNum8.setCellValueFactory(new PropertyValueFactory<>("num8"));
        colunaNum9.setCellValueFactory(new PropertyValueFactory<>("num9"));
        colunaNum10.setCellValueFactory(new PropertyValueFactory<>("num10"));
        colunaNum11.setCellValueFactory(new PropertyValueFactory<>("num11"));
        colunaNum12.setCellValueFactory(new PropertyValueFactory<>("num12"));
        colunaNum13.setCellValueFactory(new PropertyValueFactory<>("num13"));
        colunaNum14.setCellValueFactory(new PropertyValueFactory<>("num14"));
        colunaNum15.setCellValueFactory(new PropertyValueFactory<>("num15"));
        colunaNumConcurso.setCellValueFactory(new PropertyValueFactory<>("numConcurso"));

        carregarLoterias();
        carregarLoteriasHistorico();

        tabelaLoteriasHistorico.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newSelection) -> {
            if (newSelection != null) {
                loteriaHistoricoSelecionada = newSelection;
                preencherCampos(loteriaHistoricoSelecionada);
            }
        });

        comboBoxNomeLoteria.valueProperty().addListener((observable, oldvalue, newVal) -> carregarQuantidadeNumeros(newVal));
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

    private void carregarQuantidadeNumeros(String nomeLoteria) {
        if (nomeLoteria == null) {
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT qt_numeros FROM tb_loterias WHERE des_nome = ?")) {
            stmt.setString(1, nomeLoteria);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                qtNumeros = rs.getInt("qt_numeros");
                ajustarCamposNumeros(qtNumeros);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ajustarCamposNumeros(int qtNumeros) {
        TextField[] camposNumeros = {
                txtNum1, txtNum2, txtNum3, txtNum4, txtNum5, txtNum6, txtNum7, txtNum8, txtNum9, txtNum10,
                txtNum11, txtNum12, txtNum13, txtNum14, txtNum15, 
        };

        for (int i = 0; i < camposNumeros.length; i++) {
            camposNumeros[i].setDisable(i >= qtNumeros);
        }
    }

    private void carregarLoteriasHistorico() {
        loteriasHistorico.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT hj.*, l.des_nome FROM tb_historico_jogos hj " +
                     "JOIN tb_loterias l ON hj.id_loterias = l.id_loterias");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LoteriaHistorico loteriaHistorico = new LoteriaHistorico();
                loteriaHistorico.setIdLoterias(rs.getInt("id_loterias"));
                loteriaHistorico.setDtJogo(rs.getDate("dt_jogo").toLocalDate());
                loteriaHistorico.setNum1(rs.getInt("num1"));
                loteriaHistorico.setNum2(rs.getInt("num2"));
                loteriaHistorico.setNum3(rs.getInt("num3"));
                loteriaHistorico.setNum4(rs.getInt("num4"));
                loteriaHistorico.setNum5(rs.getInt("num5"));
                loteriaHistorico.setNum6(rs.getInt("num6"));
                loteriaHistorico.setNum7(rs.getInt("num7"));
                loteriaHistorico.setNum8(rs.getInt("num8"));
                loteriaHistorico.setNum9(rs.getInt("num9"));
                loteriaHistorico.setNum10(rs.getInt("num10"));
                loteriaHistorico.setNum11(rs.getInt("num11"));
                loteriaHistorico.setNum12(rs.getInt("num12"));
                loteriaHistorico.setNum13(rs.getInt("num13"));
                loteriaHistorico.setNum14(rs.getInt("num14"));
                loteriaHistorico.setNum15(rs.getInt("num15"));
                loteriaHistorico.setNomeLoteria(rs.getString("des_nome"));
                loteriaHistorico.setNumConcurso(rs.getInt("num_concurso"));
                loteriasHistorico.add(loteriaHistorico);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tabelaLoteriasHistorico.setItems(loteriasHistorico);
    }

    private void preencherCampos(LoteriaHistorico loteriaHistorico) {
        comboBoxNomeLoteria.setValue(loteriaHistorico.getNomeLoteria());
        datePickerDtJogo.setValue(loteriaHistorico.getDtJogo());
        txtNum1.setText(String.valueOf(loteriaHistorico.getNum1()));
        txtNum2.setText(String.valueOf(loteriaHistorico.getNum2()));
        txtNum3.setText(String.valueOf(loteriaHistorico.getNum3()));
        txtNum4.setText(String.valueOf(loteriaHistorico.getNum4()));
        txtNum5.setText(String.valueOf(loteriaHistorico.getNum5()));
        txtNum6.setText(String.valueOf(loteriaHistorico.getNum6()));
        txtNum7.setText(String.valueOf(loteriaHistorico.getNum7()));
        txtNum8.setText(String.valueOf(loteriaHistorico.getNum8()));
        txtNum9.setText(String.valueOf(loteriaHistorico.getNum9()));
        txtNum10.setText(String.valueOf(loteriaHistorico.getNum10()));
        txtNum11.setText(String.valueOf(loteriaHistorico.getNum11()));
        txtNum12.setText(String.valueOf(loteriaHistorico.getNum12()));
        txtNum13.setText(String.valueOf(loteriaHistorico.getNum13()));
        txtNum14.setText(String.valueOf(loteriaHistorico.getNum14()));
        txtNum15.setText(String.valueOf(loteriaHistorico.getNum15()));
        txtNumConcurso.setText(String.valueOf(loteriaHistorico.getNumConcurso()));
    }

    @FXML
    private void salvarLoteriaHistorico() {
        String nomeLoteria = comboBoxNomeLoteria.getValue();
        int idLoterias = loteriasMap.get(nomeLoteria);
        LocalDate dtJogo = datePickerDtJogo.getValue();
        int num1 = Integer.parseInt(txtNum1.getText());
        int num2 = Integer.parseInt(txtNum2.getText());
        int num3 = Integer.parseInt(txtNum3.getText());
        int num4 = Integer.parseInt(txtNum4.getText());
        int num5 = Integer.parseInt(txtNum5.getText());
        int num6 = Integer.parseInt(txtNum6.getText());
        int num7 = Integer.parseInt(txtNum7.getText());
        int num8 = Integer.parseInt(txtNum8.getText());
        int num9 = Integer.parseInt(txtNum9.getText());
        int num10 = Integer.parseInt(txtNum10.getText());
        int num11 = Integer.parseInt(txtNum11.getText());
        int num12 = Integer.parseInt(txtNum12.getText());
        int num13 = Integer.parseInt(txtNum13.getText());
        int num14 = Integer.parseInt(txtNum14.getText());
        int num15 = Integer.parseInt(txtNum15.getText());
        int numconcurso = Integer.parseInt(txtNumConcurso.getText());

        if (loteriaHistoricoSelecionada != null) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE tb_historico_jogos SET id_loterias = ?, dt_jogo = ?, num1 = ?, num2 = ?, num3 = ?, num4 = ?, num5 = ?, num6 = ?, "
                 		+ "num7 = ?, num8 = ?, num9 = ?, num10 = ?, num11 = ?, num12 = ?, num13 = ?, num14 = ?, num15 = ?, num_concurso = ? WHERE id_loterias = ? AND dt_jogo = ?")) {
                stmt.setInt(1, idLoterias);
                stmt.setDate(2, java.sql.Date.valueOf(dtJogo));
                stmt.setInt(3, num1);
                stmt.setInt(4, num2);
                stmt.setInt(5, num3);
                stmt.setInt(6, num4);
                stmt.setInt(7, num5);
                stmt.setInt(8, num6);
                stmt.setInt(9, num7);
                stmt.setInt(10, num8);
                stmt.setInt(11, num9);
                stmt.setInt(12, num10);
                stmt.setInt(13, num11);
                stmt.setInt(14, num12);
                stmt.setInt(15, num13);
                stmt.setInt(16, num14);
                stmt.setInt(17, num15);
                stmt.setInt(18, numconcurso);
                stmt.setInt(19, loteriaHistoricoSelecionada.getIdLoterias());
                stmt.setDate(20, java.sql.Date.valueOf(loteriaHistoricoSelecionada.getDtJogo()));
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO tb_historico_jogos (id_loterias, dt_jogo, num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, num11, num12, num13, num14, num15, num_concurso) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setInt(1, idLoterias);
                stmt.setDate(2, java.sql.Date.valueOf(dtJogo));
                stmt.setInt(3, num1);
                stmt.setInt(4, num2);
                stmt.setInt(5, num3);
                stmt.setInt(6, num4);
                stmt.setInt(7, num5);
                stmt.setInt(8, num6);
                stmt.setInt(9, num7);
                stmt.setInt(10, num8);
                stmt.setInt(11, num9);
                stmt.setInt(12, num10);
                stmt.setInt(13, num11);
                stmt.setInt(14, num12);
                stmt.setInt(15, num13);
                stmt.setInt(16, num14);
                stmt.setInt(17, num15);
                stmt.setInt(18, numconcurso);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        carregarLoteriasHistorico();
        limparCampos();
    }

    @FXML
    private void novoLoteriaHistorico() {
        limparCampos();
    }

    @FXML
    private void excluirLoteriaHistorico() {
        LoteriaHistorico loteriaHistorico = tabelaLoteriasHistorico.getSelectionModel().getSelectedItem();
        if (loteriaHistorico != null) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_historico_jogos WHERE id_loterias = ? AND dt_jogo = ?")) {
                stmt.setInt(1, loteriaHistorico.getIdLoterias());
                stmt.setDate(2, java.sql.Date.valueOf(loteriaHistorico.getDtJogo()));
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            carregarLoteriasHistorico();
            limparCampos();
        }
    }

    private void limparCampos() {
        comboBoxNomeLoteria.getSelectionModel().clearSelection();
        datePickerDtJogo.setValue(null);
        txtNum1.clear();
        txtNum2.clear();
        txtNum3.clear();
        txtNum4.clear();
        txtNum5.clear();
        txtNum6.clear();
        txtNum7.clear();
        txtNum8.clear();
        txtNum9.clear();
        txtNum10.clear();
        txtNum11.clear();
        txtNum12.clear();
        txtNum13.clear();
        txtNum14.clear();
        txtNum15.clear();
        txtNumConcurso.clear();
        loteriaHistoricoSelecionada = null;
    }
}