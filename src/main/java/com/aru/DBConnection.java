package com.aru;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by avenkat
 */
public class DBConnection {

    private final JdbcConnectionPool jdbcConnectionPool;

    private static class SingletonHolder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private DBConnection() {
        jdbcConnectionPool = JdbcConnectionPool.create("jdbc:h2:mem:db1:mode=MySQL", "aru", "aru");
        jdbcConnectionPool.setMaxConnections(100);
        createTables();

    }

    private void createTables() {
        Connection connection = getConnection();
        if (connection != null) {
            Statement statement = null;
            try {
                statement = connection.createStatement();
            } catch (SQLException e) {
                System.err.println("Could not execute command, Sorry! " + e);
            }

            if (statement != null) {
                createFollowsTable(statement);
            }
        }
    }

    private void createFollowsTable(Statement statement) {
        try {
            String sql = "CREATE TABLE FOLLOWS " +
                    "(name VARCHAR(255), " +
                    " subject VARCHAR(255), " +
                    " PRIMARY KEY ( name ))";

            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    public Connection getConnection() {
        try {
            return jdbcConnectionPool.getConnection();
        } catch (SQLException e) {
            System.err.println("Error getting DB connection: " + e);
        }
        return null;
    }
}
