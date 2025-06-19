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
    
    public List<Jogo> buscarJogosPorConcursoNaoConferidos(int concursoId) throws Exception {
        List<Jogo> lista = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection()) {
            String sql = "SELECT * FROM jogo WHERE concurso_id = ? AND (acertos IS NULL OR acertos = 0)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, concursoId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Jogo jogo = new Jogo();
                    jogo.setId(rs.getInt("id"));
                    jogo.setLoteriaId(rs.getInt("loteria_id"));
                    jogo.setConcursoId(rs.getInt("concurso_id"));
                    jogo.setNumeroConcursoPrevisto(rs.getInt("numero_concurso_previsto"));
                    jogo.setDezenas(rs.getString("dezenas"));
                    jogo.setDataCadastro(rs.getTimestamp("data_cadastro").toLocalDateTime());
                    jogo.setAcertos(rs.getObject("acertos") != null ? rs.getInt("acertos") : null);
                    // ... outros campos se houver ...
                    lista.add(jogo);
                }
            }
        }
        return lista;
    }    
    
    public void atualizarAcertos(Jogo jogo) throws Exception {
        try (Connection conn = ConexaoBanco.getConnection()) {
            String sql = "UPDATE jogo SET acertos = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (jogo.getAcertos() != null) {
                    ps.setInt(1, jogo.getAcertos());
                } else {
                    ps.setNull(1, java.sql.Types.INTEGER);
                }
                ps.setInt(2, jogo.getId());
                ps.executeUpdate();
            }
        }
    }
    
    public List<Jogo> buscarJogosConferidosPorConcursos(List<Integer> concursoIds) throws Exception {
        List<Jogo> lista = new ArrayList<>();
        if (concursoIds == null || concursoIds.isEmpty()) return lista;

        String placeHolders = concursoIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM jogo WHERE concurso_id IN (" + placeHolders + ") AND acertos IS NOT NULL";

        try (Connection conn = ConexaoBanco.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < concursoIds.size(); i++) {
                    ps.setInt(i + 1, concursoIds.get(i));
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Jogo jogo = new Jogo();
                    jogo.setId(rs.getInt("id"));
                    jogo.setLoteriaId(rs.getInt("loteria_id"));
                    jogo.setConcursoId(rs.getInt("concurso_id"));
                    jogo.setNumeroConcursoPrevisto(rs.getInt("numero_concurso_previsto"));
                    jogo.setDezenas(rs.getString("dezenas"));
                    jogo.setDataCadastro(rs.getTimestamp("data_cadastro").toLocalDateTime());
                    jogo.setAcertos(rs.getObject("acertos") != null ? rs.getInt("acertos") : null);
                    lista.add(jogo);
                }
            }
        }
        return lista;
    }
}