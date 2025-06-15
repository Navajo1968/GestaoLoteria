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

    public Loteria buscarLoteriaPorId(int id) throws Exception {
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
}