package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Jogo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JogoDAO {

    public void inserirJogo(Jogo jogo) throws Exception {
        String sql = "INSERT INTO jogo (loteria_id, concurso_id, numeros) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jogo.getLoteriaId());
            ps.setInt(2, jogo.getConcursoId());
            ps.setString(3, jogo.getNumeros());
            ps.executeUpdate();
        }
    }

    public List<Jogo> listarJogos() throws Exception {
        String sql = "SELECT id, loteria_id, concurso_id, numeros FROM jogo";
        List<Jogo> jogos = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Jogo jogo = new Jogo();
                jogo.setId(rs.getInt("id"));
                jogo.setLoteriaId(rs.getInt("loteria_id"));
                jogo.setConcursoId(rs.getInt("concurso_id"));
                jogo.setNumeros(rs.getString("numeros"));
                jogos.add(jogo);
            }
        }
        return jogos;
    }

    public void salvarJogos(List<Jogo> jogos) throws Exception {
        String sql = "INSERT INTO jogo (loteria_id, concurso_id, numeros) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Jogo jogo : jogos) {
                ps.setInt(1, jogo.getLoteriaId());
                ps.setInt(2, jogo.getConcursoId());
                ps.setString(3, jogo.getNumeros());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}