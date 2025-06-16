package com.gestaoloteria.loteria.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexaoBanco {
    private static final String URL = "jdbc:sqlite:gestaoloteria.db";

    public static Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC"); // força registro do driver!
        return DriverManager.getConnection(URL);
    }
}