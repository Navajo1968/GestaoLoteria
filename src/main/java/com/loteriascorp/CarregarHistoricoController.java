package com.loteriascorp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CarregarHistoricoController {

    @FXML
    private ComboBox<String> comboBoxLoteria;

    @FXML
    private Label label;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/gestaoloterias";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "@NaVaJo68#PostGre#";

    private Map<String, Integer> loteriasMap;

    @FXML
    private void initialize() {
        loteriasMap = new HashMap<>();
        carregarLoterias();
    }

    private void carregarLoterias() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_loterias, des_nome FROM tb_loterias")) {

            ObservableList<String> loterias = FXCollections.observableArrayList();
            while (rs.next()) {
                int id = rs.getInt("id_loterias");
                String nome = rs.getString("des_nome");
                loterias.add(nome);
                loteriasMap.put(nome, id);
            }
            comboBoxLoteria.setItems(loterias);

        } catch (Exception e) {
            e.printStackTrace();
            label.setText("Erro ao carregar loterias: " + e.getMessage());
        }
    }

    @FXML
    private void selecionarArquivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Excel", "*.xlsx"));
        File arquivo = fileChooser.showOpenDialog(new Stage());

        if (arquivo != null) {
            carregarHistorico(arquivo.getAbsolutePath());
        }
    }

    public void carregarHistorico(String caminhoArquivo) {
        String loteriaSelecionada = comboBoxLoteria.getValue();
        if (loteriaSelecionada == null || loteriaSelecionada.isEmpty()) {
            label.setText("Por favor, selecione uma loteria.");
            return;
        }

        int idLoteria = loteriasMap.get(loteriaSelecionada);

        try (FileInputStream fis = new FileInputStream(caminhoArquivo);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<String[]> dados = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                String[] linhaDados = new String[17];
                linhaDados[0] = row.getCell(1).getStringCellValue();  // Data do concurso
                for (int j = 1; j <= 15; j++) {
                    linhaDados[j] = String.valueOf((int) row.getCell(j + 1).getNumericCellValue());  // Números sorteados
                }
                dados.add(linhaDados);
            }

            // Deletar registros existentes na tabela
            String deleteSQL = "DELETE FROM tb_historico_jogos WHERE id_loterias = ?";
            try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteSQL)) {
                pstmtDelete.setInt(1, idLoteria);
                pstmtDelete.executeUpdate();
            }

            // Inserir novos registros
            String insertSQL = "INSERT INTO tb_historico_jogos (id_loterias, dt_jogo, num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, num11, num12, num13, num14, num15) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (String[] linha : dados) {
                    LocalDate data = LocalDate.parse(linha[0], formatter);
                    pstmt.setInt(1, idLoteria);
                    pstmt.setDate(2, java.sql.Date.valueOf(data));
                    for (int j = 1; j <= 15; j++) {
                        pstmt.setInt(j + 2, Integer.parseInt(linha[j]));
                    }
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            label.setText("Histórico carregado com sucesso!");

        } catch (IOException e) {
            e.printStackTrace();
            label.setText("Erro ao ler o arquivo: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            label.setText("Erro ao carregar histórico: " + e.getMessage());
        }
    }
}