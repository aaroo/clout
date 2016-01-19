package com.aru;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by avenkat
 * The class parses a given line of input, then extracts valid commands form the line and processes them.
 * It uses DBUtils to interact with the database to perform "clout" and "follows" commands by reading from and
 * writing to the database.
 */
public class CloutCommandParser {

    /**
     * This method will parse a line of command entered by the user and perform any possible commands
     * @param input line with command, entered by user
     */
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

    /**
     * This methdod handles valid "clout" and "follows" commands.
     * @param input command
     */
    private void handleCommand(String input) {
        if(Commands.CLOUT.equalsInput(input)) {
            performCloutCommand(input);
        } else if(Commands.FOLLOWS.equalsInput(input)) {
            performFollowsCommand(input);
        } else {
            handleDefault();
        }
    }

    /**
     * Performs a clout command with or without specific subject.
     * If there is no subject following the clout command, it will find the clout for every subject in the database.
     * If subject specified then finds that subjects clout,
     * @param input clout command, either with a subject specified, or without.
     */
    private void performCloutCommand(String input) {

        List<String> fields = getFields(input, Commands.CLOUT);
        if(fields.size() == 0) {
            getEntireClout();
        } else if(fields.size() == 1) {
            getSubjectClout(fields);
        } else {
            System.err.println("Sorry looks like your command was not quite correct, please try again");
        }

    }

    /**
     * Gets the entire clout for all subjects in the database
     */
    private void getEntireClout() {
        List<String> subjectList = new ArrayList<String>();
        DBUtils.getSubjectList(subjectList);

        if(!subjectList.isEmpty()) {
            for(String follower : subjectList) {
                int count = 0;
                count = DBUtils.queryFollowersForSubjectRecursive(follower, follower, count);
                System.out.println(follower + " has " + ((count > 0) ? count : "no") + (count ==1 ? " follower " : " followers " ));
            }
        }
    }

    /**
     * Extracts a specific subject then calls getClout(subject) on that subject.
     * @param fields input fields to extract subject from.
     */
    private void getSubjectClout(List<String> fields) {
        String subject = fields.get(0);
        subject = StringUtils.strip(subject);
        getClout(subject);
    }

    /**
     * Gets the clout for specified subject from the database
     * @param subject wSubject whose clout the user desires to see
     */
    private void getClout(String subject) {
        int count = 0;
        count = DBUtils.queryFollowersForSubjectRecursive(subject, subject, count);
        System.out.println(subject + " has " + ((count > 0) ? count : "no") + " followers " );
    }

    /**
     * Performs the follows command by extracting subject and follower and then writing the relationship to database
     * @param input Input line containing the follows command and subject and follower
     */
    private void performFollowsCommand(String input) {
        List<String> fields = getFields(input, Commands.FOLLOWS);
        if(fields.size() == 2) {
            String follower = fields.get(0);
            follower = StringUtils.trim(follower);
            String subject = fields.get(1);
            subject = StringUtils.trim(subject);
            DBUtils.writeToDB(follower, subject);
            if(!StringUtils.equalsIgnoreCase(follower, subject)) {
                DBUtils.writeToDB(subject, "");
                System.out.println("OK!");
            } else {
                System.out.println("Interesting, but that doesn't make sense.");
            }
        } else {
            System.err.println("Sorry incorrect syntax");
        }
    }

    /**
     * Extracts the operands given the command included commands from an input line
     * @param input input line with command specified
     * @param command command whose operands are to be extracted
     * @return
     */
    @SuppressWarnings("unchecked")
    private ArrayList<String> getFields(String input, Commands command) {
        ArrayList fields =  new ArrayList<String>(Arrays.asList(input.split(command.toString())));
        if(fields.contains("")) {
            fields.remove("");
        }
        return fields;
    }

    /**
     * Handles empty input by user
     */
    private void handleEmptyInput() {
        System.out.println("We're working on a release that reads your mind, but until then, please enter a command");
    }

    /**
     * Handles the exit command
     */
    private void handleExit() {
        System.exit(0);
    }

    /**
     * Messages the user about unsupported commands
     */
    private void handleDefault() {
        System.out.println("Uh oh looks like we don't support that command, please enter something else or \"exit\" to exit");
    }
}
