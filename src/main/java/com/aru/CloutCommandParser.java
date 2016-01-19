package com.aru;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by avenkat
 */
public class CloutCommandParser {

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
        DBUtils.getSubjectList(subjectList);

        if(!subjectList.isEmpty()) {
            for(String follower : subjectList) {
                int count = 0;
                count = DBUtils.queryFollowersForSubjectRecursive(follower, follower, count);
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
        count = DBUtils.queryFollowersForSubjectRecursive(subject, subject, count);
        System.out.println(subject + " has " + ((count > 0) ? count : "no") + " followers " );
    }

    private void performFollowsCommand(String input) {
        List<String> fields = getFields(input, Commands.FOLLOWS);
        if(fields.size() == 2) {
            String follower = fields.get(0);
            String subject = fields.get(1);
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

    @SuppressWarnings("unchecked")
    private ArrayList<String> getFields(String input, Commands command) {
        ArrayList fields =  new ArrayList<String>(Arrays.asList(input.split(command.toString())));
        if(fields.contains("")) {
            fields.remove("");
        }
        return fields;
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
