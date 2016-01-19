package com.aru;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Created by avenkat
 */
public class Clout {

    public static final String EXIT = "exit";

    public static void main(String[] args) {
        CloutParser cloutParser = new CloutParser();
        boolean running = true;
       // initH2Database();
        Scanner sc = new Scanner(System.in);
        System.out.println("> clout\n");
        while(running) {

            System.out.print(">");
            String input = sc.nextLine();
            if(StringUtils.equalsIgnoreCase(EXIT, input)) {
                running = false;
            }
            cloutParser.parseInput(input);
        }

    }

  /*  private static void initH2Database() {
        JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create(
                "jdbc:h2:mem:db1", "aru", "aru");

        try {
            Connection conn = jdbcConnectionPool.getConnection();

            testDBInsert(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    private static void testDBInsert(Connection conn) {
        System.out.println("Inserting records into the table...");
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(stmt != null) {
            String sql;

            try {
                sql = "CREATE TABLE REGISTRATION " +
                        "(id INTEGER not NULL, " +
                        " name VARCHAR(255), " +
                        " PRIMARY KEY ( id ))";

                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                System.err.println(e);
            }
            System.out.println("Created table in given database...");


            try {
                sql = "INSERT INTO Registration " +
                        "VALUES (100, 'Zara', 'Ali', 18)";
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                sql = "INSERT INTO Registration " +
                        "VALUES (101, 'Mahnaz', 'Fatma', 25)";
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                sql = "INSERT INTO Registration " +
                        "VALUES (102, 'Zaid', 'Khan', 30)";
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                sql = "INSERT INTO Registration " +
                        "VALUES(103, 'Sumit', 'Mittal', 28)";
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Inserted records into the table...");
        }


    }

}
