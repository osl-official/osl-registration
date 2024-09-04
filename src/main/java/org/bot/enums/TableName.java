package org.bot.enums;

public enum TableName {
    PLAYER("Player"),
    FREEAGENT("Free Agent"),
    TEAMPLAYER("Team Player"),
    TEAM("Team");

    public final String label;

    private TableName(String label) {
        this.label = label;
    }
}
