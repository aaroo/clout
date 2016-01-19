package com.aru;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by avenkat
 */
public class CloutParser {

    public static final String SUBJECT = "subject";
    public static final String NAME = "name";
    public static final String SELECT_ALL_FROM_FOLLOWS = "SELECT name, subject from FOLLOWS";
    public static final String INSERT_INTO_FOLLOWS = "INSERT INTO FOLLOWS(name, subject) VALUES (?, ?)";
    public static final String MERGE_INTO_FOLLOWS = "MERGE INTO FOLLOWS(name, subject) KEY(name) VALUES (?, ?)";
    public static final String SELECT_SUBJECT_FROM_FOLLOWS = "SELECT name, subject from FOLLOWS where subject=?";

    public void parseInput(String input) {
        if(StringUtils.isEmpty(input)) {
            handleEmptyInput();
        } else if(StringUtils.equalsIgnoreCase(Clout.EXIT, input)) {
            handleExit();
        } else {
            Connection connection = DBConnection.getInstance().getConnection();
            if(connection != null) {
                handleCommand(input);

            } else {
                System.err.println("Sorry, trouble getting a DB Connection");
            }
        }
    }

    private void handleCommand(String input) {
        if(Commands.CLOUT.equalsInput(input)) {
            performCloutCommand(input);
        } else if(Commands.FOLLOWS.equalsInput(input)) {
            performFollowsCommand(input);
        } else {
            handleDefault();
        }
    }

    private void performCloutCommand(String input) {

        List<String> fields = getFields(input, Commands.CLOUT);
        if(fields.size() == 0) {
            getEntireClout();
        } else if(fields.size() == 1) {
            getSubjectClout(fields);
        } else {
            System.err.println("Sory looks like your command was not quite correct, please try again");
        }

    }

    private void getEntireClout() {
        List<String> subjectList = new ArrayList<String>();
        getSubjectList(subjectList);

        if(!subjectList.isEmpty()) {
            for(String follower : subjectList) {
                int count = 0;
                count = queryFollowersForSubjectRecursive(follower, follower, count);
                System.out.println(follower + " has " + ((count > 0) ? count : "no") + " followers " );
            }
        }
    }

    private void getSubjectClout(List<String> fields) {
        String subject = fields.get(0);
        subject = StringUtils.strip(subject);
        getClout(subject);
    }

    private void getClout(String subject) {
        int count = 0;
        count = queryFollowersForSubjectRecursive(subject, subject, count);
        System.out.println(subject + " has " + ((count > 0) ? count : "no") + " followers " );
    }

    private void getSubjectList(List<String> subjectList) {
       // String selectStatement = SELECT_ALL_FROM_FOLLOWS;
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

    private PreparedStatement getPreparedStatement(String selectStatement) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        return connection.prepareStatement(selectStatement);
    }

    private void performFollowsCommand(String input) {
        List<String> fields = getFields(input, Commands.FOLLOWS);
        if(fields.size() == 2) {
            String follower = fields.get(0);
            String subject = fields.get(1);
            writeToDB(follower, subject);
            if(!StringUtils.equalsIgnoreCase(follower, subject)) {
                writeToDB(subject, "");
                System.out.println("OK!");
            } else {
                System.out.println("Interesting, but that doesn't make sense.");
            }
        } else {
            System.err.println("Sorry incorrect syntax");
        }
    }

    private void writeToDB(String follower, String subject) {
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

    private int queryFollowersForSubjectRecursive(String rootSubject, String subject, int startCount) {
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


    private void addTOSubjectList(List<String> subjectList, String newSubject) {
        if(!subjectList.contains(newSubject) &&!StringUtils.isEmpty(newSubject)) {
            subjectList.add(newSubject);
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<String> getFields(String input, Commands command) {
        ArrayList fields =  new ArrayList<String>(Arrays.asList(input.split(command.toString())));
        if(fields.contains("")) {
            fields.remove("");
        }
        return fields;
    }

    private void closeConnection(Statement statement) {
        try {
            statement.close();
        } catch (SQLException e) {
            System.err.println("Unable to close db connection");
        }
    }


    private void handleEmptyInput() {
        System.out.println("We're working on a release that reads your mind, but until then, please enter a command");
    }

    private void handleExit() {
        System.exit(0);
    }

    private void handleDefault() {
        System.out.println("Uh oh looks like we don't support that command, please enter something else or \"exit\" to exit");
    }
}
