package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Concurso;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ConcursoDAO {

    public boolean existeConcurso(Integer loteriaId, Integer numeroConcurso) throws Exception {
        String sql = "SELECT id FROM concurso WHERE loteria_id = ? AND numero = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ps.setInt(2, numeroConcurso);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public int inserirConcurso(Concurso concurso) throws Exception {
        String sql = "INSERT INTO concurso (loteria_id, numero, data) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, concurso.getLoteriaId());
            ps.setInt(2, concurso.getNumero());
            ps.setDate(3, Date.valueOf(concurso.getData()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new Exception("Erro ao obter ID do concurso inserido.");
            }
        }
    }

    public List<Concurso> listarConcursosPorLoteria(Integer loteriaId) throws Exception {
        String sql = "SELECT id, loteria_id, numero, data FROM concurso WHERE loteria_id = ? ORDER BY numero DESC";
        List<Concurso> concursos = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Concurso concurso = new Concurso();
                concurso.setId(rs.getInt("id"));
                concurso.setLoteriaId(rs.getInt("loteria_id"));
                concurso.setNumero(rs.getInt("numero"));
                concurso.setData(rs.getDate("data").toLocalDate());
                concursos.add(concurso);
            }
        }
        return concursos;
    }

    // Adicione outros métodos conforme necessário
}