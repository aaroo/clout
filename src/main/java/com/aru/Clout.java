package com.aru;

import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

/**
 * Created by avenkat
 * Main() class for the Clout application.
 * Takes user input line and passes it on to the CloutCommandParser
 */
public class Clout {

    public static final String EXIT = "exit";

    public static void main(String[] args) {
        CloutCommandParser cloutCommandParser = new CloutCommandParser();
        boolean running = true;
        Scanner sc = new Scanner(System.in);
        System.out.println("> clout\n");
        while(running) {
            System.out.print(">");
            String input = sc.nextLine();
            if(StringUtils.equalsIgnoreCase(EXIT, input)) {
                running = false;
            }
            cloutCommandParser.parseInput(input);
        }

    }

}
