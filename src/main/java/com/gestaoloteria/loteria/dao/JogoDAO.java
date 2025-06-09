package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.ConexaoBanco;
import com.gestaoloteria.loteria.model.Jogo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JogoDAO {

    public void salvarJogos(List<Jogo> jogos) throws Exception {
        String sql = "INSERT INTO jogo (loteria_id, concurso, data, dezenas) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Jogo jogo : jogos) {
                ps.setInt(1, jogo.getLoteriaId());
                ps.setInt(2, jogo.getConcurso());
                ps.setDate(3, Date.valueOf(jogo.getData()));
                ps.setString(4, dezenasToString(jogo.getDezenas()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Jogo> listarJogosPorLoteria(int loteriaId) throws Exception {
        String sql = "SELECT * FROM jogo WHERE loteria_id = ? ORDER BY concurso DESC";
        List<Jogo> lista = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Jogo jogo = new Jogo();
                jogo.setId(rs.getInt("id"));
                jogo.setLoteriaId(rs.getInt("loteria_id"));
                jogo.setConcurso(rs.getInt("concurso"));
                jogo.setData(rs.getDate("data").toLocalDate());
                jogo.setDezenas(stringToDezenas(rs.getString("dezenas")));
                lista.add(jogo);
            }
        }
        return lista;
    }

    private String dezenasToString(List<Integer> dezenas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dezenas.size(); i++) {
            sb.append(dezenas.get(i));
            if (i < dezenas.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private List<Integer> stringToDezenas(String str) {
        if (str == null || str.isEmpty()) return new ArrayList<>();
        String[] parts = str.split(",");
        List<Integer> dezenas = new ArrayList<>();
        for (String part : parts) {
            dezenas.add(Integer.parseInt(part.trim()));
        }
        return dezenas;
    }
}