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
}
