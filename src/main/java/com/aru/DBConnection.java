package com.aru;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by avenkat
 * Connection class holds the singleton instance of the connection pool to be shared by application.
 * Other classes may request a connection to the database form the shared pool.
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

    /**
     * Create initial empty table
     */
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

    /**
     * Creates the initial "FOLLOWS" table
     * @param statement Statement used to create the table
     */
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

    /**
     * Gets a database connection from the shared connection pool
     * @return Connection to database
     */
    public Connection getConnection() {
        try {
            return jdbcConnectionPool.getConnection();
        } catch (SQLException e) {
            System.err.println("Error getting DB connection: " + e);
        }
        return null;
    }
}
