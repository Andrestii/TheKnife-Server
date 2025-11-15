package com.theknife;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    private String hostDB, usernameDB, passwordDB;
    private Connection connection;

    public Database(String hostDB, String usernameDB, String passwordDB) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + hostDB, usernameDB, passwordDB);
            
            if(connection != null){
                System.out.println("Connection ok");
            }else{
                System.out.println("Connection failed");
            }
            //connection.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}