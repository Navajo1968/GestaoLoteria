package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Loteria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LoteriaDAO {

    public List<Loteria> listarLoterias() throws Exception {
        String sql = "SELECT id, nome, tipo FROM loteria";
        List<Loteria> loterias = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Loteria loteria = new Loteria();
                loteria.setId(rs.getInt("id"));
                loteria.setNome(rs.getString("nome"));
                loteria.setTipo(rs.getString("tipo"));
                loterias.add(loteria);
            }
        }
        return loterias;
    }

    public Loteria obterLoteriaPorId(int id) throws Exception {
        String sql = "SELECT id, nome, tipo FROM loteria WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Loteria loteria = new Loteria();
                loteria.setId(rs.getInt("id"));
                loteria.setNome(rs.getString("nome"));
                loteria.setTipo(rs.getString("tipo"));
                return loteria;
            } else {
                return null;
            }
        }
    }

    public void excluirLoteria(int id) throws Exception {
        String sql = "DELETE FROM loteria WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void salvarLoteriaComFaixas(Loteria loteria) throws Exception {
        // Exemplo de inserção simples. Adapte para faixas se necessário.
        String sql = "INSERT INTO loteria (nome, tipo) VALUES (?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loteria.getNome());
            ps.setString(2, loteria.getTipo());
            ps.executeUpdate();
        }
    }

    public void atualizarLoteriaComFaixas(Loteria loteria) throws Exception {
        // Exemplo de update simples. Adapte para faixas se necessário.
        String sql = "UPDATE loteria SET nome = ?, tipo = ? WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loteria.getNome());
            ps.setString(2, loteria.getTipo());
            ps.setInt(3, loteria.getId());
            ps.executeUpdate();
        }
    }
}