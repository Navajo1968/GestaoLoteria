package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Concurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        String sql = "INSERT INTO concurso (loteria_id, numero_concurso, data_concurso) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, concurso.getLoteriaId());
            ps.setInt(2, concurso.getNumero());
            ps.setDate(3, java.sql.Date.valueOf(concurso.getData()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new Exception("Falha ao inserir concurso.");
    }

    public List<Concurso> listarConcursosPorLoteria(int loteriaId) throws Exception {
        try (Connection conn = ConexaoBanco.getConnection()) {
            return listarConcursosPorLoteria(loteriaId, conn);
        }
    }
    public List<Concurso> listarConcursosPorLoteria(int loteriaId, Connection conn) throws Exception {
        String sql = "SELECT id, numero_concurso FROM concurso WHERE loteria_id = ?";
        List<Concurso> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Concurso c = new Concurso();
                c.setId(rs.getInt("id"));
                c.setNumero(rs.getInt("numero_concurso"));
                lista.add(c);
            }
        }
        return lista;
    }

    // NOVO: retorna o último concurso da loteria (maior numero_concurso)
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
}