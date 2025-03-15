package com.loteriascorp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class LoteriaController {
    @FXML
    private TableView<Loteria> tabelaLoterias;
    @FXML
    private TableColumn<Loteria, Integer> colunaId;
    @FXML
    private TableColumn<Loteria, String> colunaNome;
    @FXML
    private TableColumn<Loteria, String> colunaContexto;
    @FXML
    private TableColumn<Loteria, Integer> colunaQtNumeros;
    @FXML
    private TableColumn<Loteria, String> colunaDtInclusao;
    @FXML
    private TableColumn<Loteria, Boolean> colunaSeg;
    @FXML
    private TableColumn<Loteria, Boolean> colunaTer;
    @FXML
    private TableColumn<Loteria, Boolean> colunaQua;
    @FXML
    private TableColumn<Loteria, Boolean> colunaQui;
    @FXML
    private TableColumn<Loteria, Boolean> colunaSex;
    @FXML
    private TableColumn<Loteria, Boolean> colunaSab;
    @FXML
    private TableColumn<Loteria, Boolean> colunaDom;
    @FXML
    private TableColumn<Loteria, String> colunaHorarioSorteio;
    @FXML
    private TextField txtNome;
    @FXML
    private TextArea txtContexto;
    @FXML
    private TextField txtQtNumeros;
    @FXML
    private CheckBox cbSeg;
    @FXML
    private CheckBox cbTer;
    @FXML
    private CheckBox cbQua;
    @FXML
    private CheckBox cbQui;
    @FXML
    private CheckBox cbSex;
    @FXML
    private CheckBox cbSab;
    @FXML
    private CheckBox cbDom;
    @FXML
    private TextField txtHorarioSorteio;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnNovo;
    @FXML
    private Button btnExcluir;

    private ObservableList<Loteria> loterias = FXCollections.observableArrayList();
    private Loteria loteriaSelecionada;

    @FXML
    public void initialize() {
        colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaContexto.setCellValueFactory(new PropertyValueFactory<>("contexto"));
        colunaQtNumeros.setCellValueFactory(new PropertyValueFactory<>("qtNumeros"));
        colunaDtInclusao.setCellValueFactory(new PropertyValueFactory<>("dtInclusao"));
        colunaSeg.setCellValueFactory(new PropertyValueFactory<>("seg"));
        colunaTer.setCellValueFactory(new PropertyValueFactory<>("ter"));
        colunaQua.setCellValueFactory(new PropertyValueFactory<>("qua"));
        colunaQui.setCellValueFactory(new PropertyValueFactory<>("qui"));
        colunaSex.setCellValueFactory(new PropertyValueFactory<>("sex"));
        colunaSab.setCellValueFactory(new PropertyValueFactory<>("sab"));
        colunaDom.setCellValueFactory(new PropertyValueFactory<>("dom"));
        colunaHorarioSorteio.setCellValueFactory(new PropertyValueFactory<>("horarioSorteio"));

        carregarLoterias();

        tabelaLoterias.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newSelection) -> {
            if (newSelection != null) {
                loteriaSelecionada = newSelection;
                preencherCampos(loteriaSelecionada);
            }
        });
    }

    private void carregarLoterias() {
        loterias.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tb_loterias");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Loteria loteria = new Loteria();
                loteria.setId(rs.getInt("id_loterias"));
                loteria.setNome(rs.getString("des_nome"));
                loteria.setContexto(rs.getString("des_contexto"));
                loteria.setQtNumeros(rs.getInt("qt_numeros"));
                loteria.setDtInclusao(rs.getString("dt_inclusao"));
                loteria.setSeg(rs.getBoolean("seg"));
                loteria.setTer(rs.getBoolean("ter"));
                loteria.setQua(rs.getBoolean("qua"));
                loteria.setQui(rs.getBoolean("qui"));
                loteria.setSex(rs.getBoolean("sex"));
                loteria.setSab(rs.getBoolean("sab"));
                loteria.setDom(rs.getBoolean("dom"));
                loteria.setHorarioSorteio(rs.getString("horario_sorteio"));
                loterias.add(loteria);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tabelaLoterias.setItems(loterias);
    }

    private void preencherCampos(Loteria loteria) {
        txtNome.setText(loteria.getNome());
        txtContexto.setText(loteria.getContexto());
        txtQtNumeros.setText(String.valueOf(loteria.getQtNumeros()));
        cbSeg.setSelected(loteria.isSeg());
        cbTer.setSelected(loteria.isTer());
        cbQua.setSelected(loteria.isQua());
        cbQui.setSelected(loteria.isQui());
        cbSex.setSelected(loteria.isSex());
        cbSab.setSelected(loteria.isSab());
        cbDom.setSelected(loteria.isDom());
        txtHorarioSorteio.setText(loteria.getHorarioSorteio());
    }

    @FXML
    private void salvarLoteria() {
        try {
            // Validações básicas
            if (txtNome.getText().trim().isEmpty()) {
                mostrarErro("Nome obrigatório", "Por favor, informe o nome da loteria.");
                return;
            }

            if (txtQtNumeros.getText().trim().isEmpty()) {
                mostrarErro("Quantidade de números obrigatória", "Por favor, informe a quantidade de números.");
                return;
            }

            String nome = txtNome.getText();
            String contexto = txtContexto.getText();
            int qtNumeros;
            
            try {
                qtNumeros = Integer.parseInt(txtQtNumeros.getText());
                if (qtNumeros <= 0) {
                    mostrarErro("Valor inválido", "A quantidade de números deve ser maior que zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarErro("Valor inválido", "Por favor, informe um número válido para a quantidade de números.");
                return;
            }

            boolean seg = cbSeg.isSelected();
            boolean ter = cbTer.isSelected();
            boolean qua = cbQua.isSelected();
            boolean qui = cbQui.isSelected();
            boolean sex = cbSex.isSelected();
            boolean sab = cbSab.isSelected();
            boolean dom = cbDom.isSelected();
            String horarioSorteioStr = txtHorarioSorteio.getText();

            // Validação do horário
            if (horarioSorteioStr.trim().isEmpty()) {
                mostrarErro("Horário obrigatório", "Por favor, informe o horário do sorteio.");
                return;
            }

            LocalTime horarioSorteio;
            try {
                horarioSorteio = LocalTime.parse(horarioSorteioStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (DateTimeParseException e) {
                mostrarErro("Horário inválido", "Por favor, informe o horário no formato HH:mm:ss");
                return;
            }

            // Salvar no banco de dados
            try (Connection conn = Database.getConnection()) {
                String sql;
                PreparedStatement stmt;

                if (loteriaSelecionada != null) {
                    // Update
                    sql = """
                        UPDATE tb_loterias 
                        SET des_nome = ?, des_contexto = ?, qt_numeros = ?, 
                            seg = ?, ter = ?, qua = ?, qui = ?, sex = ?, sab = ?, dom = ?,
                            horario_sorteio = ? 
                        WHERE id_loterias = ?
                        """;
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, nome);
                    stmt.setString(2, contexto);
                    stmt.setInt(3, qtNumeros);
                    stmt.setBoolean(4, seg);
                    stmt.setBoolean(5, ter);
                    stmt.setBoolean(6, qua);
                    stmt.setBoolean(7, qui);
                    stmt.setBoolean(8, sex);
                    stmt.setBoolean(9, sab);
                    stmt.setBoolean(10, dom);
                    stmt.setString(11, horarioSorteio.toString());
                    stmt.setInt(12, loteriaSelecionada.getId());
                } else {
                    // Insert
                    sql = """
                        INSERT INTO tb_loterias 
                        (des_nome, des_contexto, qt_numeros, dt_inclusao, 
                         seg, ter, qua, qui, sex, sab, dom, horario_sorteio)
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, nome);
                    stmt.setString(2, contexto);
                    stmt.setInt(3, qtNumeros);
                    stmt.setBoolean(4, seg);
                    stmt.setBoolean(5, ter);
                    stmt.setBoolean(6, qua);
                    stmt.setBoolean(7, qui);
                    stmt.setBoolean(8, sex);
                    stmt.setBoolean(9, sab);
                    stmt.setBoolean(10, dom);
                    stmt.setString(11, horarioSorteio.toString());
                }

                stmt.executeUpdate();
                mostrarSucesso("Loteria salva com sucesso!");
                carregarLoterias();
                limparCampos();
                
            } catch (SQLException e) {
                mostrarErro("Erro de banco de dados", "Erro ao salvar loteria: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            mostrarErro("Erro", "Ocorreu um erro ao salvar a loteria: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    @FXML
    private void novoLoteria() {
        limparCampos();
    }

    @FXML
    private void excluirLoteria() {
        Loteria loteria = tabelaLoterias.getSelectionModel().getSelectedItem();
        if (loteria != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText(null);
            alert.setContentText("Tem certeza que deseja excluir a loteria " + loteria.getNome() + "?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = Database.getConnection();
                         PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_loterias WHERE id_loterias = ?")) {
                        stmt.setInt(1, loteria.getId());
                        stmt.executeUpdate();
                        carregarLoterias();
                        limparCampos();
                        mostrarSucesso("Loteria excluída com sucesso!");
                    } catch (SQLException e) {
                        mostrarErro("Erro ao excluir", "Não foi possível excluir a loteria: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void limparCampos() {
        txtNome.clear();
        txtContexto.clear();
        txtQtNumeros.clear();
        cbSeg.setSelected(false);
        cbTer.setSelected(false);
        cbQua.setSelected(false);
        cbQui.setSelected(false);
        cbSex.setSelected(false);
        cbSab.setSelected(false);
        cbDom.setSelected(false);
        txtHorarioSorteio.clear();
        loteriaSelecionada = null;
    }
}