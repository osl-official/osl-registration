package org.bot.models;

import org.bot.enums.League;

import java.awt.Color;
import java.util.List;

public record Team(Player captain, List<Player> players, String name, String nameAbbr, Color color, League league) {
    public Team {
        if (name.trim().length() < 5) {
            throw new IllegalArgumentException("Team Name must be at least 5 characters");
        }
        if (name.trim().length() > 50) {
            throw new IllegalArgumentException("Team Name must not be longer than 50 characters");
        }
        if (players.contains(captain)) {
            throw new IllegalArgumentException("Captain cannot be included in the \"Players\" list.");
        }
        if (players.size() + 1 < 3) {
            throw new IllegalArgumentException("Teams require a minimum of 3 players including the captain.");
        }
        if (players.size() + 1 > 5) {
            throw new IllegalArgumentException("Teams require a maximum of 5 players including the captain.");
        }
        if (nameAbbr.length() > 4) {
            throw new IllegalArgumentException("Team Name Abbreviation must not be more than 4 characters long.");
        }
        if (nameAbbr.isEmpty()) {
            throw new IllegalArgumentException("Team Name Abbreviation must not be empty.");
        }
    }

    public Team(Player captain, List<Player> players) {
        this(captain, players, null, null, null, null);
    }

    public Team(Player captain, List<Player> players, String name, String nameAbbr) {
        this(captain, players, name, nameAbbr, null, null);
    }

    public Team(Player captain, List<Player> players, String name, String nameAbbr, League league) {
        this(captain, players, name, nameAbbr, null, league);
    }
}
