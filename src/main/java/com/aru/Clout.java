package com.aru;

import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

/**
 * Created by avenkat
 */
public class Clout {

    public static final String EXIT = "exit";

    public static void main(String[] args) {
        CloutParser cloutParser = new CloutParser();
        boolean running = true;
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

}
