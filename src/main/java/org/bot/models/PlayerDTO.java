package org.bot.models;

import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {
    @Setter
    private long discordId;
    @Setter
    private int slapId;
    @Setter
    private String league;

    public PlayerDTO(long discordId, int slapId) {
        this.slapId = slapId;
        this.discordId = discordId;
    }
}
