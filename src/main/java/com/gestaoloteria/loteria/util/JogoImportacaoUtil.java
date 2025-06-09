package com.gestaoloteria.loteria.util;

import com.gestaoloteria.loteria.dao.JogoDAO;
import com.gestaoloteria.loteria.model.Jogo;
import com.gestaoloteria.loteria.model.Loteria;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.*;

public class JogoImportacaoUtil {

    public static void importar(Loteria loteria, File arquivo) throws Exception {
        List<Jogo> jogos = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(arquivo);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            boolean headerLido = false;
            while (iterator.hasNext()) {
                Row row = iterator.next();
                if (!headerLido) { headerLido = true; continue; }
                int concurso = (int) row.getCell(0).getNumericCellValue();
                LocalDate data = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
                List<Integer> dezenas = new ArrayList<>();
                for (int i = 2; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        dezenas.add((int) cell.getNumericCellValue());
                    }
                }
                jogos.add(new Jogo(loteria.getId(), concurso, data, dezenas));
            }
        }
        if (!jogos.isEmpty()) {
            JogoDAO dao = new JogoDAO();
            dao.salvarJogos(jogos);
        }
    }
}