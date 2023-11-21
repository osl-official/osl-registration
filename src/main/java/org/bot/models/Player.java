package org.bot.models;

import org.bot.enums.League;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public record Player(long discordId, OptionalInt slapId, @Nullable League league) {

    public Player() {
        this(-1L);
    }

    public Player(long discordId) {
        this(discordId, OptionalInt.empty(), null);
    }

    public Player(long discordId, League league) {
        this(discordId, OptionalInt.empty(), league);
    }

    public Player(long discordId, int slapId) {
        this(discordId, OptionalInt.of(slapId), null);
    }

    public boolean isNull() {
        return discordId == -1L;
    }
}
