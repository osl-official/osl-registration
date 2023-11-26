package org.bot.models;

import lombok.*;

import java.util.List;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {
    @Setter
    private String teamName;
    @Setter
    private String teamId;
    @Setter
    private String league;
    @Setter
    private PlayerDTO captain;
    @Setter
    private List<PlayerDTO> players;
}
