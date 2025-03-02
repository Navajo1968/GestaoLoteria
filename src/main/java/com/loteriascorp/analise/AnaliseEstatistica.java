package com.loteriascorp.analise;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.loteriascorp.Database;

public class AnaliseEstatistica {
    private final int idLoteria;
    private final Logger logger = LogManager.getLogger(AnaliseEstatistica.class);
    
    public AnaliseEstatistica(int idLoteria) {
        this.idLoteria = idLoteria;
    }
    
    public void analisarNumeros() {
        String baseQuery = """
            UNION ALL
            SELECT num_concurso, num%d,
                   num1, num2, num3, num4, num5, num6, num7, num8, 
                   num9, num10, num11, num12, num13, num14, num15
            FROM tb_historico_jogos 
            WHERE id_loterias = ?
        """;

        StringBuilder sqlBuilder = new StringBuilder("""
            WITH numeros_expandidos AS (
                SELECT 
                    num_concurso,
                    num,
                    CASE 
                        WHEN num = num1 THEN 1
                        WHEN num = num2 THEN 2
                        WHEN num = num3 THEN 3
                        WHEN num = num4 THEN 4
                        WHEN num = num5 THEN 5
                        WHEN num = num6 THEN 6
                        WHEN num = num7 THEN 7
                        WHEN num = num8 THEN 8
                        WHEN num = num9 THEN 9
                        WHEN num = num10 THEN 10
                        WHEN num = num11 THEN 11
                        WHEN num = num12 THEN 12
                        WHEN num = num13 THEN 13
                        WHEN num = num14 THEN 14
                        WHEN num = num15 THEN 15
                    END as posicao_no_jogo
                FROM (
                    SELECT num_concurso, num1 as num, 
                           num1, num2, num3, num4, num5, num6, num7, num8, 
                           num9, num10, num11, num12, num13, num14, num15 
                    FROM tb_historico_jogos 
                    WHERE id_loterias = ?
        """);

        for (int i = 2; i <= 15; i++) {
            sqlBuilder.append(String.format(baseQuery, i));
        }

        sqlBuilder.append("""
                ) nums
            ),
            ultimos_sorteios AS (
                SELECT 
                    num,
                    num_concurso,
                    posicao_no_jogo,
                    ROW_NUMBER() OVER (PARTITION BY num ORDER BY num_concurso DESC) as recencia
                FROM numeros_expandidos
            ),
            estatisticas AS (
                SELECT 
                    num,
                    COUNT(*) as frequencia,
                    MIN(recencia) as recencia,
                    AVG(posicao_no_jogo) as media_posicao
                FROM ultimos_sorteios
                GROUP BY num
            )
            INSERT INTO tb_analise_estatistica 
            (id_loterias, num_analisado, frequencia, recencia, media_posicao)
            SELECT 
                ?, -- id_loterias
                num,
                frequencia,
                recencia,
                media_posicao
            FROM estatisticas
        """);

        String sql = sqlBuilder.toString();
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 1; i <= 15; i++) {
                stmt.setInt(i, idLoteria);
            }
            stmt.setInt(16, idLoteria); // Para o SELECT final
            
            int inseridos = stmt.executeUpdate();
            logger.info("Análise estatística atualizada: {} números analisados", inseridos);
            
        } catch (SQLException e) {
            logger.error("Erro ao atualizar análise estatística: ", e);
        }
    }
}