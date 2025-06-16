package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Jogo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JogoDAO {

    public void inserirJogo(Jogo jogo) throws Exception {
        String sql = "INSERT INTO jogo (loteria_id, concurso_id, numero_concurso_previsto, numeros, data_hora, acertos, observacao) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jogo.getLoteriaId());
            // concurso_id pode ser null
            if (jogo.getConcursoId() != null) {
                ps.setInt(2, jogo.getConcursoId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            // numero_concurso_previsto
            if (jogo.getNumeroConcursoPrevisto() != null) {
                ps.setInt(3, jogo.getNumeroConcursoPrevisto());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, jogo.getNumeros());
            // acertos pode ser null
            if (jogo.getAcertos() != null) {
                ps.setInt(5, jogo.getAcertos());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setString(6, jogo.getObservacao());
            ps.executeUpdate();
        }
    }

    public List<Jogo> listarJogos() throws Exception {
        String sql = "SELECT id, loteria_id, concurso_id, numero_concurso_previsto, numeros, data_hora, acertos, observacao FROM jogo";
        List<Jogo> jogos = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Jogo jogo = new Jogo();
                jogo.setId(rs.getInt("id"));
                jogo.setLoteriaId(rs.getInt("loteria_id"));
                int concursoId = rs.getInt("concurso_id");
                if (!rs.wasNull()) jogo.setConcursoId(concursoId);
                int numeroConcursoPrevisto = rs.getInt("numero_concurso_previsto");
                if (!rs.wasNull()) jogo.setNumeroConcursoPrevisto(numeroConcursoPrevisto);
                jogo.setNumeros(rs.getString("numeros"));
                jogo.setDataHora(rs.getTimestamp("data_hora") != null ? rs.getTimestamp("data_hora").toLocalDateTime() : null);
                Integer acertos = rs.getInt("acertos");
                if (!rs.wasNull()) jogo.setAcertos(acertos);
                jogo.setObservacao(rs.getString("observacao"));
                jogos.add(jogo);
            }
        }
        return jogos;
    }

    public void salvarJogos(List<Jogo> jogos) throws Exception {
        String sql = "INSERT INTO jogo (loteria_id, concurso_id, numero_concurso_previsto, numeros, data_hora, acertos, observacao) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Jogo jogo : jogos) {
                ps.setInt(1, jogo.getLoteriaId());
                if (jogo.getConcursoId() != null) {
                    ps.setInt(2, jogo.getConcursoId());
                } else {
                    ps.setNull(2, java.sql.Types.INTEGER);
                }
                if (jogo.getNumeroConcursoPrevisto() != null) {
                    ps.setInt(3, jogo.getNumeroConcursoPrevisto());
                } else {
                    ps.setNull(3, java.sql.Types.INTEGER);
                }
                ps.setString(4, jogo.getNumeros());
                if (jogo.getAcertos() != null) {
                    ps.setInt(5, jogo.getAcertos());
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
                ps.setString(6, jogo.getObservacao());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}