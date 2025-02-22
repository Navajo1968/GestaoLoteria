package com.loteriascorp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.loteriascorp.Database;
import com.loteriascorp.analise.AnalisadorConcurso;

public class AnaliseConcursoScheduler {
    private static final Logger logger = LogManager.getLogger(AnaliseConcursoScheduler.class);
    private final ScheduledExecutorService scheduler;

    public AnaliseConcursoScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void iniciarAnaliseAutomatica() {
        scheduler.scheduleWithFixedDelay(
            () -> {
                try {
                    analisarUltimosConcursos();
                } catch (Exception e) {
                    logger.error("Erro na análise automática: ", e);
                }
            },
            0, 24, TimeUnit.HOURS
        );
    }

    private void analisarUltimosConcursos() {
        String sql = """
            SELECT DISTINCT hj.id_loterias, hj.num_concurso
            FROM tb_historico_jogos hj
            LEFT JOIN tb_analise_concursos ac 
                ON hj.id_loterias = ac.id_loterias 
                AND hj.num_concurso = ac.num_concurso
            WHERE ac.id_loterias IS NULL
            ORDER BY hj.id_loterias, hj.num_concurso
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int idLoterias = rs.getInt("id_loterias");
                int numConcurso = rs.getInt("num_concurso");
                
                try {
                    new AnalisadorConcurso(idLoterias, numConcurso).analisarConcurso();
                    logger.info("Análise realizada para loteria {} concurso {}", 
                              idLoterias, numConcurso);
                } catch (Exception e) {
                    logger.error("Erro ao analisar loteria {} concurso {}: {}", 
                               idLoterias, numConcurso, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar concursos para análise: ", e);
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}