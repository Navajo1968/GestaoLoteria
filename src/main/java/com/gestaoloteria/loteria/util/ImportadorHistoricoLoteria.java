package com.gestaoloteria.loteria.util;

import com.gestaoloteria.loteria.dao.ConcursoDAO;
import com.gestaoloteria.loteria.dao.ConcursoNumeroSorteadoDAO;
import com.gestaoloteria.loteria.model.Concurso;
import com.gestaoloteria.loteria.model.ConcursoNumeroSorteado;
import com.gestaoloteria.loteria.model.Loteria;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImportadorHistoricoLoteria {

    public static void importar(Loteria loteria, File arquivo) throws Exception {
        try (FileInputStream fis = new FileInputStream(arquivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Pular cabeçalho (ajuste se necessário)
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            ConcursoDAO concursoDAO = new ConcursoDAO();
            ConcursoNumeroSorteadoDAO numeroDAO = new ConcursoNumeroSorteadoDAO();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Ajuste os índices das colunas conforme o seu layout do Excel
                Cell cellConcurso = row.getCell(0); // número do concurso
                Cell cellData = row.getCell(1);     // data do concurso
                List<Integer> dezenas = new ArrayList<>();

                // Para Lotofácil, geralmente há 15 dezenas, começando na coluna 2
                for (int i = 2; i < 17; i++) {
                    Cell cellDezena = row.getCell(i);
                    Integer dezena = getCellAsInteger(cellDezena);
                    if (dezena != null) {
                        dezenas.add(dezena);
                    }
                }

                Integer numeroConcurso = getCellAsInteger(cellConcurso);
                LocalDate dataConcurso = getCellAsLocalDate(cellData);

                if (numeroConcurso == null || dataConcurso == null || dezenas.size() != 15) {
                    // Pula linhas inválidas
                    continue;
                }

                // Verifica se o concurso já existe para evitar duplicidade
                if (concursoDAO.existeConcurso(loteria.getId(), numeroConcurso)) {
                    continue;
                }

                Concurso concurso = new Concurso();
                concurso.setLoteriaId(loteria.getId());
                concurso.setNumero(numeroConcurso);
                concurso.setData(dataConcurso);

                int concursoId = concursoDAO.inserirConcurso(concurso);

                for (int idx = 0; idx < dezenas.size(); idx++) {
                    Integer dezena = dezenas.get(idx);
                    ConcursoNumeroSorteado numeroSorteado = new ConcursoNumeroSorteado();
                    numeroSorteado.setConcursoId(concursoId);
                    numeroSorteado.setNumero(dezena);
                    numeroSorteado.setOrdem(idx + 1); // ordem começa em 1
                    numeroDAO.inserirNumeroSorteado(numeroSorteado);
                }
            }
        }
    }

    // Utilitário seguro para extrair inteiro de uma célula
    public static Integer getCellAsInteger(Cell cell) {
        Double d = getCellAsDouble(cell);
        return (d == null) ? null : d.intValue();
    }

    // Utilitário seguro para extrair double de uma célula
    public static Double getCellAsDouble(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String valor = cell.getStringCellValue().replace(",", ".").trim();
                try {
                    return Double.parseDouble(valor);
                } catch (NumberFormatException e) {
                    return null;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (IllegalStateException e) {
                    // Caso fórmula retorne string
                    try {
                        String formulaResult = cell.getStringCellValue().replace(",", ".").trim();
                        return Double.parseDouble(formulaResult);
                    } catch (Exception ex) {
                        return null;
                    }
                }
            case BLANK:
            case BOOLEAN:
            case ERROR:
            default:
                return null;
        }
    }

    // Utilitário seguro para extrair LocalDate de uma célula (data)
    public static LocalDate getCellAsLocalDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            String dataStr = cell.getStringCellValue().trim();
            try {
                // Tenta formato padrão ISO (yyyy-MM-dd)
                return LocalDate.parse(dataStr);
            } catch (Exception e) {
                // Tenta outros formatos se necessário
                // Exemplo: dd/MM/yyyy
                try {
                    String[] parts = dataStr.split("[/\\-]");
                    if (parts.length == 3) {
                        int dia = Integer.parseInt(parts[0]);
                        int mes = Integer.parseInt(parts[1]);
                        int ano = Integer.parseInt(parts[2]);
                        return LocalDate.of(ano, mes, dia);
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}