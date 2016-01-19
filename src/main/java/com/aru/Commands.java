package com.aru;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by avenkat
 * Enumeration of supported commands.
 */
public enum Commands {
    CLOUT("clout"),
    FOLLOWS(" follows ");

    private String command;

    Commands(String command) {
        this.command = command;
    }

    public boolean equalsInput(String input) {
        return !StringUtils.isEmpty(input) && StringUtils.containsIgnoreCase(input, command);
    }

    public String toString() {
        return this.command;
    }
}
