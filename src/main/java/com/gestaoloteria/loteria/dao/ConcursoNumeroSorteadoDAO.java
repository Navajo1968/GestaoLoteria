package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.ConexaoBanco;
import com.gestaoloteria.loteria.model.ConcursoNumeroSorteado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConcursoNumeroSorteadoDAO {
    public void inserirNumerosSorteados(List<ConcursoNumeroSorteado> numeros) throws Exception {
        String sql = "INSERT INTO concurso_numero_sorteado (concurso_id, numero, ordem) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ConcursoNumeroSorteado n : numeros) {
                ps.setInt(1, n.getConcursoId());
                ps.setInt(2, n.getNumero());
                ps.setInt(3, n.getOrdem());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<ConcursoNumeroSorteado> listarNumerosPorConcurso(int concursoId) throws Exception {
        String sql = "SELECT * FROM concurso_numero_sorteado WHERE concurso_id = ? ORDER BY ordem";
        List<ConcursoNumeroSorteado> lista = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concursoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ConcursoNumeroSorteado n = new ConcursoNumeroSorteado();
                n.setId(rs.getInt("id"));
                n.setConcursoId(rs.getInt("concurso_id"));
                n.setNumero(rs.getInt("numero"));
                n.setOrdem(rs.getInt("ordem"));
                lista.add(n);
            }
        }
        return lista;
    }
}