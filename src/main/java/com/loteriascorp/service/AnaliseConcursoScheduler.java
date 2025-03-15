package com.loteriascorp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.loteriascorp.Database;
import com.loteriascorp.analise.AnalisadorConcurso;

public class AnaliseConcursoScheduler {
    private static final Logger logger = LogManager.getLogger(AnaliseConcursoScheduler.class);
    private final ScheduledExecutorService scheduler;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AnaliseConcursoScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void iniciarAnaliseAutomatica() {
        scheduler.scheduleWithFixedDelay(
            () -> {
                try {
                    logger.info("Iniciando análise automática em {}", LocalDateTime.now().format(formatter));
                    analisarUltimosConcursos();
                } catch (Exception e) {
                    logger.error("Erro na análise automática: ", e);
                }
            },
            0, 24, TimeUnit.HOURS
        );
    }

    private void analisarUltimosConcursos() {
        // Query para buscar concursos que ainda não foram analisados
        String sql = """
            SELECT DISTINCT 
                hj.id_loterias, 
                hj.num_concurso,
                l.des_nome
            FROM tb_historico_jogos hj
            INNER JOIN tb_loterias l ON l.id_loterias = hj.id_loterias
            LEFT JOIN tb_analise_concursos ac 
                ON hj.id_loterias = ac.id_loterias 
                AND hj.num_concurso = ac.nr_concurso
            WHERE ac.id_loterias IS NULL
                AND hj.num_concurso > 0
                AND EXISTS (
                    SELECT 1 
                    FROM tb_historico_jogos 
                    WHERE id_loterias = hj.id_loterias 
                    AND num_concurso = hj.num_concurso
                )
            ORDER BY hj.id_loterias, hj.num_concurso DESC
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            logger.info("Buscando concursos pendentes de análise...");
            ResultSet rs = stmt.executeQuery();
            boolean encontrouConcursos = false;
            int concursosAnalisados = 0;
            
            while (rs.next()) {
                encontrouConcursos = true;
                int idLoterias = rs.getInt("id_loterias");
                int numConcurso = rs.getInt("num_concurso");
                String nomeLoteria = rs.getString("des_nome");
                
                try {
                    logger.info("Analisando {} concurso {}", nomeLoteria, numConcurso);
                    AnalisadorConcurso analisador = new AnalisadorConcurso(idLoterias, numConcurso);
                    analisador.analisarConcurso();
                    
                    concursosAnalisados++;
                    logger.info("Análise concluída com sucesso para {} concurso {}", 
                              nomeLoteria, numConcurso);
                    
                } catch (Exception e) {
                    logger.error("Erro ao analisar {} concurso {}: {}", 
                               nomeLoteria, numConcurso, e.getMessage());
                }
            }
            
            if (!encontrouConcursos) {
                logger.info("Nenhum concurso novo para analisar");
            } else {
                logger.info("Análise automática concluída. {} concursos analisados.", 
                          concursosAnalisados);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar concursos para análise: ", e);
            throw new RuntimeException("Falha ao executar análise automática", e);
        }
    }

    /**
     * Verifica se existe um concurso específico para análise
     * @param idLoteria ID da loteria
     * @param numConcurso Número do concurso
     * @return true se o concurso existe e ainda não foi analisado
     */
    public boolean verificarConcursoPendente(int idLoteria, int numConcurso) {
        String sql = """
            SELECT 1
            FROM tb_historico_jogos hj
            LEFT JOIN tb_analise_concursos ac 
                ON hj.id_loterias = ac.id_loterias 
                AND hj.num_concurso = ac.nr_concurso
            WHERE hj.id_loterias = ?
                AND hj.num_concurso = ?
                AND ac.id_loterias IS NULL
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLoteria);
            stmt.setInt(2, numConcurso);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar concurso pendente: ", e);
            return false;
        }
    }

    /**
     * Realiza a análise de um concurso específico
     * @param idLoteria ID da loteria
     * @param numConcurso Número do concurso
     * @return true se a análise foi realizada com sucesso
     */
    public boolean analisarConcursoEspecifico(int idLoteria, int numConcurso) {
        if (!verificarConcursoPendente(idLoteria, numConcurso)) {
            logger.info("Concurso {} da loteria {} já foi analisado ou não existe", 
                       numConcurso, idLoteria);
            return false;
        }

        try {
            AnalisadorConcurso analisador = new AnalisadorConcurso(idLoteria, numConcurso);
            analisador.analisarConcurso();
            logger.info("Análise do concurso {} da loteria {} realizada com sucesso", 
                       numConcurso, idLoteria);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao analisar concurso {} da loteria {}: {}", 
                        numConcurso, idLoteria, e.getMessage());
            return false;
        }
    }

    public void shutdown() {
        logger.info("Iniciando desligamento do scheduler de análise...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                logger.warn("Forçando encerramento do scheduler após timeout");
            } else {
                logger.info("Scheduler encerrado com sucesso");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.error("Interrupção durante o desligamento do scheduler", e);
        }
    }
}