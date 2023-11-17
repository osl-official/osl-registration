package org.bot.enums;

public enum League {
    PRO("Pro"),
    INTERMEDIATE("Intermediate"),
    OPEN("Open");

    public final String label;

    private League(String label) {
        this.label = label;
    }
}


