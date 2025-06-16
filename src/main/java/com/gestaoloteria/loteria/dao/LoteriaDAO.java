package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.model.Loteria;
import com.gestaoloteria.loteria.model.FaixaPremiacao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoteriaDAO {

    public void salvarLoteriaComFaixas(Loteria loteria) throws Exception {
        String sql = "INSERT INTO loteria (nome, descricao, quantidade_numeros_aposta_min, quantidade_numeros_aposta_max, quantidade_numeros_sorteados) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, loteria.getNome());
            ps.setString(2, loteria.getDescricao());
            ps.setInt(3, loteria.getQtdMin());
            ps.setInt(4, loteria.getQtdMax());
            ps.setInt(5, loteria.getQtdSorteados());
            ps.executeUpdate();

            int loteriaId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    loteriaId = rs.getInt(1);
                }
            }
            if (loteria.getFaixas() != null && loteriaId > 0) {
                salvarFaixasPremiacao(loteriaId, loteria.getFaixas());
            }
        }
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
        excluirFaixasPremiacao(loteria.getId());
        if (loteria.getFaixas() != null) {
            salvarFaixasPremiacao(loteria.getId(), loteria.getFaixas());
        }
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
                loteria.setFaixas(listarFaixasPremiacao(loteria.getId()));
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
                    loteria.setFaixas(listarFaixasPremiacao(loteria.getId()));
                    return loteria;
                }
            }
        }
        return null;
    }

    public void excluirLoteria(int id) throws Exception {
        excluirFaixasPremiacao(id); // Exclui faixas associadas antes
        String sql = "DELETE FROM loteria WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ------ Métodos para Faixas de Premiação sem o campo descricao ------

    private void salvarFaixasPremiacao(int loteriaId, List<FaixaPremiacao> faixas) throws Exception {
        String sql = "INSERT INTO faixa_premiacao (loteria_id, nome, ordem, acertos) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (FaixaPremiacao faixa : faixas) {
                ps.setInt(1, loteriaId);
                ps.setString(2, faixa.getNome());
                ps.setInt(3, faixa.getOrdem());
                ps.setInt(4, faixa.getAcertos());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void excluirFaixasPremiacao(int loteriaId) throws Exception {
        String sql = "DELETE FROM faixa_premiacao WHERE loteria_id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ps.executeUpdate();
        }
    }

    private List<FaixaPremiacao> listarFaixasPremiacao(int loteriaId) throws Exception {
        List<FaixaPremiacao> faixas = new ArrayList<>();
        String sql = "SELECT id, nome, ordem, acertos FROM faixa_premiacao WHERE loteria_id = ? ORDER BY ordem";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FaixaPremiacao faixa = new FaixaPremiacao();
                    faixa.setId(rs.getInt("id"));
                    faixa.setNome(rs.getString("nome"));
                    faixa.setOrdem(rs.getInt("ordem"));
                    faixa.setAcertos(rs.getInt("acertos"));
                    faixas.add(faixa);
                }
            }
        }
        return faixas;
    }
}