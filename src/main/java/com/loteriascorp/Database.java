package com.loteriascorp;

import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
//import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
	private static DataSource dataSource;
//    private static final String URL = "jdbc:postgresql://localhost:5432/gestaoloterias";
//    private static final String USER = "postgres";
//    private static final String PASSWORD = "@NaVaJo68#PostGre#";

	 @Autowired
	    public Database(DataSource dataSource) {
	        Database.dataSource = dataSource;
	    }

	
    public static Connection getConnection() throws SQLException {
    	return dataSource.getConnection();
//    	   return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(Connection conn, PreparedStatement stmt, ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        if (conn != null) {
            conn.close();
        }
    }
}