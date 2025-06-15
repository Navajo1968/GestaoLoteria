package com.gestaoloteria.loteria.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexaoBanco {

    private static final String URL = "jdbc:sqlite:gestaoloteria.db"; // OU seu JDBC de produção
    // Exemplo H2: "jdbc:h2:./gestaoloteria"
    // Exemplo MySQL: "jdbc:mysql://localhost:3306/gestaoloteria?user=root&password=123"

    public static Connection getConnection() throws Exception {
        // Para SQLite, não precisa de usuário/senha
        return DriverManager.getConnection(URL);
    }
}