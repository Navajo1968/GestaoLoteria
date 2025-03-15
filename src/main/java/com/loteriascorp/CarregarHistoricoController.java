package com.loteriascorp;
// Carrega os resultados das loterias
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
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
                if (row == null) continue; // Ignora linhas vazias

                String[] linhaDados = new String[18];

                // Número do concurso

                Cell cellConcurso = row.getCell(0);
                int numConcurso;
                if (cellConcurso.getCellType() == CellType.NUMERIC) {
                    numConcurso = (int) cellConcurso.getNumericCellValue();
                } else {
                    numConcurso = Integer.parseInt(cellConcurso.getStringCellValue().trim());
                }
                linhaDados[0] = String.valueOf(numConcurso);


                // Data do jogo
                Cell cellData = row.getCell(1);
                if (cellData.getCellType() == CellType.NUMERIC) {
                    // Caso a célula esteja em formato numérico (excel armazena datas assim)
                    LocalDate data = cellData.getLocalDateTimeCellValue().toLocalDate();
                    linhaDados[1] = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    linhaDados[1] = cellData.getStringCellValue().trim();
                }

                // Números sorteados
                for (int j = 2; j <= 16; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        linhaDados[j] = String.valueOf((int) cell.getNumericCellValue());
                    } else {
                        linhaDados[j] = "0";  // Evita erro se a célula estiver vazia
                    }
                }

                dados.add(linhaDados);
            }

            // Deletar registros existentes apenas da loteria selecionada
            String deleteSQL = "DELETE FROM tb_historico_jogos WHERE id_loterias = ?";
            try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteSQL)) {
                pstmtDelete.setInt(1, idLoteria);
                pstmtDelete.executeUpdate();
            }

            // Inserir novos registros
            String insertSQL = "INSERT INTO tb_historico_jogos (id_loterias, dt_jogo, num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, num11, num12, num13, num14, num15, num_concurso) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                for (String[] linha : dados) {
                    LocalDate data = LocalDate.parse(linha[1], formatter);

                    pstmt.setInt(1, idLoteria);
                    pstmt.setDate(2, java.sql.Date.valueOf(data));
                    for (int j = 2; j <= 16; j++) {
                        pstmt.setInt(j + 1, Integer.parseInt(linha[j]));
                    }
                    pstmt.setInt(18, Integer.parseInt(linha[0])); // Número do concurso

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
