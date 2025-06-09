package com.gestaoloteria.loteria.dao;

import com.gestaoloteria.loteria.ConexaoBanco;
import com.gestaoloteria.loteria.model.FaixaPremiacao;
import com.gestaoloteria.loteria.model.Loteria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoteriaDAO {

    public void salvarLoteriaComFaixas(Loteria loteria) throws Exception {
        String insertLoteria = "INSERT INTO loteria (nome, descricao, quantidade_numeros_aposta_min, quantidade_numeros_aposta_max, quantidade_numeros_sorteados) VALUES (?, ?, ?, ?, ?) RETURNING id";
        String insertFaixa = "INSERT INTO faixa_premiacao (loteria_id, nome, acertos, ordem) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psLoteria = conn.prepareStatement(insertLoteria)) {
                psLoteria.setString(1, loteria.getNome());
                psLoteria.setString(2, loteria.getDescricao());
                psLoteria.setInt(3, loteria.getQtdMin());
                psLoteria.setInt(4, loteria.getQtdMax());
                psLoteria.setInt(5, loteria.getQtdSorteados());
                ResultSet rs = psLoteria.executeQuery();
                if (rs.next()) {
                    int loteriaId = rs.getInt(1);
                    try (PreparedStatement psFaixa = conn.prepareStatement(insertFaixa)) {
                        for (FaixaPremiacao faixa : loteria.getFaixas()) {
                            psFaixa.setInt(1, loteriaId);
                            psFaixa.setString(2, faixa.getNome());
                            psFaixa.setInt(3, faixa.getAcertos());
                            psFaixa.setInt(4, faixa.getOrdem());
                            psFaixa.addBatch();
                        }
                        psFaixa.executeBatch();
                    }
                    conn.commit();
                } else {
                    conn.rollback();
                    throw new SQLException("Erro ao inserir loteria.");
                }
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public void atualizarLoteriaComFaixas(Loteria loteria) throws Exception {
        String updateLoteria = "UPDATE loteria SET nome=?, descricao=?, quantidade_numeros_aposta_min=?, quantidade_numeros_aposta_max=?, quantidade_numeros_sorteados=? WHERE id=?";
        String deleteFaixas = "DELETE FROM faixa_premiacao WHERE loteria_id=?";
        String insertFaixa = "INSERT INTO faixa_premiacao (loteria_id, nome, acertos, ordem) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psLoteria = conn.prepareStatement(updateLoteria)) {
                psLoteria.setString(1, loteria.getNome());
                psLoteria.setString(2, loteria.getDescricao());
                psLoteria.setInt(3, loteria.getQtdMin());
                psLoteria.setInt(4, loteria.getQtdMax());
                psLoteria.setInt(5, loteria.getQtdSorteados());
                psLoteria.setInt(6, loteria.getId());
                psLoteria.executeUpdate();
            }
            try (PreparedStatement psDelete = conn.prepareStatement(deleteFaixas)) {
                psDelete.setInt(1, loteria.getId());
                psDelete.executeUpdate();
            }
            try (PreparedStatement psFaixa = conn.prepareStatement(insertFaixa)) {
                for (FaixaPremiacao faixa : loteria.getFaixas()) {
                    psFaixa.setInt(1, loteria.getId());
                    psFaixa.setString(2, faixa.getNome());
                    psFaixa.setInt(3, faixa.getAcertos());
                    psFaixa.setInt(4, faixa.getOrdem());
                    psFaixa.addBatch();
                }
                psFaixa.executeBatch();
            }
            conn.commit();
        }
    }

    public void excluirLoteria(int loteriaId) throws Exception {
        String deleteFaixas = "DELETE FROM faixa_premiacao WHERE loteria_id=?";
        String deleteLoteria = "DELETE FROM loteria WHERE id=?";
        try (Connection conn = ConexaoBanco.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psFaixas = conn.prepareStatement(deleteFaixas)) {
                psFaixas.setInt(1, loteriaId);
                psFaixas.executeUpdate();
            }
            try (PreparedStatement psLoteria = conn.prepareStatement(deleteLoteria)) {
                psLoteria.setInt(1, loteriaId);
                psLoteria.executeUpdate();
            }
            conn.commit();
        }
    }

    public List<Loteria> listarLoterias() throws Exception {
        List<Loteria> lista = new ArrayList<>();
        String sql = "SELECT * FROM loteria";
        try (Connection conn = ConexaoBanco.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Loteria l = new Loteria();
                l.setId(rs.getInt("id"));
                l.setNome(rs.getString("nome"));
                l.setDescricao(rs.getString("descricao"));
                l.setQtdMin(rs.getInt("quantidade_numeros_aposta_min"));
                l.setQtdMax(rs.getInt("quantidade_numeros_aposta_max"));
                l.setQtdSorteados(rs.getInt("quantidade_numeros_sorteados"));
                l.setFaixas(listarFaixasPorLoteria(l.getId()));
                lista.add(l);
            }
        }
        return lista;
    }

    public Loteria obterLoteriaPorId(int loteriaId) throws Exception {
        String sql = "SELECT * FROM loteria WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Loteria l = new Loteria();
                l.setId(rs.getInt("id"));
                l.setNome(rs.getString("nome"));
                l.setDescricao(rs.getString("descricao"));
                l.setQtdMin(rs.getInt("quantidade_numeros_aposta_min"));
                l.setQtdMax(rs.getInt("quantidade_numeros_aposta_max"));
                l.setQtdSorteados(rs.getInt("quantidade_numeros_sorteados"));
                l.setFaixas(listarFaixasPorLoteria(l.getId()));
                return l;
            }
        }
        return null;
    }

    public List<FaixaPremiacao> listarFaixasPorLoteria(int loteriaId) throws Exception {
        List<FaixaPremiacao> faixas = new ArrayList<>();
        String sql = "SELECT * FROM faixa_premiacao WHERE loteria_id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loteriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FaixaPremiacao f = new FaixaPremiacao();
                f.setId(rs.getInt("id"));
                f.setLoteriaId(rs.getInt("loteria_id"));
                f.setNome(rs.getString("nome"));
                f.setAcertos(rs.getInt("acertos"));
                f.setOrdem(rs.getInt("ordem"));
                faixas.add(f);
            }
        }
        return faixas;
    }
}