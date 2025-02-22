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
        String sql = """
            WITH ultimos_sorteios AS (
                SELECT 
                    num, 
                    nr_concurso,
                    ROW_NUMBER() OVER (PARTITION BY num ORDER BY num_concurso DESC) as recencia
                FROM (
                    SELECT num_concurso, num1 as num FROM tb_historico_jogos WHERE id_loterias = ?
                    UNION ALL SELECT num_concurso, num2 
                    UNION ALL SELECT num_concurso, num3
                    UNION ALL SELECT num_concurso, num4
                    UNION ALL SELECT num_concurso, num5
                    UNION ALL SELECT num_concurso, num6
                    UNION ALL SELECT num_concurso, num7
                    UNION ALL SELECT num_concurso, num8
                    UNION ALL SELECT num_concurso, num9
                    UNION ALL SELECT num_concurso, num10
                    UNION ALL SELECT num_concurso, num11
                    UNION ALL SELECT num_concurso, num12
                    UNION ALL SELECT num_concurso, num13
                    UNION ALL SELECT num_concurso, num14
                    UNION ALL SELECT num_concurso, num15
                ) nums
            ),
            estatisticas AS (
                SELECT 
                    num,
                    COUNT(*) as frequencia,
                    MIN(recencia) as recencia,
                    AVG(nr_concurso) as media_posicao
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
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, idLoteria);
            
            int inseridos = stmt.executeUpdate();
            logger.info("Análise estatística atualizada: {} números analisados", inseridos);
            
        } catch (SQLException e) {
            logger.error("Erro ao atualizar análise estatística: ", e);
        }
    }
}