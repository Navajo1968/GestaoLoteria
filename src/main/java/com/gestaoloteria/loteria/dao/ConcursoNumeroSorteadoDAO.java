package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.ConcursoNumeroSorteado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ConcursoNumeroSorteadoDAO {

    public void inserirNumeroSorteado(ConcursoNumeroSorteado numeroSorteado) throws Exception {
        try (Connection conn = ConexaoBanco.getConnection()) {
            inserirNumeroSorteado(numeroSorteado, conn);
        }
    }

    public void inserirNumeroSorteado(ConcursoNumeroSorteado numeroSorteado, Connection conn) throws Exception {
        String sql = "INSERT INTO concurso_numero_sorteado (concurso_id, numero, ordem) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numeroSorteado.getConcursoId());
            ps.setInt(2, numeroSorteado.getNumero());
            ps.setInt(3, numeroSorteado.getOrdem());
            ps.executeUpdate();
        }
    }

    // Novo método otimizado para importar vários números sorteados de uma só vez (batch)
    public void inserirNumerosSorteadosBatch(List<ConcursoNumeroSorteado> numeros, Connection conn) throws Exception {
        String sql = "INSERT INTO concurso_numero_sorteado (concurso_id, numero, ordem) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ConcursoNumeroSorteado n : numeros) {
                ps.setInt(1, n.getConcursoId());
                ps.setInt(2, n.getNumero());
                ps.setInt(3, n.getOrdem());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<ConcursoNumeroSorteado> listarNumerosPorConcurso(Integer concursoId) throws Exception {
        List<ConcursoNumeroSorteado> lista = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection()) {
            lista.addAll(listarNumerosPorConcurso(concursoId, conn));
        }
        return lista;
    }

    public List<ConcursoNumeroSorteado> listarNumerosPorConcurso(Integer concursoId, Connection conn) throws Exception {
        String sql = "SELECT id, concurso_id, numero, ordem FROM concurso_numero_sorteado WHERE concurso_id = ? ORDER BY ordem";
        List<ConcursoNumeroSorteado> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, concursoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ConcursoNumeroSorteado num = new ConcursoNumeroSorteado();
                num.setId(rs.getInt("id"));
                num.setConcursoId(rs.getInt("concurso_id"));
                num.setNumero(rs.getInt("numero"));
                num.setOrdem(rs.getInt("ordem"));
                lista.add(num);
            }
        }
        return lista;
    }
}