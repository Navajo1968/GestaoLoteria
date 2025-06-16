package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Loteria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoteriaDAO {

    public void salvarLoteriaComFaixas(Loteria loteria) throws Exception {
        String sql = "INSERT INTO loteria (nome, descricao, quantidade_numeros_aposta_min, quantidade_numeros_aposta_max, quantidade_numeros_sorteados) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loteria.getNome());
            ps.setString(2, loteria.getDescricao());
            ps.setInt(3, loteria.getQtdMin());
            ps.setInt(4, loteria.getQtdMax());
            ps.setInt(5, loteria.getQtdSorteados());
            ps.executeUpdate();
        }
        // Aqui você faria a lógica para salvar as faixas, se necessário
    }

    public void atualizarLoteriaComFaixas(Loteria loteria) throws Exception {
        String sql = "UPDATE loteria SET nome = ?, descricao = ?, quantidade_numeros_aposta_min = ?, quantidade_numeros_aposta_max = ?, quantidade_numeros_sorteados = ? WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loteria.getNome());
            ps.setString(2, loteria.getDescricao());
            ps.setInt(3, loteria.getQtdMin());
            ps.setInt(4, loteria.getQtdMax());
            ps.setInt(5, loteria.getQtdSorteados());
            ps.setInt(6, loteria.getId());
            ps.executeUpdate();
        }
        // Aqui você faria a lógica para atualizar as faixas, se necessário
    }

    public List<Loteria> listarLoterias() throws Exception {
        List<Loteria> lista = new ArrayList<>();
        String sql = "SELECT id, nome, descricao, quantidade_numeros_aposta_min, quantidade_numeros_aposta_max, quantidade_numeros_sorteados FROM loteria";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Loteria loteria = new Loteria();
                loteria.setId(rs.getInt("id"));
                loteria.setNome(rs.getString("nome"));
                loteria.setDescricao(rs.getString("descricao"));
                loteria.setQtdMin(rs.getInt("quantidade_numeros_aposta_min"));
                loteria.setQtdMax(rs.getInt("quantidade_numeros_aposta_max"));
                loteria.setQtdSorteados(rs.getInt("quantidade_numeros_sorteados"));
                // Aqui você pode carregar as faixas, se necessário
                lista.add(loteria);
            }
        }
        return lista;
    }

    public Loteria obterLoteriaPorId(int id) throws Exception {
        String sql = "SELECT id, nome, descricao, quantidade_numeros_aposta_min, quantidade_numeros_aposta_max, quantidade_numeros_sorteados FROM loteria WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Loteria loteria = new Loteria();
                    loteria.setId(rs.getInt("id"));
                    loteria.setNome(rs.getString("nome"));
                    loteria.setDescricao(rs.getString("descricao"));
                    loteria.setQtdMin(rs.getInt("quantidade_numeros_aposta_min"));
                    loteria.setQtdMax(rs.getInt("quantidade_numeros_aposta_max"));
                    loteria.setQtdSorteados(rs.getInt("quantidade_numeros_sorteados"));
                    // Aqui você pode carregar as faixas, se necessário
                    return loteria;
                }
            }
        }
        return null;
    }
}