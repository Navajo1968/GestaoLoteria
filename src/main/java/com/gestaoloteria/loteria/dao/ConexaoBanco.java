package com.gestaoloteria.loteria.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexaoBanco {

    private static final String URL = "jdbc:sqlite:gestaoloteria.db"; // Ajuste para seu banco

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL);
    }
}