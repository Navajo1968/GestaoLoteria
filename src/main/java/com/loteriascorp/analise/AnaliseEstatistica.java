package com.loteriascorp.analise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import com.loteriascorp.Database;

public class AnaliseEstatistica {
    
    public record ResultadoAnalise(
        int qtPares,
        int qtImpares,
        double media,
        double desvioPadrao,
        Map<String, Integer> distribuicaoDezenas,
        double scoreQualidade
    ) {}

    public ResultadoAnalise analisarJogo(List<Integer> numeros) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        int pares = 0;
        Map<String, Integer> distribuicao = new HashMap<>();
        
        // Contagem de pares e ímpares
        for (int num : numeros) {
            if (num % 2 == 0) pares++;
            stats.addValue(num);
            
            // Distribuição por dezenas
            if (num <= 10) distribuicao.merge("1-10", 1, Integer::sum);
            else if (num <= 20) distribuicao.merge("11-20", 1, Integer::sum);
            else distribuicao.merge("21-25", 1, Integer::sum);
        }

        return new ResultadoAnalise(
            pares,
            15 - pares,
            stats.getMean(),
            stats.getStandardDeviation(),
            distribuicao,
            calcularScoreQualidade(pares, distribuicao)
        );
    }

    private double calcularScoreQualidade(int pares, Map<String, Integer> distribuicao) {
        double score = 0.0;
        
        // Avaliação da paridade (ideal: 7-8 ou 8-7)
        score += (pares >= 7 && pares <= 8) ? 1.0 : 0.0;
        
        // Avaliação da distribuição por dezenas
        int d1 = distribuicao.getOrDefault("1-10", 0);
        int d2 = distribuicao.getOrDefault("11-20", 0);
        int d3 = distribuicao.getOrDefault("21-25", 0);
        
        // Distribuição ideal aproximada: 6-6-3
        score += (d1 >= 5 && d1 <= 7) ? 1.0 : 0.0;
        score += (d2 >= 5 && d2 <= 7) ? 1.0 : 0.0;
        score += (d3 >= 2 && d3 <= 4) ? 1.0 : 0.0;
        
        return score / 4.0; // Normaliza para 0-1
    }

    public void salvarAnalise(int idLoterias, ResultadoAnalise analise) {
        String sql = """
            INSERT INTO tb_analise_estatistica 
            (id_loterias, dt_analise, qt_pares, qt_impares, media_dezenas, 
             desvio_padrao, qt_dezena_1_10, qt_dezena_11_20, qt_dezena_21_25, score_qualidade)
            VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoterias);
            stmt.setInt(2, analise.qtPares());
            stmt.setInt(3, analise.qtImpares());
            stmt.setDouble(4, analise.media());
            stmt.setDouble(5, analise.desvioPadrao());
            stmt.setInt(6, analise.distribuicaoDezenas().getOrDefault("1-10", 0));
            stmt.setInt(7, analise.distribuicaoDezenas().getOrDefault("11-20", 0));
            stmt.setInt(8, analise.distribuicaoDezenas().getOrDefault("21-25", 0));
            stmt.setDouble(9, analise.scoreQualidade());
            
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}