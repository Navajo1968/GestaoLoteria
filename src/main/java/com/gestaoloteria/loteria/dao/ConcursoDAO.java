package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Concurso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConcursoDAO {

    public boolean existeConcurso(int loteriaId, int numeroConcurso) throws Exception {
        try (Connection conn = ConexaoBanco.getConnection()) {
            return existeConcurso(loteriaId, numeroConcurso, conn);
        }
    }

    public boolean existeConcurso(int loteriaId, int numeroConcurso, Connection conn) throws Exception {
        String sql = "SELECT 1 FROM concurso WHERE loteria_id = ? AND numero_concurso = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ps.setInt(2, numeroConcurso);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public int inserirConcurso(Concurso concurso) throws Exception {
        try (Connection conn = ConexaoBanco.getConnection()) {
            return inserirConcurso(concurso, conn);
        }
    }

    public int inserirConcurso(Concurso concurso, Connection conn) throws Exception {
        String sql = "INSERT INTO concurso " +
                "(loteria_id, numero_concurso, data_concurso, arrecadacao_total, acumulado, valor_acumulado, estimativa_premio, acumulado_especial, observacao, time_coracao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, concurso.getLoteriaId());
            ps.setInt(2, concurso.getNumero());
            ps.setDate(3, java.sql.Date.valueOf(concurso.getData()));
            if (concurso.getArrecadacaoTotal() != null) ps.setBigDecimal(4, concurso.getArrecadacaoTotal());
            else ps.setNull(4, Types.NUMERIC);
            if (concurso.getAcumulado() != null) ps.setBoolean(5, concurso.getAcumulado());
            else ps.setNull(5, Types.BOOLEAN);
            if (concurso.getValorAcumulado() != null) ps.setBigDecimal(6, concurso.getValorAcumulado());
            else ps.setNull(6, Types.NUMERIC);
            if (concurso.getEstimativaPremio() != null) ps.setBigDecimal(7, concurso.getEstimativaPremio());
            else ps.setNull(7, Types.NUMERIC);
            if (concurso.getAcumuladoEspecial() != null) ps.setBigDecimal(8, concurso.getAcumuladoEspecial());
            else ps.setNull(8, Types.NUMERIC);
            ps.setString(9, concurso.getObservacao());
            ps.setString(10, concurso.getTimeCoracao());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new Exception("Falha ao inserir concurso.");
    }

    /**
     * Lista todos os concursos de uma loteria em ordem decrescente do número do concurso, trazendo também o ID.
     */
    public List<Concurso> listarConcursosPorLoteria(int loteriaId) throws Exception {
        try (Connection conn = ConexaoBanco.getConnection()) {
            return listarConcursosPorLoteria(loteriaId, conn);
        }
    }

    public List<Concurso> listarConcursosPorLoteria(int loteriaId, Connection conn) throws Exception {
        String sql = "SELECT id, numero_concurso, data_concurso FROM concurso WHERE loteria_id = ? ORDER BY numero_concurso DESC";
        List<Concurso> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Concurso c = new Concurso();
                c.setId(rs.getInt("id"));
                c.setNumero(rs.getInt("numero_concurso"));
                // Se precisar da data, set também:
                Date dataSql = rs.getDate("data_concurso");
                if (dataSql != null) c.setData(dataSql.toLocalDate());
                c.setLoteriaId(loteriaId);
                lista.add(c);
            }
        }
        return lista;
    }

    public Concurso buscarUltimoConcursoPorLoteria(int loteriaId) throws Exception {
        String sql = "SELECT id, numero_concurso, data_concurso FROM concurso WHERE loteria_id = ? ORDER BY numero_concurso DESC LIMIT 1";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Concurso c = new Concurso();
                    c.setId(rs.getInt("id"));
                    c.setNumero(rs.getInt("numero_concurso"));
                    c.setData(rs.getDate("data_concurso").toLocalDate());
                    c.setLoteriaId(loteriaId);
                    return c;
                }
            }
        }
        return null;
    }

    // NOVO MÉTODO: retorna os números sorteados do concurso em ordem
    public List<Integer> buscarNumerosSorteadosDoConcurso(int concursoId) throws Exception {
        List<Integer> numeros = new ArrayList<>();
        String sql = "SELECT numero FROM concurso_numero_sorteado WHERE concurso_id = ? ORDER BY ordem";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concursoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                numeros.add(rs.getInt("numero"));
            }
        }
        return numeros;
    }
}