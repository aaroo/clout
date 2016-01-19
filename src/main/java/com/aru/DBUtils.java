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
 */
public class DBUtils {

    public static final String SUBJECT = "subject";
    public static final String NAME = "name";
    public static final String SELECT_ALL_FROM_FOLLOWS = "SELECT name, subject from FOLLOWS";
    public static final String INSERT_INTO_FOLLOWS = "INSERT INTO FOLLOWS(name, subject) VALUES (?, ?)";
    public static final String MERGE_INTO_FOLLOWS = "MERGE INTO FOLLOWS(name, subject) KEY(name) VALUES (?, ?)";
    public static final String SELECT_SUBJECT_FROM_FOLLOWS = "SELECT name, subject from FOLLOWS where subject=?";

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

    private static void addTOSubjectList(List<String> subjectList, String newSubject) {
        if(!subjectList.contains(newSubject) &&!StringUtils.isEmpty(newSubject)) {
            subjectList.add(newSubject);
        }
    }

    private static PreparedStatement getPreparedStatement(String selectStatement) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        return connection.prepareStatement(selectStatement);
    }

    private static void closeConnection(Statement statement) {
        try {
            statement.close();
        } catch (SQLException e) {
            System.err.println("Unable to close db connection");
        }
    }
}
