package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.ConexaoBanco;
import com.gestaoloteria.loteria.model.Concurso;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConcursoDAO {
    public int inserirConcurso(Concurso concurso) throws Exception {
        String sql = "INSERT INTO concurso (loteria_id, numero_concurso, data_concurso) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concurso.getLoteriaId());
            ps.setInt(2, concurso.getNumeroConcurso());
            ps.setDate(3, Date.valueOf(concurso.getDataConcurso()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new Exception("Falha ao inserir concurso");
            }
        }
    }

    public List<Concurso> listarConcursosPorLoteria(int loteriaId) throws Exception {
        String sql = "SELECT * FROM concurso WHERE loteria_id = ? ORDER BY numero_concurso DESC";
        List<Concurso> lista = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Concurso c = new Concurso();
                c.setId(rs.getInt("id"));
                c.setLoteriaId(rs.getInt("loteria_id"));
                c.setNumeroConcurso(rs.getInt("numero_concurso"));
                c.setDataConcurso(rs.getDate("data_concurso").toLocalDate());
                lista.add(c);
            }
        }
        return lista;
    }
}