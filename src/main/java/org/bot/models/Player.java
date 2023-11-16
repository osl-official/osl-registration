package org.bot.models;

import java.util.OptionalInt;

public record Player(long discordId, OptionalInt slapId) {

    public Player() {
        this(-1L);
    }

    public Player(long discordId) {
        this(discordId, OptionalInt.empty());
    }

    public boolean isNull() {
        return discordId == -1L;
    }
}
