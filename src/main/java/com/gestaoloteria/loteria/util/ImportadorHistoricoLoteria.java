package com.gestaoloteria.loteria.util;

import com.gestaoloteria.loteria.dao.ConcursoDAO;
import com.gestaoloteria.loteria.dao.ConcursoNumeroSorteadoDAO;
import com.gestaoloteria.loteria.model.Concurso;
import com.gestaoloteria.loteria.model.ConcursoNumeroSorteado;
import com.gestaoloteria.loteria.model.Loteria;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ImportadorHistoricoLoteria {

    public static void importar(Loteria loteria, File arquivo) throws Exception {
        try (FileInputStream fis = new FileInputStream(arquivo);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection conn = com.gestaoloteria.loteria.dao.ConexaoBanco.getConnection()) {

            conn.setAutoCommit(false); // transação única

            Set<Integer> concursosExistentes = new HashSet<>();
            ConcursoDAO concursoDAO = new ConcursoDAO();
            for (Concurso c : concursoDAO.listarConcursosPorLoteria(loteria.getId(), conn)) {
                concursosExistentes.add(c.getNumero());
            }

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Pular cabeçalho
            if (rowIterator.hasNext()) rowIterator.next();

            ConcursoNumeroSorteadoDAO numeroDAO = new ConcursoNumeroSorteadoDAO();

            int novosConcursos = 0;
            int novosNumeros = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cellConcurso = row.getCell(0);
                Cell cellData = row.getCell(1);
                List<Integer> dezenas = new ArrayList<>();

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
                    continue;
                }

                if (concursosExistentes.contains(numeroConcurso)) {
                    continue;
                }

                Concurso concurso = new Concurso();
                concurso.setLoteriaId(loteria.getId());
                concurso.setNumero(numeroConcurso);
                concurso.setData(dataConcurso);

                // Colunas adicionais conforme ordem do arquivo/sheet
                concurso.setArrecadacaoTotal(getCellAsBigDecimal(row.getCell(17)));
                concurso.setAcumulado(getCellAsBoolean(row.getCell(18)));
                concurso.setValorAcumulado(getCellAsBigDecimal(row.getCell(19)));
                concurso.setEstimativaPremio(getCellAsBigDecimal(row.getCell(20)));
                concurso.setAcumuladoEspecial(getCellAsBigDecimal(row.getCell(21)));
                concurso.setObservacao(getCellAsString(row.getCell(22)));
                concurso.setTimeCoracao(getCellAsString(row.getCell(23)));

                int concursoId = concursoDAO.inserirConcurso(concurso, conn);

                List<ConcursoNumeroSorteado> batchNumeros = new ArrayList<>();
                for (int idx = 0; idx < dezenas.size(); idx++) {
                    Integer dezena = dezenas.get(idx);
                    ConcursoNumeroSorteado numeroSorteado = new ConcursoNumeroSorteado();
                    numeroSorteado.setConcursoId(concursoId);
                    numeroSorteado.setNumero(dezena);
                    numeroSorteado.setOrdem(idx + 1);
                    batchNumeros.add(numeroSorteado);
                }
                numeroDAO.inserirNumerosSorteadosBatch(batchNumeros, conn);

                concursosExistentes.add(numeroConcurso); // previne duplicidade se arquivo estiver bagunçado
                novosConcursos++;
                novosNumeros += batchNumeros.size();
            }

            conn.commit();
            System.out.println("Importação finalizada: " + novosConcursos + " concursos, " + novosNumeros + " dezenas inseridas.");
        }
    }

    public static Integer getCellAsInteger(Cell cell) {
        Double d = getCellAsDouble(cell);
        return (d == null) ? null : d.intValue();
    }

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
                    try {
                        String formulaResult = cell.getStringCellValue().replace(",", ".").trim();
                        return Double.parseDouble(formulaResult);
                    } catch (Exception ex) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }

    public static LocalDate getCellAsLocalDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            String dataStr = cell.getStringCellValue().trim();
            try {
                return LocalDate.parse(dataStr);
            } catch (Exception e) {
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

    public static BigDecimal getCellAsBigDecimal(Cell cell) {
        Double d = getCellAsDouble(cell);
        return (d == null) ? null : BigDecimal.valueOf(d);
    }

    public static Boolean getCellAsBoolean(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.BOOLEAN) return cell.getBooleanCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue() != 0;
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim().toLowerCase();
            if (val.equals("true") || val.equals("sim") || val.equals("1")) return true;
            if (val.equals("false") || val.equals("não") || val.equals("nao") || val.equals("0")) return false;
        }
        return null;
    }

    public static String getCellAsString(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf(cell.getNumericCellValue());
        if (cell.getCellType() == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        return null;
    }
}