package com.aru;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by avenkat
 * Collections of utilities to read and write from and to the database.
 */
public class DBUtils {

    public static final String SUBJECT = "subject";
    public static final String NAME = "name";
    public static final String SELECT_ALL_FROM_FOLLOWS = "SELECT name, subject from FOLLOWS";
    public static final String INSERT_INTO_FOLLOWS = "INSERT INTO FOLLOWS(name, subject) VALUES (?, ?)";
    public static final String MERGE_INTO_FOLLOWS = "MERGE INTO FOLLOWS(name, subject) KEY(name) VALUES (?, ?)";
    public static final String SELECT_SUBJECT_FROM_FOLLOWS = "SELECT name, subject from FOLLOWS where subject=?";

    /**
     * Writes a relationship between a subject and a follower to the database.
     * @param follower follower following given subject.
     * @param subject subject being followed.
     */
    public static void writeToDB(String follower, String subject) {
        PreparedStatement statement = null;
        String writeStatement = (StringUtils.isEmpty(subject)) ? INSERT_INTO_FOLLOWS : MERGE_INTO_FOLLOWS;
        try {
            statement = getPreparedStatement(writeStatement);
            statement.setString(1, follower);
            statement.setString(2, subject);
            statement.executeUpdate();
        } catch (SQLException e) {
            String reason = e.getMessage();
            if(StringUtils.isEmpty(subject) && !StringUtils.containsIgnoreCase(reason, "Unique index or primary key violation:")) {
                System.err.println("Could not add follower: " + e);
            }
        } catch (Exception e) {
            System.err.println("Could not query database");
        } finally {
            if(statement != null) {
                closeConnection(statement);
            }
        }
    }

    /**
     * Gets a list of subjects form the database
     * @param subjectList list of subjects in database
     */
    public static void getSubjectList(List<String> subjectList) {
        PreparedStatement statement = null;
        ResultSet resultSet;
        try {
            statement = getPreparedStatement(SELECT_ALL_FROM_FOLLOWS);
            resultSet = statement.executeQuery();
            while(resultSet.next()) {
                String newSubject = resultSet.getString(NAME);
                addTOSubjectList(subjectList, newSubject);
                newSubject = resultSet.getString(SUBJECT);
                addTOSubjectList(subjectList, newSubject);
            }
        } catch (SQLException e) {
            System.err.println("Could not query database");
        } catch (Exception e) {
            System.err.println("Could not query database");
        } finally {
            if(statement != null) {
                closeConnection(statement);
            }
        }
    }

    /**
     * Queries a subject recursively for followers.
     * If a subject's followers have sub followers then they will be the subjects followers as well.
     * @param rootSubject Root subject whose followers are calculated, to avoid endless loops
     * @param subject Subject whose followers are calculated, in case they are a sub part of the overall graph
     * @param startCount starting count of number of followers for subject
     * @return Count of total recursive followers
     */
    public static int queryFollowersForSubjectRecursive(String rootSubject, String subject, int startCount) {
        int count = startCount;
        PreparedStatement statement = null;
        ResultSet resultSet;
        try {
            List<String> subjectList = new ArrayList<String>();
            statement = getPreparedStatement(SELECT_SUBJECT_FROM_FOLLOWS);
            statement.setString(1, subject);
            resultSet = statement.executeQuery();
            while(resultSet.next()) {
                count++;
                subjectList.add(resultSet.getString(NAME));
            }

            for(String follower : subjectList) {
                if(!StringUtils.equalsIgnoreCase(subject, follower) && !StringUtils.equalsIgnoreCase(rootSubject, follower)) {
                    count += queryFollowersForSubjectRecursive(rootSubject, follower, startCount);
                }
            }

        } catch (SQLException e) {
            System.err.println("Could not query database");
        } catch (Exception e) {
            System.err.println("Could not query database");
        } finally {
            if(statement != null) {
                closeConnection(statement);
            }
        }
        return count;
    }

    /**
     * Checks for presence of a subject and adds to a given subject list
     * @param subjectList list of subjects to be added onto
     * @param newSubject name of subject to be added to list
     */
    private static void addTOSubjectList(List<String> subjectList, String newSubject) {
        if(!subjectList.contains(newSubject) &&!StringUtils.isEmpty(newSubject)) {
            subjectList.add(newSubject);
        }
    }

    /**
     * Gets a prepared statement by getting a connection from shared connection pool.
     * @param selectStatement Query statement to be used
     * @return PreparedStatement for the given query
     */
    private static PreparedStatement getPreparedStatement(String selectStatement) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        return connection.prepareStatement(selectStatement);
    }

    /**
     * CLose the db connection
     * @param statement statemet whose connection needs to be closed
     */
    private static void closeConnection(Statement statement) {
        try {
            statement.close();
        } catch (SQLException e) {
            System.err.println("Unable to close db connection");
        }
    }
}
