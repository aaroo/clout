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

    public void parseInput(String input) {
        if(StringUtils.isEmpty(input)) {
            handleEmptyInput();
        } else if(StringUtils.equalsIgnoreCase(Clout.EXIT, input)) {
            handleExit();
        } else {
            Connection connection = DBConnection.getInstance().getConnection();

            if(connection != null) {
                handleCommand(input, connection);

            } else {
                System.err.println("Sorry, trouble getting a DB Connection");
            }

        }

    }

    private void handleCommand(String input, Connection connection) {
        if(Commands.CLOUT.equalsInput(input)) {
            performCloutCommand(input, connection);
        } else if(Commands.FOLLOWS.equalsInput(input)) {
            performFollowsCommand(input, connection);
        } else {
            handleDefault();
        }
    }

    private void handleEmptyInput() {
        System.out.println("We're working on a release that reads your mind, but until then, please enter a command");
    }

    private void handleExit() {
        System.exit(0);
    }

    private void performCloutCommand(String input, Connection connection) {

        List<String> fields = getFields(input, Commands.CLOUT);
        if(fields.size() == 0) {
            List<String> subjectList = new ArrayList<String>();
            String selectStatement = "SELECT name, subject from FOLLOWS";
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.prepareStatement(selectStatement);
                resultSet = statement.executeQuery();
                while(resultSet.next()) {
                    String newSubject = resultSet.getString("name");
                    addTOSubjectList(subjectList, newSubject);
                    newSubject = resultSet.getString("subject");
                    addTOSubjectList(subjectList, newSubject);
                }
                for(String follower : subjectList) {
                    int count = 0;
                    count = queryFollowersForSubjectRecursive(follower, follower, count, connection);
                    System.out.println(follower + " has " + ((count > 0) ? count : "no") + " followers " );
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
        } else if(fields.size() == 1) {
            String subject = fields.get(0);
            subject = StringUtils.strip(subject);
            int count = 0;
            count = queryFollowersForSubjectRecursive(subject, subject, count, connection);
            System.out.println(subject + " has " + ((count > 0) ? count : "no") + " followers " );
        } else {
            //TODO: Handle
        }

    }

    private void addTOSubjectList(List<String> subjectList, String newSubject) {
        if(!subjectList.contains(newSubject)) {
            subjectList.add(newSubject);
        }
    }

    private ArrayList<String> getFields(String input, Commands command) {
        ArrayList fields =  new ArrayList<String>(Arrays.asList(input.split(command.toString())));
        if(fields.contains("")) {
            fields.remove("");
        }
        return fields;
    }

    private void performFollowsCommand(String input, Connection connection) {

        List<String> fields = getFields(input, Commands.FOLLOWS);
        if(fields.size() == 2) {
            PreparedStatement statement = null;
            try {
                String insertQuery = "MERGE INTO FOLLOWS KEY(name) VALUES (?, ?)";
                statement = connection.prepareStatement(insertQuery);
                statement.setString(1, fields.get(0));
                statement.setString(2, fields.get(1));
                statement.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Could not add follower: " + e);
            } catch (Exception e) {
                System.err.println("Could not query database");
            } finally {
                if(statement != null) {
                    closeConnection(statement);
                }
            }
        } else {
            System.err.println("Sorry incorrect syntax");
        }


    }

    private int queryFollowersForSubjectRecursive(String rootSubject, String subject, int startCount, Connection connection) {
        int count = startCount;
        String selectStatement = "SELECT name, subject from FOLLOWS where subject=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            List<String> subjectList = new ArrayList<String>();
            statement = connection.prepareStatement(selectStatement);
            statement.setString(1, subject);
            resultSet = statement.executeQuery();
            while(resultSet.next()) {
                count++;
                subjectList.add(resultSet.getString("name"));
            }

            for(String follower : subjectList) {
                if(!StringUtils.equalsIgnoreCase(subject, follower) && !StringUtils.equalsIgnoreCase(rootSubject, follower)) {
                    count += queryFollowersForSubjectRecursive(rootSubject, follower, startCount, connection);
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

    private void closeConnection(Statement statement) {
        try {
            statement.close();
        } catch (SQLException e) {
            System.err.println("Unable to close db connection");
        }
    }


    private void handleDefault() {
        System.out.println("Uh oh looks like we don't support that command, please enter something else or \"exit\" to exit");
    }
}
