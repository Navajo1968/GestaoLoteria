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
import java.util.*;

public class ImportadorHistoricoLoteria {
    /**
     * Importa o arquivo XLSX do histórico de uma loteria e insere concursos e dezenas normalizados.
     */
    public static void importar(Loteria loteria, File arquivo) throws Exception {
        List<Concurso> concursos = new ArrayList<>();
        Map<Concurso, List<Integer>> dezenasPorConcurso = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(arquivo);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            boolean headerLido = false;
            while (iterator.hasNext()) {
                Row row = iterator.next();
                if (!headerLido) { headerLido = true; continue; }
                // Supondo as primeiras colunas: numero_concurso, data_concurso, dezenas...
                int numeroConcurso = (int) row.getCell(0).getNumericCellValue();
                LocalDate dataConcurso = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
                List<Integer> dezenas = new ArrayList<>();
                for (int i = 2; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        dezenas.add((int) cell.getNumericCellValue());
                    }
                }
                Concurso concurso = new Concurso(loteria.getId(), numeroConcurso, dataConcurso);
                concursos.add(concurso);
                dezenasPorConcurso.put(concurso, dezenas);
            }
        }

        ConcursoDAO concursoDAO = new ConcursoDAO();
        ConcursoNumeroSorteadoDAO numeroDAO = new ConcursoNumeroSorteadoDAO();

        for (Concurso concurso : concursos) {
            int concursoId = concursoDAO.inserirConcurso(concurso);
            List<Integer> dezenas = dezenasPorConcurso.get(concurso);
            List<ConcursoNumeroSorteado> numeros = new ArrayList<>();
            int ordem = 1;
            for (Integer dezena : dezenas) {
                numeros.add(new ConcursoNumeroSorteado(concursoId, dezena, ordem));
                ordem++;
            }
            numeroDAO.inserirNumerosSorteados(numeros);
        }
    }
}