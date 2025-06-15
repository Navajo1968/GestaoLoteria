package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.ConcursoNumeroSorteado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

public class ConcursoNumeroSorteadoDAO {

    public void inserirNumeroSorteado(ConcursoNumeroSorteado numeroSorteado) throws Exception {
        String sql = "INSERT INTO concurso_numero_sorteado (concurso_id, numero) VALUES (?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numeroSorteado.getConcursoId());
            ps.setInt(2, numeroSorteado.getNumero());
            ps.executeUpdate();
        }
    }

    public List<ConcursoNumeroSorteado> listarNumerosPorConcurso(Integer concursoId) throws Exception {
        String sql = "SELECT id, concurso_id, numero FROM concurso_numero_sorteado WHERE concurso_id = ? ORDER BY numero";
        List<ConcursoNumeroSorteado> lista = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concursoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ConcursoNumeroSorteado num = new ConcursoNumeroSorteado();
                num.setId(rs.getInt("id"));
                num.setConcursoId(rs.getInt("concurso_id"));
                num.setNumero(rs.getInt("numero"));
                lista.add(num);
            }
        }
        return lista;
    }
}