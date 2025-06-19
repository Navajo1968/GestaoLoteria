package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Jogo;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Access Object para a tabela de jogos.
 * Gerencia inserção, consulta, atualização e estatísticas dos jogos cadastrados no sistema.
 */
public class JogoDAO {

    /**
     * Insere um novo jogo na base.
     */
    public void inserirJogo(Jogo jogo) throws Exception {
        String sql = "INSERT INTO jogo (loteria_id, concurso_id, numero_concurso_previsto, dezenas, data_cadastro, acertos, observacao) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
            ps.setString(4, jogo.getDezenas() != null ? jogo.getDezenas() : jogo.getNumeros()); // compatibilidade
            if (jogo.getAcertos() != null) {
                ps.setInt(5, jogo.getAcertos());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setString(6, jogo.getObservacao());
            ps.executeUpdate();
        }
    }

    /**
     * Salva vários jogos em lote (batch).
     */
    public void salvarJogos(List<Jogo> jogos) throws Exception {
        String sql = "INSERT INTO jogo (loteria_id, concurso_id, numero_concurso_previsto, dezenas, data_cadastro, acertos, observacao) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
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
                ps.setString(4, jogo.getDezenas() != null ? jogo.getDezenas() : jogo.getNumeros());
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

    /**
     * Lista todos os jogos cadastrados.
     */
    public List<Jogo> listarJogos() throws Exception {
        String sql = "SELECT id, loteria_id, concurso_id, numero_concurso_previsto, dezenas, data_cadastro, acertos, observacao FROM jogo";
        List<Jogo> jogos = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jogos.add(mapResultSetToJogo(rs));
            }
        }
        return jogos;
    }

    /**
     * Busca jogos por loteria.
     */
    public List<Jogo> buscarJogosPorLoteria(int loteriaId) throws Exception {
        String sql = "SELECT * FROM jogo WHERE loteria_id = ?";
        List<Jogo> jogos = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jogos.add(mapResultSetToJogo(rs));
            }
        }
        return jogos;
    }

    /**
     * Busca jogos por concurso.
     */
    public List<Jogo> buscarJogosPorConcurso(int concursoId) throws Exception {
        String sql = "SELECT * FROM jogo WHERE concurso_id = ?";
        List<Jogo> jogos = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concursoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jogos.add(mapResultSetToJogo(rs));
            }
        }
        return jogos;
    }

    /**
     * Busca jogos por faixa de acertos.
     */
    public List<Jogo> buscarJogosPorAcertos(int minAcertos, int maxAcertos) throws Exception {
        String sql = "SELECT * FROM jogo WHERE acertos BETWEEN ? AND ?";
        List<Jogo> jogos = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, minAcertos);
            ps.setInt(2, maxAcertos);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jogos.add(mapResultSetToJogo(rs));
            }
        }
        return jogos;
    }

    /**
     * Busca jogos não conferidos de determinado concurso (acertos NULL ou zero).
     */
    public List<Jogo> buscarJogosPorConcursoNaoConferidos(int concursoId) throws Exception {
        List<Jogo> lista = new ArrayList<>();
        String sql = "SELECT * FROM jogo WHERE concurso_id = ? AND (acertos IS NULL OR acertos = 0)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concursoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToJogo(rs));
            }
        }
        return lista;
    }

    /**
     * Atualiza a quantidade de acertos de um jogo.
     */
    public void atualizarAcertos(Jogo jogo) throws Exception {
        String sql = "UPDATE jogo SET acertos = ? WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (jogo.getAcertos() != null) {
                ps.setInt(1, jogo.getAcertos());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setInt(2, jogo.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Busca jogos conferidos de uma lista de concursos.
     */
    public List<Jogo> buscarJogosConferidosPorConcursos(List<Integer> concursoIds) throws Exception {
        List<Jogo> lista = new ArrayList<>();
        if (concursoIds == null || concursoIds.isEmpty()) return lista;
        String placeHolders = concursoIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM jogo WHERE concurso_id IN (" + placeHolders + ") AND acertos IS NOT NULL";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < concursoIds.size(); i++) {
                ps.setInt(i + 1, concursoIds.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToJogo(rs));
            }
        }
        return lista;
    }

    /**
     * Remove um jogo pelo ID.
     */
    public void removerJogo(int jogoId) throws Exception {
        String sql = "DELETE FROM jogo WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jogoId);
            ps.executeUpdate();
        }
    }

    /**
     * Atualiza dados de um jogo (exceto acertos).
     */
    public void atualizarJogo(Jogo jogo) throws Exception {
        String sql = "UPDATE jogo SET loteria_id = ?, concurso_id = ?, numero_concurso_previsto = ?, dezenas = ?, observacao = ? WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
            ps.setString(4, jogo.getDezenas() != null ? jogo.getDezenas() : jogo.getNumeros());
            ps.setString(5, jogo.getObservacao());
            ps.setInt(6, jogo.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Conta quantos jogos existem para uma loteria.
     */
    public int contarJogosPorLoteria(int loteriaId) throws Exception {
        String sql = "SELECT COUNT(*) FROM jogo WHERE loteria_id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Estatística: média de acertos por loteria.
     */
    public double mediaAcertosPorLoteria(int loteriaId) throws Exception {
        String sql = "SELECT AVG(acertos) FROM jogo WHERE loteria_id = ? AND acertos IS NOT NULL";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    /**
     * Busca jogos cadastrados em um intervalo de datas.
     */
    public List<Jogo> buscarJogosPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws Exception {
        String sql = "SELECT * FROM jogo WHERE data_cadastro BETWEEN ? AND ?";
        List<Jogo> jogos = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fim));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jogos.add(mapResultSetToJogo(rs));
            }
        }
        return jogos;
    }

    /**
     * Mapeia um ResultSet para objeto Jogo, compatível com diferentes nomes de campos.
     */
    private Jogo mapResultSetToJogo(ResultSet rs) throws SQLException {
        Jogo jogo = new Jogo();
        jogo.setId(rs.getInt("id"));
        jogo.setLoteriaId(rs.getInt("loteria_id"));
        int concursoId = rs.getInt("concurso_id");
        if (!rs.wasNull()) jogo.setConcursoId(concursoId);
        int numeroConcursoPrevisto = rs.getInt("numero_concurso_previsto");
        if (!rs.wasNull()) jogo.setNumeroConcursoPrevisto(numeroConcursoPrevisto);

        // Suporte tanto para "dezenas" quanto "numeros"
        try {
            jogo.setDezenas(rs.getString("dezenas"));
        } catch (SQLException e) {
            try {
                jogo.setDezenas(rs.getString("numeros"));
            } catch (SQLException ignore) {}
        }

        // Suporte tanto para data_cadastro quanto data_hora
        try {
            Timestamp ts = rs.getTimestamp("data_cadastro");
            jogo.setDataCadastro(ts != null ? ts.toLocalDateTime() : null);
        } catch (SQLException e) {
            try {
                Timestamp ts = rs.getTimestamp("data_hora");
                jogo.setDataCadastro(ts != null ? ts.toLocalDateTime() : null);
            } catch (SQLException ignore) {}
        }

        Integer acertos = rs.getInt("acertos");
        if (!rs.wasNull()) jogo.setAcertos(acertos);

        jogo.setObservacao(rs.getString("observacao"));
        return jogo;
    }

    // TODO: Métodos para integração futura com IA/Machine Learning podem ser incluídos aqui.
    // Exemplos: sugerir jogos, fazer clustering das apostas, etc.
}